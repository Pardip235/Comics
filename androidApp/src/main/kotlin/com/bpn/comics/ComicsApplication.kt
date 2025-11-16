package com.bpn.comics

import android.app.Application
import com.bpn.comics.di.appModule
import com.bpn.comics.di.platformModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ComicsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Koin with Android context and all modules
        startKoin {
            androidContext(this@ComicsApplication)
            modules(
                *appModule().toTypedArray(),
                platformModule()
            )
        }
    }
}

