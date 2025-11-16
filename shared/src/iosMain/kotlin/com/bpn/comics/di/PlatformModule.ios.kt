package com.bpn.comics.di

import com.bpn.comics.platform.DatabaseDriverFactory
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Platform-specific module for iOS.
 * Provides iOS-specific dependencies like DatabaseDriverFactory.
 */
fun platformModule(): Module {
    return module {
        // Database Driver Factory (no Context needed for iOS)
        single<DatabaseDriverFactory> {
            DatabaseDriverFactory()
        }
    }
}

