package com.bpn.comics.di

import com.bpn.comics.presentation.comicdetail.ComicDetailViewModel
import com.bpn.comics.presentation.comics.ComicsViewModel
import com.bpn.comics.presentation.favorites.FavoritesViewModel
import org.koin.dsl.module

val viewModelModule = module {
    factory { ComicsViewModel(get(), get(), get(), get(), get()) }
    factory { ComicDetailViewModel(get(), get(), get()) }
    factory { FavoritesViewModel(get(), get()) }
}

