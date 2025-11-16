package com.bpn.comics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpn.comics.domain.usecase.GetInitialComicsUseCase
import com.bpn.comics.domain.usecase.HasMoreComicsUseCase
import com.bpn.comics.domain.usecase.LoadMoreComicsUseCase
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
    private val hasMoreComicsUseCase: HasMoreComicsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ComicsUiState())
    val uiState: StateFlow<ComicsUiState> = _uiState.asStateFlow()

    init {
        loadInitialComics()
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
                println("❌ ViewModel: Error loading initial comics: ${e.message} (Type: $errorType)")
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
                val moreComics = loadMoreComicsUseCase(oldestComicNumber, PaginationConfig.PAGINATION_LOAD_COUNT)
                
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
                    println("✅ ViewModel: Loaded ${moreComics.size} more comics. Total: ${updatedComics.size}")
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
                println("❌ ViewModel: Error loading more comics: ${e.message} (Type: $errorType)")
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

