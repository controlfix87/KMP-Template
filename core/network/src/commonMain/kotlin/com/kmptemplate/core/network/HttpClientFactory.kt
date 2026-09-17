package com.kmptemplate.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** Relative endpoint paths resolve beneath [baseUrl], which must end with a slash. */
fun createHttpClient(
    baseUrl: String,
    enableLogging: Boolean = false,
    engine: HttpClientEngineFactory<*> = platformHttpClientEngine(),
): HttpClient {
    val parsed = Url(baseUrl)
    require(parsed.protocol == URLProtocol.HTTPS) { "API base URL must use HTTPS" }
    require(parsed.host.isNotBlank() && baseUrl.endsWith('/')) { "API base URL must have a host and trailing slash" }
    require(parsed.user == null && parsed.password == null && parsed.parameters.isEmpty() && parsed.fragment.isEmpty()) {
        "API base URL must not contain credentials, query parameters or a fragment"
    }
    return HttpClient(engine) {
        expectSuccess = true
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 15_000
            socketTimeoutMillis = 30_000
        }
        if (enableLogging) {
            install(Logging) {
                level = LogLevel.HEADERS
                sanitizeHeader { it.equals(HttpHeaders.Authorization, true) || it.equals(HttpHeaders.Cookie, true) || it.equals(HttpHeaders.SetCookie, true) }
            }
        }
        defaultRequest { url(baseUrl) }
    }
}

internal expect fun platformHttpClientEngine(): HttpClientEngineFactory<*>
