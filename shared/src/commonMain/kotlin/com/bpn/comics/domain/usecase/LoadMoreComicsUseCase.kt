package com.bpn.comics.domain.usecase

import com.bpn.comics.domain.repository.ComicRepository

/**
 * Use case for loading more comics for pagination.
 * 
 * Triggers an API fetch for older comics and updates the cache.
 * The updated data will automatically propagate through [ObserveComicsUseCase].
 */
class LoadMoreComicsUseCase(
    private val repository: ComicRepository
) {
    /**
     * Executes the use case to load more comics.
     * 
     * @param oldestComicNumber The number of the oldest comic currently displayed
     * @param count The number of comics to load
     */
    suspend operator fun invoke(oldestComicNumber: Int, count: Int) {
        repository.loadMoreComics(oldestComicNumber, count)
    }
}

