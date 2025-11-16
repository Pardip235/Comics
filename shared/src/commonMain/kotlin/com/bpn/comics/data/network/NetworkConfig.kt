package com.bpn.comics.data.network

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

interface NetworkConfig {
    val baseUrl: String
    val timeoutMillis: Long
    val enableLogging: Boolean
}

class DefaultNetworkConfig : NetworkConfig {
    override val baseUrl: String = "https://xkcd.com"
    override val timeoutMillis: Long = 30_000
    override val enableLogging: Boolean = true
}

class HttpClientFactory(
    private val config: NetworkConfig = DefaultNetworkConfig()
) {
    
    fun createHttpClient(engine: HttpClientEngine): HttpClient {
        return HttpClient(engine) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    coerceInputValues = true
                })
            }
            
            install(HttpTimeout) {
                requestTimeoutMillis = config.timeoutMillis
                connectTimeoutMillis = config.timeoutMillis
                socketTimeoutMillis = config.timeoutMillis
            }
            
            if (config.enableLogging) {
                install(Logging) {
                    level = LogLevel.INFO
                    logger = object : Logger {
                        override fun log(message: String) {
                            println("🌐 Network: $message")
                        }
                    }
                }
            }
            
            install(DefaultRequest) {
                url(config.baseUrl)
                headers {
                    append("User-Agent", "xkcd-comic-app/1.0")
                    append("Accept", "application/json")
                }
            }
        }
    }
}
