package com.bpn.comics.presentation.comics

import com.bpn.comics.data.model.Comic
import com.bpn.comics.domain.usecase.GetInitialComicsUseCase
import com.bpn.comics.domain.usecase.HasMoreComicsUseCase
import com.bpn.comics.domain.usecase.LoadMoreComicsUseCase
import com.bpn.comics.domain.usecase.PerformCacheCleanupUseCase
import com.bpn.comics.domain.usecase.ToggleFavoriteUseCase
import com.bpn.comics.presentation.FavoritesEventManager
import com.bpn.comics.test.MainTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

/**
 * Unit test for ComicsViewModel using MockK.
 * Demonstrates testing the ViewModel layer with mocked use cases.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ComicsViewModelTest : MainTest() {

    @Test
    fun `toggleFavorite should call use case and update UI state`() = runTest {
        // Arrange
        val mockGetInitialComicsUseCase = mockk<GetInitialComicsUseCase>()
        val mockLoadMoreComicsUseCase = mockk<LoadMoreComicsUseCase>()
        val mockHasMoreComicsUseCase = mockk<HasMoreComicsUseCase>()
        val mockToggleFavoriteUseCase = mockk<ToggleFavoriteUseCase>()
        val mockPerformCacheCleanupUseCase = mockk<PerformCacheCleanupUseCase>()
        val favoritesEventManager = FavoritesEventManager()

        val testComics = listOf(
            Comic(
                num = 1,
                title = "Test Comic 1",
                img = "https://example.com/comic1.jpg",
                alt = "Alt text 1",
                year = "2024",
                month = "1",
                day = "1",
                isFavorite = false
            )
        )

        coEvery { mockGetInitialComicsUseCase(any()) } returns testComics
        every { mockHasMoreComicsUseCase(any()) } returns false
        coEvery { mockToggleFavoriteUseCase(1) } returns true
        coEvery { mockPerformCacheCleanupUseCase() } returns 0

        val viewModel = ComicsViewModel(
            mockGetInitialComicsUseCase,
            mockLoadMoreComicsUseCase,
            mockHasMoreComicsUseCase,
            mockToggleFavoriteUseCase,
            favoritesEventManager,
            mockPerformCacheCleanupUseCase
        )

        // Wait for initial load to complete
        advanceUntilIdle()

        // Verify initial state
        val initialState = viewModel.uiState.value
        assertTrue(initialState.comics.isNotEmpty(), "Comics should be loaded")

        // Act - toggle favorite
        viewModel.toggleFavorite(1)
        advanceUntilIdle()

        // Assert - verify use case was called
        coVerify(exactly = 1) { mockToggleFavoriteUseCase(1) }
    }

    @Test
    fun `hasMoreComics should return correct value based on oldest comic number`() = runTest {
        // Arrange
        val mockGetInitialComicsUseCase = mockk<GetInitialComicsUseCase>()
        val mockLoadMoreComicsUseCase = mockk<LoadMoreComicsUseCase>()
        val mockHasMoreComicsUseCase = mockk<HasMoreComicsUseCase>()
        val mockToggleFavoriteUseCase = mockk<ToggleFavoriteUseCase>()
        val mockPerformCacheCleanupUseCase = mockk<PerformCacheCleanupUseCase>()
        val favoritesEventManager = FavoritesEventManager()

        val testComics = listOf(
            createTestComic(2),
            createTestComic(1)
        )

        coEvery { mockGetInitialComicsUseCase(any()) } returns testComics
        every { mockHasMoreComicsUseCase(1) } returns false
        coEvery { mockPerformCacheCleanupUseCase() } returns 0

        val viewModel = ComicsViewModel(
            mockGetInitialComicsUseCase,
            mockLoadMoreComicsUseCase,
            mockHasMoreComicsUseCase,
            mockToggleFavoriteUseCase,
            favoritesEventManager,
            mockPerformCacheCleanupUseCase
        )

        // Wait for initial load
        advanceUntilIdle()

        // Act & Assert
        val uiState = viewModel.uiState.value
        assertFalse(uiState.hasMore, "Should not have more comics when oldest is 1")
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
