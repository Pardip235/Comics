package com.bpn.comics.data.repository

import com.bpn.comics.data.api.XkcdApiServiceInterface
import com.bpn.comics.data.model.Comic
import com.bpn.comics.database.ComicsDatabase
import com.bpn.comics.database.TestDatabaseFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

/**
 * Unit test for ComicRepositoryImpl using MockK for API and real in-memory database.
 * Demonstrates testing the repository layer with mocked API and real database operations.
 */
class ComicRepositoryImplTest {

    private lateinit var testDatabase: ComicsDatabase

    @AfterTest
    fun tearDown() {
        // Clean up test database if needed
        // In-memory database is automatically cleaned up, but we can clear it explicitly
        if (::testDatabase.isInitialized) {
            testDatabase.comicEntityQueries.clearAllComics()
        }
    }

    @Test
    fun `getInitialComics should return comics from API and cache them in database`() = runTest {
        // Arrange
        val mockApiService = mockk<XkcdApiServiceInterface>()
        testDatabase = TestDatabaseFactory.createTestDatabase()

        val testComics = listOf(
            createTestComic(1),
            createTestComic(2),
            createTestComic(3)
        )

        coEvery { mockApiService.getRecentComics(3) } returns testComics

        val repository = ComicRepositoryImpl(mockApiService, testDatabase)

        // Act
        val result = repository.getInitialComics(3)

        // Assert
        assertEquals(3, result.size)
        assertEquals(testComics[0].num, result[0].num)
        // Verify comics are cached in database
        val cachedComics = repository.getAllCachedComics()
        assertEquals(3, cachedComics.size)
        coVerify(exactly = 1) { mockApiService.getRecentComics(3) }
    }

    @Test
    fun `toggleFavorite should toggle favorite status when comic exists in database`() = runTest {
        // Arrange
        val mockApiService = mockk<XkcdApiServiceInterface>()
        testDatabase = TestDatabaseFactory.createTestDatabase()

        val testComic = createTestComic(1, isFavorite = false)
        
        // Insert comic into database first
        testDatabase.comicEntityQueries.insertComic(
            num = testComic.num.toLong(),
            title = testComic.title,
            img = testComic.img,
            alt = testComic.alt,
            year = testComic.year,
            month = testComic.month,
            day = testComic.day,
            link = testComic.link,
            news = testComic.news,
            safe_title = testComic.safe_title,
            transcript = testComic.transcript,
            is_favorite = 0L
        )

        val repository = ComicRepositoryImpl(mockApiService, testDatabase)

        // Act - toggle favorite
        val result = repository.toggleFavorite(1)

        // Assert
        assertTrue(result, "Should return true when toggling to favorite")
        val isFavorite = repository.isFavorite(1)
        assertTrue(isFavorite, "Comic should be marked as favorite")
    }

    @Test
    fun `toggleFavorite should return false when comic not found in database`() = runTest {
        // Arrange
        val mockApiService = mockk<XkcdApiServiceInterface>()
        testDatabase = TestDatabaseFactory.createTestDatabase()

        val repository = ComicRepositoryImpl(mockApiService, testDatabase)

        // Act
        val result = repository.toggleFavorite(999)

        // Assert
        assertFalse(result, "Should return false when comic not found")
    }

    @Test
    fun `getFavoriteComics should return only favorite comics`() = runTest {
        // Arrange
        val mockApiService = mockk<XkcdApiServiceInterface>()
        testDatabase = TestDatabaseFactory.createTestDatabase()

        val comic1 = createTestComic(1, isFavorite = false)
        val comic2 = createTestComic(2, isFavorite = true)
        val comic3 = createTestComic(3, isFavorite = true)

        // Insert comics into database
        testDatabase.comicEntityQueries.insertComic(
            num = comic1.num.toLong(),
            title = comic1.title,
            img = comic1.img,
            alt = comic1.alt,
            year = comic1.year,
            month = comic1.month,
            day = comic1.day,
            link = comic1.link,
            news = comic1.news,
            safe_title = comic1.safe_title,
            transcript = comic1.transcript,
            is_favorite = 0L
        )
        testDatabase.comicEntityQueries.insertComic(
            num = comic2.num.toLong(),
            title = comic2.title,
            img = comic2.img,
            alt = comic2.alt,
            year = comic2.year,
            month = comic2.month,
            day = comic2.day,
            link = comic2.link,
            news = comic2.news,
            safe_title = comic2.safe_title,
            transcript = comic2.transcript,
            is_favorite = 1L
        )
        testDatabase.comicEntityQueries.insertComic(
            num = comic3.num.toLong(),
            title = comic3.title,
            img = comic3.img,
            alt = comic3.alt,
            year = comic3.year,
            month = comic3.month,
            day = comic3.day,
            link = comic3.link,
            news = comic3.news,
            safe_title = comic3.safe_title,
            transcript = comic3.transcript,
            is_favorite = 1L
        )

        val repository = ComicRepositoryImpl(mockApiService, testDatabase)

        // Act
        val favorites = repository.getFavoriteComics()

        // Assert
        assertEquals(2, favorites.size)
        assertTrue(favorites.all { it.isFavorite })
        assertTrue(favorites.any { it.num == 2 })
        assertTrue(favorites.any { it.num == 3 })
    }

    private fun createTestComic(num: Int, isFavorite: Boolean = false): Comic {
        return Comic(
            num = num,
            title = "Test Comic $num",
            img = "https://example.com/comic$num.jpg",
            alt = "Alt text $num",
            year = "2024",
            month = "1",
            day = num.toString(),
            isFavorite = isFavorite
        )
    }
}
