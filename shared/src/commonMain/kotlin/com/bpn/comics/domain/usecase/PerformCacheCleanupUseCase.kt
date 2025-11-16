package com.bpn.comics.domain.usecase

import com.bpn.comics.domain.repository.ComicRepository
import com.bpn.comics.util.CacheConfig

/**
 * Use case for performing automatic cache cleanup.
 * Clears old comics based on age and size limits, preserving favorites.
 */
class PerformCacheCleanupUseCase(
    private val repository: ComicRepository
) {
    /**
     * Perform automatic cache cleanup using default configuration.
     * @return Total number of comics deleted
     */
    suspend operator fun invoke(): Int {
        return repository.performAutomaticCacheCleanup(
            maxAgeDays = CacheConfig.MAX_CACHE_AGE_DAYS,
            maxNonFavoriteComics = CacheConfig.MAX_NON_FAVORITE_COMICS
        )
    }
}

