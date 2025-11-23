package com.bpn.comics.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.bpn.comics.data.api.XkcdApiServiceInterface
import com.bpn.comics.data.model.ComicDto
import com.bpn.comics.database.ComicEntity
import com.bpn.comics.database.ComicsDatabase
import com.bpn.comics.domain.model.Comic
import com.bpn.comics.domain.repository.ComicRepository
import com.bpn.comics.platform.getCurrentTimestampSeconds
import com.bpn.comics.util.CacheConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * Repository implementation following cache-first strategy with stale-while-revalidate pattern.
 * 
 * Approach consider as :
 * 1. **Database as Single Source of Truth**: All observe methods only read from database
 * 2. **Cache-First Strategy**: Always show cached data immediately
 * 3. **Stale-While-Revalidate**: Show cached data even if stale, refresh in background
 * 4. **TTL-based Refresh**: Only refresh if data is older than configured TTL
 * 5. **Explicit Refresh**: API calls only happen via explicit load* methods or background refresh
 */
class ComicRepositoryImpl(
    private val apiService: XkcdApiServiceInterface,
    database: ComicsDatabase
) : ComicRepository {

    private val queries = database.comicEntityQueries
    private val refreshScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * Observe all comics from database (cache-first).
     * If cache is empty or stale, triggers background refresh.
     */
    override fun observeComics(): Flow<List<Comic>> {
        return queries.getAllComics()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { entities ->
                val comics = entities.map { it.toDomainModel() }
                // Background refresh if cache is empty or stale
                if (comics.isEmpty() || isComicsListStale()) {
                    refreshScope.launch {
                        refreshComicsListIfNeeded()
                    }
                }
                comics
            }
            .distinctUntilChanged()
    }

    /**
     * Observe a specific comic from database (cache-first).
     * If not in cache or stale, triggers background refresh.
     */
    override fun observeComicByNumber(number: Int): Flow<Comic?> {
        return queries.getComicByNumber(number.toLong())
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { entity ->
                val comic = entity?.toDomainModel()
                // Background refresh if not in cache or stale
                if (comic == null || isComicStale(entity)) {
                    refreshScope.launch {
                        refreshComicIfNeeded(number)
                    }
                }
                comic
            }
            .onStart { emit(null) }
            .distinctUntilChanged()
    }

    /**
     * Observe favorite comics from database (cache-first).
     * Only reads from database - no API calls.
     */
    override fun observeFavoriteComics(): Flow<List<Comic>> {
        return queries.getFavoriteComics()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { entities -> entities.map { it.toDomainModel(isFavoriteOverride = true) } }
            .distinctUntilChanged()
    }

    /**
     * Force refresh initial comics from API.
     * Updates cache and timestamps.
     */
    override suspend fun loadInitialComics(count: Int) {
        val dtos = apiService.getRecentComics(count)
        insertComicsWithRefresh(dtos)
    }

    /**
     * Force refresh more comics from API.
     * Updates cache and timestamps.
     */
    override suspend fun loadMoreComics(oldestComicNumber: Int, count: Int) {
        val startNumber = maxOf(1, oldestComicNumber - count)
        val endNumber = oldestComicNumber - 1
        if (startNumber > endNumber) return
        val dtos = apiService.getComicsRange(startNumber, endNumber)
        insertComicsWithRefresh(dtos)
    }

    /**
     * Force refresh a specific comic from API.
     * Updates cache and timestamp.
     */
    override suspend fun loadComicByNumber(number: Int) {
        val dto = apiService.getComicByNumber(number)
        saveComicToDatabaseWithRefresh(dto)
    }

    override fun hasMoreComics(oldestComicNumber: Int): Boolean {
        return oldestComicNumber > 1
    }

    override suspend fun toggleFavorite(comicNumber: Int) {
        val entity = queries.getComicByNumber(comicNumber.toLong())
            .executeAsOneOrNull()

        if (entity != null) {
            val newValue = if (entity.is_favorite > 0) 0L else 1L
            queries.updateFavoriteStatus(newValue, comicNumber.toLong())
        } else {
            // If not in cache, fetch from API and save as favorite
            val dto = apiService.getComicByNumber(comicNumber)
            saveComicToDatabaseWithRefresh(dto)
            // Mark as favorite after saving
            queries.updateFavoriteStatus(1L, comicNumber.toLong())
        }
    }

    override suspend fun performAutomaticCacheCleanup(
        maxAgeDays: Long,
        maxNonFavoriteComics: Int
    ): Int {
        val currentTime = getCurrentTimestampSeconds()
        val ageThreshold = currentTime - (maxAgeDays * 24 * 60 * 60)

        val deletedByAge = clearOldComics(ageThreshold)
        val deletedBySize = clearCacheExceedingLimit(maxNonFavoriteComics)

        return deletedByAge + deletedBySize
    }

    /**
     * Check if the comics list cache is stale.
     * Uses the most recent last_refreshed_at from any comic.
     */
    private suspend fun isComicsListStale(): Boolean {
        val entities = queries.getAllComics().executeAsList()
        if (entities.isEmpty()) return true
        
        val currentTime = getCurrentTimestampSeconds()
        val oldestRefreshTime = entities.minOfOrNull { it.last_refreshed_at } ?: 0L
        return (currentTime - oldestRefreshTime) > CacheConfig.CACHE_FRESHNESS_TTL_SECONDS
    }

    /**
     * Check if a specific comic is stale.
     */
    private fun isComicStale(entity: ComicEntity): Boolean {
        val currentTime = getCurrentTimestampSeconds()
        return (currentTime - entity.last_refreshed_at) > CacheConfig.COMIC_DETAIL_CACHE_TTL_SECONDS
    }

    /**
     * Background refresh for comics list if needed.
     * Only refreshes if cache is actually stale.
     */
    private suspend fun refreshComicsListIfNeeded() {
        if (!isComicsListStale()) return
        
        try {
            val dtos = apiService.getRecentComics(10) // Default count
            insertComicsWithRefresh(dtos)
        } catch (e: Exception) {
            // Silently fail - cache remains available
        }
    }

    /**
     * Background refresh for a specific comic if needed.
     * Only refreshes if comic is actually stale.
     */
    private suspend fun refreshComicIfNeeded(number: Int) {
        val entity = queries.getComicByNumber(number.toLong()).executeAsOneOrNull()
        if (entity != null && !isComicStale(entity)) return
        
        try {
            val dto = apiService.getComicByNumber(number)
            saveComicToDatabaseWithRefresh(dto)
        } catch (e: Exception) {
            // Silently fail - cached data remains available
        }
    }

    private fun clearOldComics(olderThanTimestamp: Long): Int {
        val countBefore = getNonFavoriteComicCount()
        queries.clearOldComics(olderThanTimestamp)
        val countAfter = getNonFavoriteComicCount()
        return countBefore - countAfter
    }

    private fun clearCacheExceedingLimit(maxNonFavoriteComics: Int): Int {
        val currentCount = getNonFavoriteComicCount()
        if (currentCount <= maxNonFavoriteComics) return 0

        val toDelete = currentCount - maxNonFavoriteComics
        val oldestComics = queries.getOldestNonFavoriteComics(toDelete.toLong()).executeAsList()
        oldestComics.forEach { queries.deleteComicByNumber(it.num) }
        return oldestComics.size
    }

    private fun getNonFavoriteComicCount(): Int {
        return queries.getNonFavoriteComicCount().executeAsOne().toInt()
    }

    /**
     * Insert comics and update refresh timestamps.
     */
    private fun insertComicsWithRefresh(comics: List<ComicDto>) {
        val currentTime = getCurrentTimestampSeconds()
        comics.forEach { dto ->
            saveComicToDatabaseWithRefresh(dto, currentTime)
        }
    }

    /**
     * Save comic to database with refresh timestamp.
     */
    private fun saveComicToDatabaseWithRefresh(
        comic: ComicDto,
        refreshTime: Long = getCurrentTimestampSeconds()
    ) {
        val existingEntity = queries.getComicByNumber(comic.num.toLong())
            .executeAsOneOrNull()
        
        val existingFavorite = existingEntity?.is_favorite ?: 0L

        queries.insertComic(
            num = comic.num.toLong(),
            title = comic.title,
            img = comic.img,
            alt = comic.alt,
            year = comic.year,
            month = comic.month,
            day = comic.day,
            link = comic.link,
            news = comic.news,
            safe_title = comic.safe_title,
            transcript = comic.transcript,
            is_favorite = existingFavorite,
            last_refreshed_at = refreshTime
        )
    }

    private fun ComicEntity.toDomainModel(isFavoriteOverride: Boolean? = null): Comic {
        val favorite = isFavoriteOverride ?: (is_favorite > 0)
        return Comic(
            num = num.toInt(),
            title = title,
            img = img,
            alt = alt,
            year = year,
            month = month,
            day = day,
            link = link,
            news = news,
            safeTitle = safe_title ?: title,
            transcript = transcript ?: "",
            isFavorite = favorite
        )
    }
}
