package com.bpn.comics.domain.usecase

import com.bpn.comics.data.model.Comic
import com.bpn.comics.domain.repository.ComicRepository

/**
 * Use case for getting all favorite comics.
 */
class GetFavoriteComicsUseCase(
    private val repository: ComicRepository
) {
    suspend operator fun invoke(): List<Comic> {
        return repository.getFavoriteComics()
    }
}

