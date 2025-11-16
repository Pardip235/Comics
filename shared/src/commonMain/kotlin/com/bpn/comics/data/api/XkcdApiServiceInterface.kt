package com.bpn.comics.data.api

import com.bpn.comics.data.model.Comic


/**
 * Interface for xkcd API service operations.
 * This allows for easy mocking and testing.
 */
interface XkcdApiServiceInterface {
    suspend fun getLatestComic(): Comic
    suspend fun getComicByNumber(number: Int): Comic
    suspend fun getComicsRange(startNumber: Int, endNumber: Int): List<Comic>
    suspend fun getRecentComics(count: Int = 5): List<Comic>
    fun close()
}

