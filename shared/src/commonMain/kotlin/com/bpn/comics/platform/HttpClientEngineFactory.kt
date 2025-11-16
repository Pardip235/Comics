package com.bpn.comics.platform

import io.ktor.client.engine.HttpClientEngine

expect class HttpClientEngineFactory() {
    fun createEngine(): HttpClientEngine
}
