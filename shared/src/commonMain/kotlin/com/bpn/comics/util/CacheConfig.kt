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
    
    /**
     * Cache freshness TTL in seconds.
     * Data older than this will be considered stale and refreshed in background.
     * Default: 1 hour (3600 seconds)
     */
    const val CACHE_FRESHNESS_TTL_SECONDS = 3600L
    
    /**
     * Cache freshness TTL for individual comics in seconds.
     * Individual comic details older than this will be considered stale.
     * Default: 24 hours (86400 seconds)
     */
    const val COMIC_DETAIL_CACHE_TTL_SECONDS = 86400L
}

