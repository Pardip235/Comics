package com.bpn.comics.presentation.comicdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpn.comics.domain.usecase.GetComicDetailUseCase
import com.bpn.comics.domain.usecase.ToggleFavoriteUseCase
import com.bpn.comics.presentation.FavoritesEventManager
import com.bpn.comics.util.ErrorType
import com.bpn.comics.util.isIOException
import com.bpn.comics.util.isKtorNetworkException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for managing comic detail state and business logic.
 */
class ComicDetailViewModel(
    private val getComicDetailUseCase: GetComicDetailUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val favoritesEventManager: FavoritesEventManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ComicDetailUiState())
    val uiState: StateFlow<ComicDetailUiState> = _uiState.asStateFlow()

    /**
     * Load comic detail by number.
     */
    fun loadComicDetail(comicNumber: Int) {
        if (_uiState.value.isLoading) {
            return
        }

        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isLoading = true,
                    errorType = null
                )
            }

            try {
                println("🔄 ComicDetailViewModel: Loading comic #$comicNumber...")
                val comic = getComicDetailUseCase(comicNumber)
                _uiState.update {
                    it.copy(
                        comic = comic,
                        isLoading = false
                    )
                }
                println("✅ ComicDetailViewModel: Loaded comic #${comic.num}")
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
                    "❌ ComicDetailViewModel: Error loading comic #$comicNumber: " +
                            "${e.message} (Type: $errorType)"
                )
            }
        }
    }

    /**
     * Retry loading comic detail.
     */
    fun retry(comicNumber: Int) {
        _uiState.update { 
            it.copy(
                errorType = null
            )
        }
        loadComicDetail(comicNumber)
    }

    /**
     * Clear error.
     */
    fun clearError() {
        _uiState.update { 
            it.copy(
                errorType = null
            )
        }
    }

    /**
     * Toggle favorite status for the current comic.
     * Uses optimistic UI update for better UX.
     */
    fun toggleFavorite() {
        val currentComic = _uiState.value.comic ?: return

        // Store original favorite status for potential revert
        val originalFavoriteStatus = currentComic.isFavorite

        // Optimistic UI update - toggle immediately
        val optimisticFavoriteStatus = !originalFavoriteStatus
        _uiState.update {
            it.copy(
                comic = currentComic.copy(isFavorite = optimisticFavoriteStatus),
                errorType = null
            )
        }

        viewModelScope.launch {
            try {
                val newFavoriteStatus = toggleFavoriteUseCase(currentComic.num)
                // Update with actual result from repository
                _uiState.update {
                    it.copy(
                        comic = currentComic.copy(isFavorite = newFavoriteStatus),
                        errorType = null
                    )
                }
                println(
                    "✅ ComicDetailViewModel: Toggled favorite for " +
                            "comic #${currentComic.num}: $newFavoriteStatus"
                )
                // Notify that favorites have changed (for cross-platform observers)
                favoritesEventManager.notifyFavoritesChanged()
            } catch (e: Exception) {
                // Revert optimistic update on error
                val errorType = if (isKtorNetworkException(e) || isIOException(e)) {
                    ErrorType.NETWORK_ERROR
                } else {
                    ErrorType.UNKNOWN_ERROR
                }
                _uiState.update {
                    it.copy(
                        comic = currentComic.copy(isFavorite = originalFavoriteStatus),
                        errorType = errorType
                    )
                }
                println(
                    "❌ ComicDetailViewModel: Error toggling favorite: ${e.message}"
                )
            }
        }
    }
}

