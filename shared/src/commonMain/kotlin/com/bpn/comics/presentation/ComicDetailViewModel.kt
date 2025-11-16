package com.bpn.comics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpn.comics.domain.usecase.GetComicDetailUseCase
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
    private val getComicDetailUseCase: GetComicDetailUseCase
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
                println("❌ ComicDetailViewModel: Error loading comic #$comicNumber: ${e.message} (Type: $errorType)")
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
}

