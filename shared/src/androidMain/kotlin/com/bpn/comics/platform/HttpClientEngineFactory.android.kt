package com.bpn.comics.platform

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android

actual class HttpClientEngineFactory {
    actual fun createEngine(): HttpClientEngine {
        return Android.create()
    }
}

