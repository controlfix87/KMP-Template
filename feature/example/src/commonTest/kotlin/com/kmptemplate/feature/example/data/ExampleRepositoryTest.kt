package com.kmptemplate.feature.example.data

import com.kmptemplate.core.common.*
import com.kmptemplate.core.network.createHttpClient
import io.ktor.client.engine.mock.*
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.HttpClientEngine
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.first
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.*

private fun mockEngine(handler: MockRequestHandler) = object : HttpClientEngineFactory<MockEngineConfig> {
    override fun create(block: MockEngineConfig.() -> Unit): HttpClientEngine =
        MockEngine(MockEngineConfig().apply(block).apply { addHandler(handler) })
}

class ExampleRepositoryTest {
    @Test fun `maps successful response and uses base path`() = runTest {
        val engine = mockEngine { request ->
            assertEquals("https://example.test/api/example/items", request.url.toString())
            respond("""[{"id":"42","title":"Answer"}]""", headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val client = createHttpClient("https://example.test/api/", engine = engine)
        try {
            val repo = ExampleRepositoryImpl(client)
            assertTrue(repo.refresh().isSuccess)
            assertEquals("42", repo.observeItems().first().single().id)
        } finally { client.close() }
    }
    @Test fun `HTTP and malformed payload failures become typed results`() = runTest {
        for ((status, body, expected) in listOf(
            Triple(HttpStatusCode.Unauthorized, "{}", DataError.Remote.UNAUTHORIZED),
            Triple(HttpStatusCode.NotFound, "{}", DataError.Remote.NOT_FOUND),
            Triple(HttpStatusCode.InternalServerError, "{}", DataError.Remote.SERVER_ERROR),
            Triple(HttpStatusCode.OK, "not json", DataError.Remote.SERIALIZATION),
        )) {
            val engine = mockEngine { respond(body, status, headersOf(HttpHeaders.ContentType, "application/json")) }
            val client = createHttpClient("https://example.test/", engine = engine)
            try { assertEquals(expected, ExampleRepositoryImpl(client).refresh().errorOrNull()) }
            finally { client.close() }
        }
    }
    @Test fun `cancellation propagates rather than becoming an error`() = runTest {
        val engine = mockEngine { throw CancellationException("test cancellation") }
        val client = createHttpClient("https://example.test/", engine = engine)
        try { assertFailsWith<CancellationException> { ExampleRepositoryImpl(client).refresh() } }
        finally { client.close() }
    }
    @Test fun `cleartext and invalid base path are rejected`() {
        assertFailsWith<IllegalArgumentException> { createHttpClient("http://example.test/") }
        assertFailsWith<IllegalArgumentException> { createHttpClient("https://example.test/api") }
    }
}
