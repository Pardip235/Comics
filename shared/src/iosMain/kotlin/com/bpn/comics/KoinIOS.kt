package com.bpn.comics

import com.bpn.comics.di.appModule
import com.bpn.comics.di.platformModule
import com.bpn.comics.presentation.comicdetail.ComicDetailViewModel
import com.bpn.comics.presentation.comics.ComicsViewModel
import com.bpn.comics.presentation.favorites.FavoritesViewModel
import com.bpn.comics.util.initializeLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.Koin
import org.koin.core.context.startKoin

/**
 * Simple Koin initialization for iOS
 */
object KoinIOS {
    val shared = KoinIOS
    
    private var koinInstance: Koin? = null
    private val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun doInitKoinIOS() {
        // Initialize Napier logging (KMM-friendly logging)
        // This will output to Xcode console via NSLog
        initializeLogging()
        
        val koinApplication = startKoin {
            modules(appModule() + platformModule())
        }
        koinInstance = koinApplication.koin
    }

    /**
     * Get ComicsViewModel instance
     */
    fun getComicsViewModel(): ComicsViewModel {
        return koinInstance?.get() ?: throw IllegalStateException(
            "Koin not initialized. Call doInitKoinIOS() first."
        )
    }

    /**
     * Get fresh ComicDetailViewModel instance (not singleton)
     */
    fun getComicDetailViewModel(): ComicDetailViewModel {
        return koinInstance?.get() ?: throw IllegalStateException(
            "Koin not initialized. Call doInitKoinIOS() first."
        )
    }

    /**
     * Get FavoritesViewModel instance
     */
    fun getFavoritesViewModel(): FavoritesViewModel {
        return koinInstance?.get() ?: throw IllegalStateException(
            "Koin not initialized. Call doInitKoinIOS() first."
        )
    }

    /**
     * Get CoroutineScope for StateFlow observation
     */
    fun getScope(): CoroutineScope = mainScope
}

