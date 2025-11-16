package com.bpn.comics.domain.usecase

import com.bpn.comics.data.model.Comic
import com.bpn.comics.domain.repository.ComicRepository

/**
 * Use case for loading more comics for pagination.
 * Encapsulates the business logic for fetching older comics.
 */
class LoadMoreComicsUseCase(
    private val repository: ComicRepository
) {
    /**
     * Execute the use case to load more comics.
     * 
     * @param oldestComicNumber The number of the oldest comic currently displayed
     * @param count Number of comics to load (default: 10)
     * @return List of comics, or empty list if API fails and no cache available
     */
    suspend operator fun invoke(oldestComicNumber: Int, count: Int = 10): List<Comic> {
        return repository.loadMoreComics(oldestComicNumber, count)
    }
}

