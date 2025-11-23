package com.bpn.comics.data.api

import com.bpn.comics.data.model.ComicDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class XkcdApiService(
    private val httpClient: HttpClient
) : XkcdApiServiceInterface {

    override suspend fun getLatestComic(): ComicDto {
        val response = httpClient.get("/info.0.json")
        return response.body()
    }

    override suspend fun getComicByNumber(number: Int): ComicDto {
        val response = httpClient.get("/$number/info.0.json")
        return response.body()
    }

    override suspend fun getComicsRange(startNumber: Int, endNumber: Int): List<ComicDto> {
        val comics = mutableListOf<ComicDto>()
        for (num in startNumber..endNumber) {
            runCatching { getComicByNumber(num) }
                .onSuccess { comics.add(it) }
                .onFailure { /* Silently skip failed comics */ }
        }
        return comics
    }

    override suspend fun getRecentComics(count: Int): List<ComicDto> {
        val latestComic = getLatestComic()
        val startNumber = maxOf(1, latestComic.num - count + 1)
        return getComicsRange(startNumber, latestComic.num)
    }

    override fun close() {
        httpClient.close()
    }
}

