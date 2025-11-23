package com.bpn.comics.domain.usecase

import com.bpn.comics.domain.repository.ComicRepository

/**
 * Use case for loading a specific comic by number from the API.
 * 
 * Triggers an API fetch for the comic and updates the cache.
 * The updated data will automatically propagate through [ObserveComicByNumberUseCase].
 */
class LoadComicByNumberUseCase(
    private val repository: ComicRepository
) {
    /**
     * Executes the use case to load a comic by number.
     * 
     * @param comicNumber The comic number to load
     */
    suspend operator fun invoke(comicNumber: Int) {
        repository.loadComicByNumber(comicNumber)
    }
}

