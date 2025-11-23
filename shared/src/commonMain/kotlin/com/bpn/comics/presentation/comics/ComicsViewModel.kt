package com.bpn.comics.presentation.comics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpn.comics.domain.usecase.LoadInitialComicsUseCase
import com.bpn.comics.domain.usecase.LoadMoreComicsUseCase
import com.bpn.comics.domain.usecase.ObserveComicsUseCase
import com.bpn.comics.domain.usecase.PerformCacheCleanupUseCase
import com.bpn.comics.domain.usecase.ToggleFavoriteUseCase
import com.bpn.comics.presentation.executeWithErrorHandling
import com.bpn.comics.presentation.observeFlow
import com.bpn.comics.util.ErrorType
import com.bpn.comics.util.PaginationConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for managing comics list screen state and business logic.
 * 
 * Implements cache-first strategy with reactive data observation:
 * - Observes comics from database (single source of truth)
 * - Handles initial loading, pagination, and refresh
 * - Manages favorite toggling
 * - Performs automatic cache cleanup on initialization
 * 
 * @property uiState The current UI state exposed as a [StateFlow]
 */
class ComicsViewModel(
    private val observeComicsUseCase: ObserveComicsUseCase,
    private val loadInitialComicsUseCase: LoadInitialComicsUseCase,
    private val loadMoreComicsUseCase: LoadMoreComicsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val performCacheCleanupUseCase: PerformCacheCleanupUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ComicsUiState(isLoading = true))
    val uiState: StateFlow<ComicsUiState> = _uiState.asStateFlow()
    
    private var initialLoadJob: kotlinx.coroutines.Job? = null

    init {
        performCacheCleanup()
        // Start with loading state
        _uiState.update { it.copy(isLoading = true) }
        observeComics()
        loadInitialComics()
    }
    
    /**
     * Determines if initial load is still in progress.
     * Initial load is considered in progress if:
     * - isLoading is true AND comics list is empty (no data loaded yet)
     * - AND initial load job is still active
     */
    private val isInitialLoadInProgress: Boolean
        get() = _uiState.value.isLoading && 
                _uiState.value.comics.isEmpty() && 
                initialLoadJob?.isActive == true

    private fun performCacheCleanup() {
        viewModelScope.launch {
            runCatching { performCacheCleanupUseCase() }
                .onFailure { /* Silently fail - cache cleanup is non-critical */ }
        }
    }

    private fun observeComics() {
        observeFlow(
            flow = observeComicsUseCase(),
            onError = ::handleError,
            onSuccess = { comics ->
                val oldestComicNumber = comics.minOfOrNull { it.num } ?: 0
                // hasMore = true if no comics loaded (0) or if oldest > 1 (more available)
                // hasMore = false only when oldestComicNumber == 1 (reached the first comic)
                val hasMore = oldestComicNumber != 1
                _uiState.update { currentState ->
                    currentState.copy(
                        comics = comics,
                        // Keep isLoading = true during initial load, even if cached data exists
                        // Only set isLoading = false when initial load is complete
                        isLoading = if (isInitialLoadInProgress) {
                            // Keep loading state during initial load
                            true
                        } else {
                            // Initial load is complete, stop loading
                            false
                        },
                        isLoadingMore = false,
                        hasMore = hasMore,
                        errorType = null
                    )
                }
            }
        )
    }

    private fun loadInitialComics() {
        // Prevent duplicate initial loads
        if (initialLoadJob?.isActive == true) return

        initialLoadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorType = null) }
            executeWithErrorHandling(
                operation = { loadInitialComicsUseCase(PaginationConfig.INITIAL_LOAD_COUNT) },
                onError = { errorType ->
                    // Job will complete, marking initial load as done
                    handleError(errorType)
                }
            )
            // Job completes here - isInitialLoadInProgress will now return false
            // The Flow observation will handle updating isLoading when data arrives
            // But if we still have no data after load attempt, ensure loading is false
            // (This handles the case where API fails and cache is empty)
            if (_uiState.value.comics.isEmpty() && _uiState.value.errorType == null) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Loads more comics for pagination.
     * 
     * Fetches older comics from the API and updates the cache.
     * Only loads if there are more comics available and not already loading.
     */
    fun loadMoreComics() {
        val currentState = _uiState.value
        if (currentState.isLoadingMore || currentState.isLoading || !currentState.hasMore) return

        val oldestComicNumber = currentState.comics.minOfOrNull { it.num } ?: return

        // No more comics if we've reached comic #1
        if (oldestComicNumber == 1) {
            _uiState.update { it.copy(hasMore = false) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            executeWithErrorHandling(
                operation = {
                    loadMoreComicsUseCase(
                        oldestComicNumber = oldestComicNumber,
                        count = PaginationConfig.PAGINATION_LOAD_COUNT
                    )
                },
                onError = ::handleError
            )
        }
    }

    /**
     * Retries loading initial comics after an error.
     * Cancels any existing load operation and starts a fresh load.
     */
    fun retry() {
        // Cancel any existing initial load
        initialLoadJob?.cancel()
        _uiState.update { 
            it.copy(
                errorType = null,
                isLoading = true
            )
        }
        loadInitialComics()
    }

    /**
     * Refresh comics list from API (force refresh).
     */
    fun refresh() {
        val currentState = _uiState.value
        // Don't refresh if already refreshing or loading
        if (currentState.isRefreshing || currentState.isLoading) return

        viewModelScope.launch {
            // Show refresh indicator while keeping existing data
            _uiState.update {
                it.copy(
                    isRefreshing = true,
                    errorType = null
                )
            }

            executeWithErrorHandling(
                operation = {
                    // Force refresh from API (bypasses stale checks)
                    loadInitialComicsUseCase(PaginationConfig.INITIAL_LOAD_COUNT)
                },
                onError = { errorType ->
                    // On error, hide refresh indicator but keep cached data
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            errorType = errorType
                        )
                    }
                }
            )

            // Hide refresh indicator after operation completes
            // Note: Data will update automatically via observeComics() Flow
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    /**
     * Toggles the favorite status of a comic.
     * 
     * @param comicNumber The number of the comic to toggle
     */
    fun toggleFavorite(comicNumber: Int) {
        viewModelScope.launch {
            executeWithErrorHandling(
                operation = { toggleFavoriteUseCase(comicNumber) },
                onError = ::handleError
            )
        }
    }

    private fun handleError(errorType: ErrorType) {
        _uiState.update {
            it.copy(
                isLoading = false,
                isLoadingMore = false,
                isRefreshing = false,
                errorType = errorType
            )
        }
    }
}
