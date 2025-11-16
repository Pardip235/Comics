package com.bpn.comics.data.repository

import com.bpn.comics.data.api.ApiServiceFactory
import com.bpn.comics.data.api.XkcdApiServiceInterface
import com.bpn.comics.database.ComicsDatabase
import com.bpn.comics.domain.repository.ComicRepository
import com.bpn.comics.platform.DatabaseDriverFactory

/**
 * Factory for creating ComicRepository instances.
 * Handles the setup of dependencies (API service and database).
 */
object ComicRepositoryFactory {
    /**
     * Create a ComicRepository instance with all dependencies.
     * 
     * @param databaseDriverFactory Platform-specific database driver factory
     * @return Configured ComicRepository instance
     */
    fun createRepository(
        databaseDriverFactory: DatabaseDriverFactory
    ): ComicRepository {
        // Create API service
        val apiService: XkcdApiServiceInterface = ApiServiceFactory.createApiService()
        
        // Create database instance
        val database = ComicsDatabase(databaseDriverFactory.createDriver())
        
        // Create and return repository
        return ComicRepositoryImpl(
            apiService = apiService,
            database = database
        )
    }
}

