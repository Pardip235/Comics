package com.bpn.comics.data.api

import com.bpn.comics.data.model.ComicDto

/**
 * Interface for XKCD API service operations.
 * 
 * This interface abstracts the XKCD API and allows for easy mocking and testing.
 * All methods return [ComicDto] which mirrors the API response structure.
 */
interface XkcdApiServiceInterface {
    /**
     * Fetches the latest comic from the XKCD API.
     * 
     * @return The latest comic as [ComicDto]
     */
    suspend fun getLatestComic(): ComicDto
    
    /**
     * Fetches a specific comic by number from the XKCD API.
     * 
     * @param number The comic number to fetch
     * @return The comic as [ComicDto]
     */
    suspend fun getComicByNumber(number: Int): ComicDto
    
    /**
     * Fetches a range of comics from the XKCD API.
     * 
     * @param startNumber The starting comic number (inclusive)
     * @param endNumber The ending comic number (inclusive)
     * @return List of comics in the specified range
     */
    suspend fun getComicsRange(startNumber: Int, endNumber: Int): List<ComicDto>
    
    /**
     * Fetches recent comics from the XKCD API.
     * 
     * @param count Number of recent comics to fetch (default: 5)
     * @return List of recent comics, starting from the latest
     */
    suspend fun getRecentComics(count: Int = 5): List<ComicDto>
    
    /**
     * Closes the HTTP client and releases resources.
     */
    fun close()
}

