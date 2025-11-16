package com.bpn.comics.di

import com.bpn.comics.presentation.ComicDetailViewModel
import com.bpn.comics.presentation.ComicsViewModel
import com.bpn.comics.presentation.FavoritesEventManager
import org.koin.dsl.module

val viewModelModule = module {
    // Favorites Event Manager (singleton for cross-platform event sharing)
    single { FavoritesEventManager() }
    
    // ViewModels
    factory { ComicsViewModel(get(), get(), get(), get(), get()) }
    factory { ComicDetailViewModel(get(), get(), get()) }
}

