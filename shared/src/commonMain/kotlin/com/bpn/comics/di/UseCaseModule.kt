package com.bpn.comics.di

import com.bpn.comics.domain.usecase.LoadComicByNumberUseCase
import com.bpn.comics.domain.usecase.LoadInitialComicsUseCase
import com.bpn.comics.domain.usecase.LoadMoreComicsUseCase
import com.bpn.comics.domain.usecase.ObserveComicByNumberUseCase
import com.bpn.comics.domain.usecase.ObserveComicsUseCase
import com.bpn.comics.domain.usecase.ObserveFavoriteComicsUseCase
import com.bpn.comics.domain.usecase.PerformCacheCleanupUseCase
import com.bpn.comics.domain.usecase.ToggleFavoriteUseCase
import org.koin.dsl.module

val useCaseModule = module {
    factory { ObserveComicsUseCase(get()) }
    factory { ObserveComicByNumberUseCase(get()) }
    factory { ObserveFavoriteComicsUseCase(get()) }
    factory { LoadInitialComicsUseCase(get()) }
    factory { LoadComicByNumberUseCase(get()) }
    factory { LoadMoreComicsUseCase(get()) }
    factory { ToggleFavoriteUseCase(get()) }
    factory { PerformCacheCleanupUseCase(get()) }
}

