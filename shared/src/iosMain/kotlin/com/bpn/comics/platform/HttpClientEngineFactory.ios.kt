package com.bpn.comics.platform

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

actual class HttpClientEngineFactory {
    actual fun createEngine(): HttpClientEngine {
        return Darwin.create()
    }
}

