package com.bpn.comics.di

import com.bpn.comics.domain.repository.ComicRepository
import com.bpn.comics.domain.usecase.GetComicDetailUseCase
import com.bpn.comics.domain.usecase.GetFavoriteComicsUseCase
import com.bpn.comics.domain.usecase.GetInitialComicsUseCase
import com.bpn.comics.domain.usecase.HasMoreComicsUseCase
import com.bpn.comics.domain.usecase.IsFavoriteUseCase
import com.bpn.comics.domain.usecase.LoadMoreComicsUseCase
import com.bpn.comics.domain.usecase.PerformCacheCleanupUseCase
import com.bpn.comics.domain.usecase.ToggleFavoriteUseCase
import org.koin.dsl.module

val useCaseModule = module {
    // Use Cases
    factory { GetInitialComicsUseCase(get()) }
    factory { LoadMoreComicsUseCase(get()) }
    factory { HasMoreComicsUseCase(get()) }
    factory { GetComicDetailUseCase(get()) }
    factory { GetFavoriteComicsUseCase(get()) }
    factory { ToggleFavoriteUseCase(get()) }
    factory { IsFavoriteUseCase(get()) }
    factory { PerformCacheCleanupUseCase(get()) }
}

