package com.bpn.comics.di

import org.koin.core.module.Module

/**
 * Combines all app modules for dependency injection.
 */
fun appModule(): List<Module> {
    return listOf(
        networkModule,
        databaseModule,
        repositoryModule,
        useCaseModule
    )
}

