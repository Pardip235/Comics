package com.bpn.comics.data.api

import com.bpn.comics.data.model.Comic
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class XkcdApiService(
    private val httpClient: HttpClient
) : XkcdApiServiceInterface {

    override suspend fun getLatestComic(): Comic {
        val response = httpClient.get("/info.0.json")
        val comic = response.body<Comic>()
        return comic
    }

    override suspend fun getComicByNumber(number: Int): Comic {
        val response = httpClient.get("/$number/info.0.json")
        val comic = response.body<Comic>()
        return comic
    }

    override suspend fun getComicsRange(startNumber: Int, endNumber: Int): List<Comic> {
        println("🔍 XkcdApiService: Fetching comics range $startNumber-$endNumber...")
        val comics = mutableListOf<Comic>()
        for (num in startNumber..endNumber) {
            try {
                val comic = getComicByNumber(num)
                comics.add(comic)
            } catch (e: Exception) {
                println("❌ XkcdApiService: Failed to fetch comic #$num: ${e.message}")
                continue
            }
        }
        println("✅ XkcdApiService: Fetched ${comics.size} comics from range")
        return comics
    }

    override suspend fun getRecentComics(count: Int): List<Comic> {
        println("🔍 XkcdApiService: Fetching $count recent comics...")
        val latestComic = getLatestComic()
        val startNumber = maxOf(1, latestComic.num - count + 1)
        val comics = getComicsRange(startNumber, latestComic.num)
        println("✅ XkcdApiService: Returning ${comics.size} recent comics")
        return comics
    }

    override fun close() {
        httpClient.close()
    }
}

