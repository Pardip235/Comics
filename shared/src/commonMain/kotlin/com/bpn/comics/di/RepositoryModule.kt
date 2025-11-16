package com.bpn.comics.di

import com.bpn.comics.data.api.XkcdApiServiceInterface
import com.bpn.comics.data.repository.ComicRepositoryImpl
import com.bpn.comics.database.ComicsDatabase
import com.bpn.comics.domain.repository.ComicRepository
import org.koin.dsl.module

val repositoryModule = module {
    // Repository
    single<ComicRepository> { ComicRepositoryImpl(apiService = get(), database = get()) }
}

