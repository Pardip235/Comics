package com.bpn.comics.di

import com.bpn.comics.domain.repository.ComicRepository
import com.bpn.comics.domain.usecase.GetComicDetailUseCase
import com.bpn.comics.domain.usecase.GetInitialComicsUseCase
import com.bpn.comics.domain.usecase.HasMoreComicsUseCase
import com.bpn.comics.domain.usecase.LoadMoreComicsUseCase
import org.koin.dsl.module

val useCaseModule = module {
    // Use Cases
    factory { GetInitialComicsUseCase(get()) }
    factory { LoadMoreComicsUseCase(get()) }
    factory { HasMoreComicsUseCase(get()) }
    factory { GetComicDetailUseCase(get()) }
}

