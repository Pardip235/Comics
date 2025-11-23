package com.bpn.comics.presentation.favorites

import com.bpn.comics.domain.model.Comic
import com.bpn.comics.util.ErrorType

/**
 * UI state for the favorites screen.
 * 
 * @property favorites The list of favorite comics to display
 * @property isLoading Whether favorites are being loaded
 * @property errorType The current error, if any
 */
data class FavoritesUiState(
    val favorites: List<Comic> = emptyList(),
    val isLoading: Boolean = false,
    val errorType: ErrorType? = null
)

