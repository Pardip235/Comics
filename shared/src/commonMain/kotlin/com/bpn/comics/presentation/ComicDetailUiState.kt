package com.bpn.comics.presentation

import com.bpn.comics.data.model.Comic
import com.bpn.comics.util.ErrorType

/**
 * UI state for the comic detail screen.
 */
data class ComicDetailUiState(
    val comic: Comic? = null,
    val isLoading: Boolean = false,
    val errorType: ErrorType? = null
)

