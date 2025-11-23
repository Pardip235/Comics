package com.bpn.comics.domain.usecase

import com.bpn.comics.domain.repository.ComicRepository

/**
 * Use case for loading initial comics from the API.
 * 
 * Triggers an API fetch for recent comics and updates the cache.
 * The updated data will automatically propagate through [ObserveComicsUseCase].
 */
class LoadInitialComicsUseCase(
    private val repository: ComicRepository
) {
    /**
     * Executes the use case to load initial comics.
     * 
     * @param count The number of recent comics to load
     */
    suspend operator fun invoke(count: Int) {
        repository.loadInitialComics(count)
    }
}

