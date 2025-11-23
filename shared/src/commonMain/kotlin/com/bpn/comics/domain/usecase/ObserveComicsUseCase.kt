package com.bpn.comics.domain.usecase

import com.bpn.comics.domain.model.Comic
import com.bpn.comics.domain.repository.ComicRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for observing all comics from the repository.
 */
class ObserveComicsUseCase(
    private val repository: ComicRepository
) {
    operator fun invoke(): Flow<List<Comic>> = repository.observeComics()
}

