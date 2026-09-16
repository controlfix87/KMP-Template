package com.kmptemplate.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * The one place an [HttpClient] gets built. Every module that talks to the
 * network takes an [HttpClient] as a constructor parameter (wired via Koin,
 * see :androidApp's DI module) rather than building its own -- one client, one
 * connection pool, one place to change timeouts/logging.
 *
 * [platformHttpClientEngine] is the only platform-specific piece: enabling an
 * Apple target later needs an `actual` in `iosMain` backed by Darwin (Ktor's
 * `io.ktor:ktor-client-darwin`) alongside the `androidMain` one already here.
 */
fun createHttpClient(
    baseUrl: String,
    enableLogging: Boolean,
    json: Json = Json { ignoreUnknownKeys = true; encodeDefaults = true },
): HttpClient = HttpClient(platformHttpClientEngine()) {
    install(ContentNegotiation) { json(json) }
    if (enableLogging) {
        install(Logging) { level = LogLevel.INFO }
    }
    defaultRequest {
        url(baseUrl)
    }
}

internal expect fun platformHttpClientEngine(): HttpClientEngineFactory<*>
