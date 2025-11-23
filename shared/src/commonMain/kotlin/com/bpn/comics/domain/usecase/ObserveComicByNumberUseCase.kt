package com.bpn.comics.domain.usecase

import com.bpn.comics.domain.model.Comic
import com.bpn.comics.domain.repository.ComicRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for observing a specific comic by number from the repository.
 *
 */
class ObserveComicByNumberUseCase(
    private val repository: ComicRepository
) {
    /**
     * Executes the use case to observe a comic by number.
     * 
     * @param number The comic number to observe
     * @return A [Flow] that emits the comic if found, or null if not in database
     */
    operator fun invoke(number: Int): Flow<Comic?> = repository.observeComicByNumber(number)
}

