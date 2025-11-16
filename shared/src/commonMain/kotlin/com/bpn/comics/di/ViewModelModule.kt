package com.bpn.comics.di

import com.bpn.comics.presentation.ComicsViewModel
import org.koin.dsl.module

val viewModelModule = module {
    // ViewModels
    factory { ComicsViewModel(get(), get(), get()) }
}

