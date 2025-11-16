package com.bpn.comics.domain.usecase

import com.bpn.comics.domain.repository.ComicRepository

/**
 * Use case for checking if there are more comics available.
 * Encapsulates the business logic for pagination state.
 */
class HasMoreComicsUseCase(
    private val repository: ComicRepository
) {
    /**
     * Execute the use case to check if more comics are available.
     * 
     * @param oldestComicNumber The number of the oldest comic currently displayed
     * @return true if there are more comics available (comic number > 1), false otherwise
     */
    operator fun invoke(oldestComicNumber: Int): Boolean {
        return repository.hasMoreComics(oldestComicNumber)
    }
}

