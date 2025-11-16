package com.bpn.comics.domain.usecase

import com.bpn.comics.domain.repository.ComicRepository

/**
 * Use case for toggling favorite status of a comic.
 */
class ToggleFavoriteUseCase(
    private val repository: ComicRepository
) {
    /**
     * Toggle favorite status for a comic.
     * @return The new favorite status (true if now favorite, false if not)
     */
    suspend operator fun invoke(comicNumber: Int): Boolean {
        return repository.toggleFavorite(comicNumber)
    }
}

