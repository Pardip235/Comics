package com.bpn.comics.di

import android.content.Context
import com.bpn.comics.platform.DatabaseDriverFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Platform-specific module for Android.
 * Provides Android-specific dependencies like DatabaseDriverFactory with Context.
 */
fun platformModule(): Module {
    return module {
        // Database Driver Factory (requires Android Context)
        single<DatabaseDriverFactory> {
            val context: Context = androidContext()
            DatabaseDriverFactory(context)
        }
    }
}

