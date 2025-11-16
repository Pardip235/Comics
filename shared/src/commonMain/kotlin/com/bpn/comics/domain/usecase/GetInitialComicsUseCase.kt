package com.bpn.comics.domain.usecase

import com.bpn.comics.data.model.Comic
import com.bpn.comics.domain.repository.ComicRepository

/**
 * Use case for getting initial comics for the list view.
 * Encapsulates the business logic for fetching and caching initial comics.
 */
class GetInitialComicsUseCase(
    private val repository: ComicRepository
) {
    /**
     * Execute the use case to get initial comics.
     * 
     * @param count Number of comics to fetch (default: 10)
     * @return List of comics, or empty list if API fails and no cache available
     */
    suspend operator fun invoke(count: Int = 10): List<Comic> {
        return repository.getInitialComics(count)
    }
}

