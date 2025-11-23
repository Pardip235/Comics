package com.bpn.comics.presentation.comicdetail

import com.bpn.comics.domain.model.Comic
import com.bpn.comics.util.ErrorType

/**
 * UI state for the comic detail screen.
 * 
 * @property comic The comic to display, or null if not loaded
 * @property isLoading Whether the comic is being loaded
 * @property errorType The current error, if any
 */
data class ComicDetailUiState(
    val comic: Comic? = null,
    val isLoading: Boolean = false,
    val errorType: ErrorType? = null
)

