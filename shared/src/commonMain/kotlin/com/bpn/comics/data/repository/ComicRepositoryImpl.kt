package com.bpn.comics.data.repository

import com.bpn.comics.data.api.XkcdApiServiceInterface
import com.bpn.comics.data.model.Comic
import com.bpn.comics.database.ComicsDatabase
import com.bpn.comics.database.ComicEntity
import com.bpn.comics.domain.repository.ComicRepository
import com.bpn.comics.platform.getCurrentTimestampSeconds

/**
 * Implementation of ComicRepository.
 * Part of the data layer - handles fetching from API and caching in local database.
 */
class ComicRepositoryImpl(
    private val apiService: XkcdApiServiceInterface,
    private val database: ComicsDatabase
) : ComicRepository {
    
    override suspend fun getLatestComic(): Comic {
        // Fetch from API
        val comic = apiService.getLatestComic()
        // Cache in database
        saveComicToDatabase(comic)
        return comic
    }
    
    override suspend fun getComicByNumber(number: Int): Comic {
        // Fetch from API
        val comic = apiService.getComicByNumber(number)
        // Cache in database
        saveComicToDatabase(comic)
        // Return with favorite status from database
        val isFavorite = isFavorite(number)
        return comic.copy(isFavorite = isFavorite)
    }
    
    override suspend fun getAllCachedComics(): List<Comic> {
        return database.comicEntityQueries.getAllComics()
            .executeAsList()
            .map { it.toComic() }
    }
    
    override suspend fun getCachedComicByNumber(number: Int): Comic? {
        return database.comicEntityQueries.getComicByNumber(number.toLong())
            .executeAsOneOrNull()
            ?.toComic()
    }
    
    override suspend fun getFavoriteComics(): List<Comic> {
        return database.comicEntityQueries.getFavoriteComics()
            .executeAsList()
            .map { it.toComic() }
    }
    
    override suspend fun toggleFavorite(comicNumber: Int): Boolean {
        val comic = getCachedComicByNumber(comicNumber)
        return if (comic != null) {
            val currentFavoriteStatus = comic.isFavorite
            val newFavoriteStatus = !currentFavoriteStatus
            database.comicEntityQueries.updateFavoriteStatus(
                is_favorite = if (newFavoriteStatus) 1L else 0L,
                num = comicNumber.toLong()
            )
            newFavoriteStatus
        } else {
            // If comic is not in database, fetch from API and cache it
            try {
                val apiComic = apiService.getComicByNumber(comicNumber)
                saveComicToDatabase(apiComic.copy(isFavorite = true))
                true
            } catch (e: Exception) {
                println("❌ ComicRepository: Error fetching comic for favorite: ${e.message}")
                false
            }
        }
    }
    
    override suspend fun isFavorite(comicNumber: Int): Boolean {
        return database.comicEntityQueries.isFavorite(comicNumber.toLong())
            .executeAsOne() > 0
    }
    
    override suspend fun getInitialComics(count: Int): List<Comic> {
        return try {
            val comics = apiService.getRecentComics(count)
            // Cache the comics locally and merge with favorite status
            insertComics(comics)
            // Return comics with favorite status from database
            comics.map { comic ->
                val isFavorite = isFavorite(comic.num)
                comic.copy(isFavorite = isFavorite)
            }
        } catch (e: Exception) {
            println("❌ ComicRepository: Error fetching initial comics: ${e.message}")
            println("📦 ComicRepository: Falling back to cached comics")
            // Fallback to cached comics if available (already have favorite status)
            val cachedComics = getAllCachedComics().take(count)
            if (cachedComics.isNotEmpty()) {
                return cachedComics.sortedByDescending { it.num }
            }
            emptyList()
        }
    }
    
    override suspend fun loadMoreComics(oldestComicNumber: Int, count: Int): List<Comic> {
        return try {
            val startNumber = maxOf(1, oldestComicNumber - count)
            val endNumber = oldestComicNumber - 1
            val comics = apiService.getComicsRange(startNumber, endNumber)
            // Cache the comics locally and merge with favorite status
            insertComics(comics)
            // Return comics with favorite status from database
            comics.map { comic ->
                val isFavorite = isFavorite(comic.num)
                comic.copy(isFavorite = isFavorite)
            }
        } catch (e: Exception) {
            println("❌ ComicRepository: Error loading more comics: ${e.message}")
            println("📦 ComicRepository: Falling back to cached comics for pagination")
            val cachedComics = getAllCachedComics()
                .filter { it.num < oldestComicNumber }
                .sortedByDescending { it.num }
                .take(count)
            if (cachedComics.isNotEmpty()) {
                return cachedComics
            }
            emptyList()
        }
    }
    
    override fun hasMoreComics(oldestComicNumber: Int): Boolean {
        return oldestComicNumber > 1
    }
    
    override suspend fun performAutomaticCacheCleanup(
        maxAgeDays: Long,
        maxNonFavoriteComics: Int
    ): Int {
        // Calculate timestamp threshold (current time - maxAgeDays)
        val currentTime = getCurrentTimestampSeconds()
        val ageThreshold = currentTime - (maxAgeDays * 24 * 60 * 60)
        
        // Clear old comics (time-based) - preserves favorites
        val deletedByAge = clearOldComics(ageThreshold)
        
        // Clear comics exceeding size limit (size-based) - preserves favorites
        val deletedBySize = clearCacheExceedingLimit(maxNonFavoriteComics)
        
        // Return total deleted
        return deletedByAge + deletedBySize
    }
    
    /**
     * Clear comics older than the specified timestamp, preserving favorites.
     * Internal implementation detail used by performAutomaticCacheCleanup.
     */
    private fun clearOldComics(olderThanTimestamp: Long): Int {
        // Get count before deletion
        val countBefore = getNonFavoriteComicCount()
        
        // Delete old comics (preserves favorites)
        database.comicEntityQueries.clearOldComics(olderThanTimestamp)
        
        // Get count after deletion
        val countAfter = getNonFavoriteComicCount()
        
        return countBefore - countAfter
    }
    
    /**
     * Clear cache to stay within size limit, preserving favorites.
     * Internal implementation detail used by performAutomaticCacheCleanup.
     */
    private fun clearCacheExceedingLimit(maxNonFavoriteComics: Int): Int {
        val currentCount = getNonFavoriteComicCount()
        if (currentCount <= maxNonFavoriteComics) {
            return 0
        }
        
        val toDelete = currentCount - maxNonFavoriteComics
        val oldestComics = database.comicEntityQueries.getOldestNonFavoriteComics(toDelete.toLong())
            .executeAsList()
        
        // Delete the oldest non-favorite comics
        oldestComics.forEach { comicEntity ->
            database.comicEntityQueries.deleteComicByNumber(comicEntity.num)
        }
        
        return oldestComics.size
    }
    
    /**
     * Get the count of non-favorite comics in cache.
     * Internal implementation detail used by cache cleanup methods.
     */
    private fun getNonFavoriteComicCount(): Int {
        return database.comicEntityQueries.getNonFavoriteComicCount()
            .executeAsOne()
            .toInt()
    }
    
    /**
     * Save comic to database, converting Comic model to ComicEntity.
     * Preserves favorite status if comic already exists in database.
     */
    private suspend fun saveComicToDatabase(comic: Comic) {
        // Check if comic exists and preserve favorite status
        val existingComic = getCachedComicByNumber(comic.num)
        val favoriteStatus = existingComic?.isFavorite ?: comic.isFavorite
        
        database.comicEntityQueries.insertComic(
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
            is_favorite = if (favoriteStatus) 1L else 0L
        )
    }
    
    /**
     * Insert multiple comics to database.
     */
    private suspend fun insertComics(comics: List<Comic>) {
        comics.forEach { comic ->
            saveComicToDatabase(comic)
        }
    }
    
    /**
     * Convert ComicEntity to Comic model.
     */
    private fun ComicEntity.toComic(): Comic {
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
            safe_title = safe_title ?: title,
            transcript = transcript ?: "",
            isFavorite = is_favorite > 0 // Convert database value to boolean
        )
    }
}
