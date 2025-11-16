package com.bpn.comics.domain.repository

import com.bpn.comics.data.model.Comic

/**
 * Repository interface for managing comics data.
 * Part of the domain layer - defines the contract for data operations.
 * Combines remote API and local database operations.
 */
interface ComicRepository {
    /**
     * Get the latest comic from API and cache it in database.
     */
    suspend fun getLatestComic(): Comic
    
    /**
     * Get a comic by its number from API and cache it in database.
     */
    suspend fun getComicByNumber(number: Int): Comic
    
    /**
     * Get all comics from local database, ordered by creation date (newest first).
     */
    suspend fun getAllCachedComics(): List<Comic>
    
    /**
     * Get a comic by number from local database.
     */
    suspend fun getCachedComicByNumber(number: Int): Comic?
    
    /**
     * Clear all cached comics from database.
     */
    suspend fun clearCache()
}

