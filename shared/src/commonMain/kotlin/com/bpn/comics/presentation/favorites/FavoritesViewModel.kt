package com.bpn.comics.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpn.comics.domain.usecase.ObserveFavoriteComicsUseCase
import com.bpn.comics.domain.usecase.ToggleFavoriteUseCase
import com.bpn.comics.presentation.executeWithErrorHandling
import com.bpn.comics.presentation.observeFlow
import com.bpn.comics.util.ErrorType
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for managing favorites screen state and business logic.
 * 
 * Observes favorite comics from the database and handles:
 * - Displaying all favorited comics
 * - Toggling favorite status
 * - Error handling and retry logic
 * 
 * @property uiState The current UI state exposed as a [StateFlow]
 */
class FavoritesViewModel(
    private val observeFavoriteComicsUseCase: ObserveFavoriteComicsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()
    private var favoritesJob: Job? = null

    init {
        observeFavorites()
    }

    private fun observeFavorites() {
        favoritesJob?.cancel()
        _uiState.update { it.copy(isLoading = true, errorType = null) }
        favoritesJob = observeFlow(
            flow = observeFavoriteComicsUseCase(),
            onError = ::handleError,
            onSuccess = { favorites ->
                _uiState.update {
                    it.copy(
                        favorites = favorites,
                        isLoading = false,
                        errorType = null
                    )
                }
            }
        )
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

    /**
     * Retries loading favorites after an error.
     */
    fun retry() {
        _uiState.update { it.copy(errorType = null) }
        observeFavorites()
    }

    /**
     * Refreshes the favorites list by re-observing from the database.
     * This is useful for manual refresh actions.
     */
    fun refresh() {
        _uiState.update { it.copy(errorType = null) }
        observeFavorites()
    }

    private fun handleError(errorType: ErrorType) {
        _uiState.update { it.copy(isLoading = false, errorType = errorType) }
    }
}

