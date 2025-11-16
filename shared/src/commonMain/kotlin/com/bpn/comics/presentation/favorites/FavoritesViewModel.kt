package com.bpn.comics.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpn.comics.domain.usecase.GetFavoriteComicsUseCase
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
 * ViewModel for managing favorites list state and business logic.
 */
class FavoritesViewModel(
    private val getFavoriteComicsUseCase: GetFavoriteComicsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val favoritesEventManager: FavoritesEventManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        loadFavorites()
        // Observe favorites changes to refresh the list
        observeFavoritesChanges()
    }

    /**
     * Load favorite comics from database.
     */
    fun loadFavorites() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorType = null) }

            try {
                println("🔄 FavoritesViewModel: Loading favorites...")
                val favorites = getFavoriteComicsUseCase()
                _uiState.update {
                    it.copy(
                        favorites = favorites,
                        isLoading = false
                    )
                }
                println("✅ FavoritesViewModel: Loaded ${favorites.size} favorites")
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
                    "❌ FavoritesViewModel: Error loading favorites: " +
                            "${e.message} (Type: $errorType)"
                )
            }
        }
    }

    /**
     * Toggle favorite status for a comic.
     * Reloads favorites list after toggle (simpler approach).
     */
    fun toggleFavorite(comicNumber: Int) {
        viewModelScope.launch {
            try {
                toggleFavoriteUseCase(comicNumber)
                // Refresh the favorites list after toggling
                loadFavorites()
                // Notify that favorites have changed
                favoritesEventManager.notifyFavoritesChanged()
            } catch (e: Exception) {
                val errorType = if (isKtorNetworkException(e) || isIOException(e)) {
                    ErrorType.NETWORK_ERROR
                } else {
                    ErrorType.UNKNOWN_ERROR
                }
                _uiState.update {
                    it.copy(errorType = errorType)
                }
                println(
                    "❌ FavoritesViewModel: Error toggling favorite for " +
                            "comic #$comicNumber: ${e.message}"
                )
            }
        }
    }

    /**
     * Retry loading favorites after an error.
     */
    fun retry() {
        _uiState.update {
            it.copy(errorType = null)
        }
        loadFavorites()
    }

    /**
     * Observe favorites changes from other screens.
     * When favorites change elsewhere, refresh this list automatically.
     */
    private fun observeFavoritesChanges() {
        viewModelScope.launch {
            favoritesEventManager.favoritesChanged.collect {
                println("🔄 FavoritesViewModel: Favorites changed, refreshing...")
                loadFavorites()
            }
        }
    }
}

