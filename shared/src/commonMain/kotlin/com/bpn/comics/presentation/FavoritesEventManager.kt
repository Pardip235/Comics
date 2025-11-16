package com.bpn.comics.presentation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Event manager for favorites changes.
 * Uses SharedFlow to notify observers across platforms (Android/iOS).
 * This is more suitable for KMM than callbacks.
 */
class FavoritesEventManager {
    private val _favoritesChanged = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 0
    )
    
    /**
     * Flow that emits when favorites change.
     * Both Android and iOS can collect from this flow.
     */
    val favoritesChanged: SharedFlow<Unit> = _favoritesChanged.asSharedFlow()
    
    /**
     * Notify that favorites have changed.
     * This should be called after a successful favorite toggle.
     * Uses tryEmit for non-blocking emission (can be called from any context).
     */
    fun notifyFavoritesChanged() {
        _favoritesChanged.tryEmit(Unit)
    }
}

