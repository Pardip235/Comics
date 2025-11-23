package com.bpn.comics.domain.repository

import com.bpn.comics.domain.model.Comic
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing comics data.
 * 
 * Follows cache-first strategy with reactive data observation:
 * - All [observe*] methods return [Flow] that reads from database (single source of truth)
 * - [load*] methods trigger API fetches and update the cache
 * - Database changes automatically propagate through Flows
 * 
 * This interface is part of the domain layer and defines the contract for data operations.
 */
interface ComicRepository {
    /**
     * Observes all comics from the database.
     * 
     * @return A [Flow] that emits the current list of comics from the database.
     *         Automatically updates when the database changes.
     */
    fun observeComics(): Flow<List<Comic>>
    
    /**
     * Observes a specific comic by number from the database.
     * 
     * @param number The comic number to observe
     * @return A [Flow] that emits the comic if found, or null if not in database.
     *         Automatically updates when the comic is added or updated.
     */
    fun observeComicByNumber(number: Int): Flow<Comic?>
    
    /**
     * Observes all favorite comics from the database.
     * 
     * @return A [Flow] that emits the current list of favorite comics.
     *         Automatically updates when favorites change.
     */
    fun observeFavoriteComics(): Flow<List<Comic>>
    
    /**
     * Loads initial comics from the API and updates the cache.
     * 
     * @param count The number of recent comics to load
     */
    suspend fun loadInitialComics(count: Int)
    
    /**
     * Loads more comics (older ones) for pagination.
     * 
     * @param oldestComicNumber The number of the oldest comic currently displayed
     * @param count The number of comics to load
     */
    suspend fun loadMoreComics(oldestComicNumber: Int, count: Int)
    
    /**
     * Loads a specific comic by number from the API and updates the cache.
     * 
     * @param number The comic number to load
     */
    suspend fun loadComicByNumber(number: Int)
    
    /**
     * Checks if there are more comics available for pagination.
     * 
     * @param oldestComicNumber The number of the oldest comic currently displayed
     * @return `true` if there are more comics (oldestComicNumber > 1), `false` otherwise
     */
    fun hasMoreComics(oldestComicNumber: Int): Boolean
    
    /**
     * Toggles the favorite status of a comic.
     * 
     * If the comic is not in the database, fetches it from the API first.
     * 
     * @param comicNumber The number of the comic to toggle
     */
    suspend fun toggleFavorite(comicNumber: Int)
    
    /**
     * Performs automatic cache cleanup based on age and size limits.
     * 
     * Preserves favorites regardless of age or cache size.
     * 
     * @param maxAgeDays Maximum age in days for cached comics (favorites preserved)
     * @param maxNonFavoriteComics Maximum number of non-favorite comics to keep
     * @return Total number of comics deleted
     */
    suspend fun performAutomaticCacheCleanup(
        maxAgeDays: Long,
        maxNonFavoriteComics: Int
    ): Int
}
