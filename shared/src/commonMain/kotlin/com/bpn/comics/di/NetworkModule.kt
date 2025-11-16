package com.bpn.comics.di

import com.bpn.comics.data.api.XkcdApiService
import com.bpn.comics.data.api.XkcdApiServiceInterface
import com.bpn.comics.data.network.DefaultNetworkConfig
import com.bpn.comics.data.network.HttpClientFactory
import com.bpn.comics.data.network.NetworkConfig
import com.bpn.comics.platform.HttpClientEngineFactory
import io.ktor.client.HttpClient
import org.koin.dsl.module

val networkModule = module {
    // Network Config
    single<NetworkConfig> { DefaultNetworkConfig() }
    
    // HTTP Client Engine Factory
    single { HttpClientEngineFactory() }
    
    // HTTP Client Factory
    single { HttpClientFactory(get()) }
    
    // HTTP Client
    single<HttpClient> {
        val engineFactory: HttpClientEngineFactory = get()
        val clientFactory: HttpClientFactory = get()
        clientFactory.createHttpClient(engineFactory.createEngine())
    }
    
    // API Service
    single<XkcdApiServiceInterface> { XkcdApiService( httpClient = get()) }
}

