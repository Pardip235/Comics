package com.bpn.comics.domain.usecase

import com.bpn.comics.domain.model.Comic
import com.bpn.comics.domain.repository.ComicRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for observing favorite comics from the repository.
 */
class ObserveFavoriteComicsUseCase(
    private val repository: ComicRepository
) {
    /**
     * Executes the use case to observe favorite comics.
     * 
     * @return A [Flow] that emits the current list of favorite comics
     */
    operator fun invoke(): Flow<List<Comic>> = repository.observeFavoriteComics()
}

