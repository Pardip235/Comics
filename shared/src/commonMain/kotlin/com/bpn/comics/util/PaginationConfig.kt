package com.bpn.comics.util

/**
 * Configuration constants for pagination in the comics list.
 * Centralizes pagination-related values for easy maintenance and testing.
 */
object PaginationConfig {
    /**
     * Number of comics to load initially when the list is first displayed.
     */
    const val INITIAL_LOAD_COUNT = 10

    /**
     * Number of comics to load when user scrolls and triggers pagination.
     */
    const val PAGINATION_LOAD_COUNT = 10

}

