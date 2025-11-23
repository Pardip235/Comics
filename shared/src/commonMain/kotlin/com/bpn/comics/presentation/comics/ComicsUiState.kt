package com.bpn.comics.presentation.comics

import com.bpn.comics.domain.model.Comic
import com.bpn.comics.util.ErrorType

/**
 * UI state for the comics list screen.
 * 
 * @property comics The list of comics to display
 * @property isLoading Whether initial load is in progress
 * @property isLoadingMore Whether pagination load is in progress
 * @property isRefreshing Whether refresh operation is in progress
 * @property errorType The current error, if any
 * @property hasMore Whether there are more comics available for pagination
 */
data class ComicsUiState(
    val comics: List<Comic> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorType: ErrorType? = null,
    val hasMore: Boolean = true
)

