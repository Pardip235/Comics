package com.bpn.comics.data.repository

import com.bpn.comics.data.api.XkcdApiServiceInterface
import com.bpn.comics.data.model.Comic
import com.bpn.comics.database.ComicsDatabase
import com.bpn.comics.database.ComicEntity
import com.bpn.comics.domain.repository.ComicRepository

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
        return comic
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
    
    override suspend fun clearCache() {
        database.comicEntityQueries.clearAllComics()
    }
    
    /**
     * Save comic to database, converting Comic model to ComicEntity.
     */
    private suspend fun saveComicToDatabase(comic: Comic) {
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
            transcript = comic.transcript
        )
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
            transcript = transcript ?: ""
        )
    }
}
