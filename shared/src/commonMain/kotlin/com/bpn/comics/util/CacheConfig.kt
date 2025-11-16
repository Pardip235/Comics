package com.bpn.comics.util

/**
 * Configuration constants for cache management.
 * Defines cache expiration and size limits.
 */
object CacheConfig {
    /**
     * Maximum age of cached comics in days.
     * Comics older than this will be automatically cleared (favorites are preserved).
     */
    const val MAX_CACHE_AGE_DAYS = 30L
    
    /**
     * Maximum number of non-favorite comics to keep in cache.
     * When exceeded, oldest non-favorite comics will be removed.
     * Favorites are always preserved regardless of this limit.
     */
    const val MAX_NON_FAVORITE_COMICS = 500
}

