package com.bpn.comics.presentation.comicdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpn.comics.domain.usecase.LoadComicByNumberUseCase
import com.bpn.comics.domain.usecase.ObserveComicByNumberUseCase
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
 * ViewModel for managing comic detail screen state and business logic.
 * 
 * Observes a specific comic from the database and handles:
 * - Loading comic details by number
 * - Toggling favorite status
 * - Error handling and retry logic
 * 
 * @property uiState The current UI state exposed as a [StateFlow]
 */
class ComicDetailViewModel(
    private val observeComicByNumberUseCase: ObserveComicByNumberUseCase,
    private val loadComicByNumberUseCase: LoadComicByNumberUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ComicDetailUiState())
    val uiState: StateFlow<ComicDetailUiState> = _uiState.asStateFlow()

    private var observedComicNumber: Int? = null
    private var observationJob: Job? = null

    /**
     * Loads comic detail by number.
     * 
     * Starts observing the comic from database and triggers API fetch if needed.
     * 
     * @param comicNumber The number of the comic to load
     */
    fun loadComicDetail(comicNumber: Int) {
        if (observedComicNumber != comicNumber) {
            observedComicNumber = comicNumber
            startObservation(comicNumber)
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorType = null) }
            executeWithErrorHandling(
                operation = { loadComicByNumberUseCase(comicNumber) },
                onError = ::handleError
            )
        }
    }

    /**
     * Retries loading the current comic after an error.
     */
    fun retry() {
        observedComicNumber?.let { loadComicDetail(it) }
    }

    /**
     * Clears the current error state.
     */
    fun clearError() {
        _uiState.update { it.copy(errorType = null) }
    }

    /**
     * Toggles the favorite status of the current comic.
     * Does nothing if no comic is currently loaded.
     */
    fun toggleFavorite() {
        val comicNumber = _uiState.value.comic?.num ?: return
        viewModelScope.launch {
            executeWithErrorHandling(
                operation = { toggleFavoriteUseCase(comicNumber) },
                onError = ::handleError
            )
        }
    }

    private fun startObservation(comicNumber: Int) {
        observationJob?.cancel()
        observationJob = observeFlow(
            flow = observeComicByNumberUseCase(comicNumber),
            onError = ::handleError,
            onSuccess = { comic ->
                _uiState.update {
                    it.copy(
                        comic = comic,
                        isLoading = false,
                        errorType = null
                    )
                }
            }
        )
    }

    private fun handleError(errorType: ErrorType) {
        _uiState.update { it.copy(isLoading = false, errorType = errorType) }
    }
}
