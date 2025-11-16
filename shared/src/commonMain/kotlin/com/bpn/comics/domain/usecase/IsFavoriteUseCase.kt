package com.bpn.comics.domain.usecase

import com.bpn.comics.domain.repository.ComicRepository

/**
 * Use case for checking if a comic is marked as favorite.
 */
class IsFavoriteUseCase(
    private val repository: ComicRepository
) {
    suspend operator fun invoke(comicNumber: Int): Boolean {
        return repository.isFavorite(comicNumber)
    }
}

