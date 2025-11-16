package com.bpn.comics.presentation.favorites

import com.bpn.comics.data.model.Comic
import com.bpn.comics.util.ErrorType

/**
 * UI state for the favorites screen.
 */
data class FavoritesUiState(
    val favorites: List<Comic> = emptyList(),
    val isLoading: Boolean = false,
    val errorType: ErrorType? = null
)

