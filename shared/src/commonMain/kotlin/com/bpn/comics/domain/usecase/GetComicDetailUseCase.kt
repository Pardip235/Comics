package com.bpn.comics.domain.usecase

import com.bpn.comics.data.model.Comic
import com.bpn.comics.domain.repository.ComicRepository

/**
 * Use case for getting comic detail by number.
 * Encapsulates the business logic for fetching a specific comic.
 */
class GetComicDetailUseCase(
    private val repository: ComicRepository
) {
    /**
     * Execute the use case to get comic detail.
     * 
     * @param comicNumber The number of the comic to fetch
     * @return Comic object
     */
    suspend operator fun invoke(comicNumber: Int): Comic {
        return repository.getComicByNumber(comicNumber)
    }
}

