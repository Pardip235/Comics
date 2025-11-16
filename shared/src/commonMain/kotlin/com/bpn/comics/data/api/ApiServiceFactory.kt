package com.bpn.comics.data.api

import com.bpn.comics.data.network.HttpClientFactory
import com.bpn.comics.platform.HttpClientEngineFactory

/**
 * Factory for creating API service instances.
 * Allows easy switching between real and mock implementations.
 */
object ApiServiceFactory {
    fun createApiService(
        engineFactory: HttpClientEngineFactory = HttpClientEngineFactory(),
        clientFactory: HttpClientFactory = HttpClientFactory()
    ): XkcdApiServiceInterface {
        val engine = engineFactory.createEngine()
        val httpClient = clientFactory.createHttpClient(engine)
        return XkcdApiService(httpClient)
    }
}

