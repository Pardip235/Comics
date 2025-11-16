package com.bpn.comics.presentation

import com.bpn.comics.data.model.Comic
import com.bpn.comics.util.ErrorType

/**
 * UI state for the comics list screen.
 */
data class ComicsUiState(
    val comics: List<Comic> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorType: ErrorType? = null,
    val hasMore: Boolean = true
)

