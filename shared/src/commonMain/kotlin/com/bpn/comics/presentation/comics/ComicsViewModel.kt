package com.bpn.comics.presentation.comics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpn.comics.domain.usecase.GetInitialComicsUseCase
import com.bpn.comics.domain.usecase.HasMoreComicsUseCase
import com.bpn.comics.domain.usecase.LoadMoreComicsUseCase
import com.bpn.comics.domain.usecase.PerformCacheCleanupUseCase
import com.bpn.comics.domain.usecase.ToggleFavoriteUseCase
import com.bpn.comics.presentation.FavoritesEventManager
import com.bpn.comics.util.ErrorType
import com.bpn.comics.util.PaginationConfig
import com.bpn.comics.util.isIOException
import com.bpn.comics.util.isKtorNetworkException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for managing comics list state and business logic.
 */
class ComicsViewModel(
    private val getInitialComicsUseCase: GetInitialComicsUseCase,
    private val loadMoreComicsUseCase: LoadMoreComicsUseCase,
    private val hasMoreComicsUseCase: HasMoreComicsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val favoritesEventManager: FavoritesEventManager,
    private val performCacheCleanupUseCase: PerformCacheCleanupUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ComicsUiState())
    val uiState: StateFlow<ComicsUiState> = _uiState.asStateFlow()

    init {
        // Perform automatic cache cleanup on app start (background task)
        performCacheCleanup()
        // Load initial comics
        loadInitialComics()
    }
    
    /**
     * Perform automatic cache cleanup in the background.
     * This runs silently without affecting the UI.
     */
    private fun performCacheCleanup() {
        viewModelScope.launch {
            try {
                val deletedCount = performCacheCleanupUseCase()
                if (deletedCount > 0) {
                    println("🧹 ComicsViewModel: Cleaned up $deletedCount old comics from cache")
                }
            } catch (e: Exception) {
                // Silently fail - cache cleanup shouldn't block app startup
                println("⚠️ ComicsViewModel: Cache cleanup failed: ${e.message}")
            }
        }
    }

    /**
     * Load initial comics when ViewModel is created.
     */
    private fun loadInitialComics() {
        if (_uiState.value.comics.isNotEmpty() || _uiState.value.isLoading) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorType = null) }
            
            try {
                println("🔄 ViewModel: Loading initial comics...")
                val comics = getInitialComicsUseCase(PaginationConfig.INITIAL_LOAD_COUNT)
                
                val oldestComicNumber = comics.minOfOrNull { it.num } ?: 0
                val hasMore = hasMoreComicsUseCase(oldestComicNumber)
                
                _uiState.update {
                    it.copy(
                        comics = comics,
                        isLoading = false,
                        hasMore = hasMore
                    )
                }
                println("✅ ViewModel: Loaded ${comics.size} initial comics")
            } catch (e: Exception) {
                val errorType = if (isKtorNetworkException(e) || isIOException(e)) {
                    ErrorType.NETWORK_ERROR
                } else {
                    ErrorType.UNKNOWN_ERROR
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorType = errorType
                    )
                }
                println(
                    "❌ ViewModel: Error loading initial comics: " +
                            "${e.message} (Type: $errorType)"
                )
            }
        }
    }

    /**
     * Load more comics for pagination.
     * Called when user scrolls near the end of the list.
     */
    fun loadMoreComics() {
        val currentState = _uiState.value
        
        if (!currentState.hasMore || currentState.isLoadingMore || currentState.isLoading) {
            return
        }

        val oldestComicNumber = currentState.comics.minOfOrNull { it.num } ?: 0
        
        if (!hasMoreComicsUseCase(oldestComicNumber)) {
            _uiState.update { it.copy(hasMore = false) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            
            try {
                println("🔄 ViewModel: Loading more comics... (oldest: $oldestComicNumber)")
                val moreComics = loadMoreComicsUseCase(
                    oldestComicNumber = oldestComicNumber,
                    count = PaginationConfig.PAGINATION_LOAD_COUNT
                )
                
                if (moreComics.isNotEmpty()) {
                    val updatedComics = currentState.comics + moreComics
                    val newOldest = updatedComics.minOfOrNull { it.num } ?: 0
                    val hasMore = hasMoreComicsUseCase(newOldest)
                    
                    _uiState.update {
                        it.copy(
                            comics = updatedComics,
                            isLoadingMore = false,
                            hasMore = hasMore
                        )
                    }
                    println("✅ ViewModel: Loaded ${moreComics.size} more comics. " +
                            "Total: ${updatedComics.size}"
                    )
                } else {
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            hasMore = false
                        )
                    }
                    println("📦 ViewModel: No more comics available")
                }
            } catch (e: Exception) {
                val errorType = if (isKtorNetworkException(e) || isIOException(e)) {
                    ErrorType.NETWORK_ERROR
                } else {
                    ErrorType.UNKNOWN_ERROR
                }
                _uiState.update {
                    it.copy(
                        isLoadingMore = false,
                        errorType = errorType
                    )
                }
                println(
                    "❌ ViewModel: Error loading more comics: " +
                            "${e.message} (Type: $errorType)"
                )
            }
        }
    }

    /**
     * Retry loading initial comics after an error.
     */
    fun retry() {
        _uiState.update { 
            it.copy(
                errorType = null
            )
        }
        loadInitialComics()
    }

    /**
     * Refresh the comics list by clearing current comics and reloading.
     * This forces a fresh load even if comics are already loaded.
     */
    fun refresh() {
        _uiState.update { 
            it.copy(
                comics = emptyList(),
                isLoading = false,
                isLoadingMore = false,
                errorType = null,
                hasMore = true
            )
        }
        loadInitialComics()
    }

    /**
     * Toggle favorite status for a comic.
     * Uses optimistic UI update for better UX.
     */
    fun toggleFavorite(comicNumber: Int) {
        val currentComic = _uiState.value.comics.find { it.num == comicNumber }
            ?: return

        // Optimistic UI update - toggle immediately
        val optimisticFavoriteStatus = !currentComic.isFavorite
        _uiState.update { currentState ->
            currentState.copy(
                comics = currentState.comics.map { comic ->
                    if (comic.num == comicNumber) {
                        comic.copy(isFavorite = optimisticFavoriteStatus)
                    } else {
                        comic
                    }
                }
            )
        }

        viewModelScope.launch {
            try {
                val newFavoriteStatus = toggleFavoriteUseCase(comicNumber)
                // Update with actual result from repository
                _uiState.update { currentState ->
                    currentState.copy(
                        comics = currentState.comics.map { comic ->
                            if (comic.num == comicNumber) {
                                comic.copy(isFavorite = newFavoriteStatus)
                            } else {
                                comic
                            }
                        },
                        errorType = null
                    )
                }
                println(
                    "✅ ComicsViewModel: Toggled favorite for " +
                            "comic #$comicNumber: $newFavoriteStatus"
                )
                // Notify that favorites have changed (for cross-platform observers)
                favoritesEventManager.notifyFavoritesChanged()
            } catch (e: Exception) {
                // Revert optimistic update on error
                _uiState.update { currentState ->
                    currentState.copy(
                        comics = currentState.comics.map { comic ->
                            if (comic.num == comicNumber) {
                                comic.copy(isFavorite = currentComic.isFavorite)
                            } else {
                                comic
                            }
                        },
                        errorType = if (isKtorNetworkException(e) || isIOException(e)) {
                            ErrorType.NETWORK_ERROR
                        } else {
                            ErrorType.UNKNOWN_ERROR
                        }
                    )
                }
                println(
                    "❌ ComicsViewModel: Error toggling favorite for " +
                            "comic #$comicNumber: ${e.message}"
                )
            }
        }
    }

}

