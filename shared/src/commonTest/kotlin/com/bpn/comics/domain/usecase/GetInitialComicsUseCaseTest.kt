package com.bpn.comics.domain.usecase

import com.bpn.comics.data.model.Comic
import com.bpn.comics.domain.repository.ComicRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

/**
 * Unit test for GetInitialComicsUseCase using MockK.
 * Demonstrates testing the use case layer with mocked repository.
 */
class GetInitialComicsUseCaseTest {

    @Test
    fun `invoke should call repository getInitialComics with correct count`() = runTest {
        // Arrange
        val mockRepository = mockk<ComicRepository>()
        val expectedComics = listOf(
            Comic(
                num = 1,
                title = "Test Comic 1",
                img = "https://example.com/comic1.jpg",
                alt = "Alt text 1",
                year = "2024",
                month = "1",
                day = "1"
            ),
            Comic(
                num = 2,
                title = "Test Comic 2",
                img = "https://example.com/comic2.jpg",
                alt = "Alt text 2",
                year = "2024",
                month = "1",
                day = "2"
            )
        )

        coEvery { mockRepository.getInitialComics(2) } returns expectedComics

        val useCase = GetInitialComicsUseCase(mockRepository)

        // Act
        val result = useCase(2)

        // Assert
        assertEquals(2, result.size)
        assertEquals(expectedComics, result)
        coVerify(exactly = 1) { mockRepository.getInitialComics(2) }
    }

    @Test
    fun `invoke should use default count when not specified`() = runTest {
        // Arrange
        val mockRepository = mockk<ComicRepository>()
        val expectedComics = (1..10).map { createTestComic(it) }

        coEvery { mockRepository.getInitialComics(10) } returns expectedComics

        val useCase = GetInitialComicsUseCase(mockRepository)

        // Act
        val result = useCase()

        // Assert
        assertEquals(10, result.size)
        coVerify(exactly = 1) { mockRepository.getInitialComics(10) }
    }

    private fun createTestComic(num: Int): Comic {
        return Comic(
            num = num,
            title = "Test Comic $num",
            img = "https://example.com/comic$num.jpg",
            alt = "Alt text $num",
            year = "2024",
            month = "1",
            day = num.toString()
        )
    }
}
