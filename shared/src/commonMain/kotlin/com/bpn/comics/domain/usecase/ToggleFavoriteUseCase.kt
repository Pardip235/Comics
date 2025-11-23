package com.bpn.comics.domain.usecase

import com.bpn.comics.domain.repository.ComicRepository

/**
 * Use case for toggling the favorite status of a comic.
 * 
 * If the comic is not in the database, it will be fetched from the API first.
 * The updated favorite status will automatically propagate through observation Flows.
 *
 */
class ToggleFavoriteUseCase(
    private val repository: ComicRepository
) {
    /**
     * Executes the use case to toggle favorite status.
     * 
     * @param comicNumber The comic number to toggle
     */
    suspend operator fun invoke(comicNumber: Int) {
        repository.toggleFavorite(comicNumber)
    }
}

