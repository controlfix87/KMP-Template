package com.cmptemplate.feature.about.report

import com.cmptemplate.core.model.AppInfo
import com.cmptemplate.core.network.createHttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

private fun engine(handler: MockRequestHandler) = object : HttpClientEngineFactory<MockEngineConfig> {
    override fun create(block: MockEngineConfig.() -> Unit): HttpClientEngine =
        MockEngine(MockEngineConfig().apply(block).apply { addHandler(handler) })
}

private val app = AppInfo("com.example.app", "Example", "1.2.3", "42", "play/release", "android")

private class FakeDiagnostics(var crash: CrashRecord? = null, val log: String? = "user me@example.com logged in") : Diagnostics {
    var cleared = false
    override fun device() = DeviceInfo(manufacturer = "Acme", model = "P1", os = "Android 17", api = "37", type = "phone", language = "en")
    override suspend fun recentLog() = log
    override fun lastCrash() = crash
    override fun clearCrash() { cleared = true; crash = null }
}

class HttpReportSenderTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test fun `posts the contract with the app id header, redacted log, and clears a sent crash`() = runTest {
        var body = ""
        val client = createHttpClient("https://reports.test/", engine = engine { request ->
            assertEquals("https://reports.test/v1/reports", request.url.toString())
            assertEquals("com.example.app", request.headers["x-report-app"])
            body = request.body.toByteArray().decodeToString()
            respond("""{"id":"T-1"}""", HttpStatusCode.Created, headersOf(HttpHeaders.ContentType, "application/json"))
        })
        val diagnostics = FakeDiagnostics(crash = CrashRecord(1L, "boom token=abc123"))
        try {
            val result = HttpReportSender(client, app, diagnostics).send(ReportDraft(ReportKind.BUG, "Title", "Message", "me@example.com"))
            assertEquals(ReportResult.Sent("T-1"), result)
        } finally { client.close() }
        val sent = json.parseToJsonElement(body).jsonObject
        assertEquals("bug", sent["kind"]!!.jsonPrimitive.content)
        assertEquals("com.example.app", sent["app"]!!.jsonObject["id"]!!.jsonPrimitive.content)
        assertEquals("Acme", sent["device"]!!.jsonObject["manufacturer"]!!.jsonPrimitive.content)
        // The contact is the user's own reply address and is sent as typed; logs are redacted.
        assertEquals("me@example.com", sent["contact"]!!.jsonPrimitive.content)
        assertFalse("me@example.com" in sent["log"]!!.jsonPrimitive.content)
        assertFalse("abc123" in sent["crash"]!!.jsonPrimitive.content)
        assertTrue(diagnostics.cleared)
    }

    @Test fun `without logs no log or crash leaves the device`() = runTest {
        var body = ""
        val client = createHttpClient("https://reports.test/", engine = engine { request ->
            body = request.body.toByteArray().decodeToString()
            respond("""{"id":"T-2"}""", HttpStatusCode.Created, headersOf(HttpHeaders.ContentType, "application/json"))
        })
        val diagnostics = FakeDiagnostics(crash = CrashRecord(1L, "trace"))
        try {
            HttpReportSender(client, app, diagnostics).send(ReportDraft(ReportKind.SUGGESTION, "T", "M", includeLogs = false))
        } finally { client.close() }
        val sent = json.parseToJsonElement(body).jsonObject
        assertNull(sent["log"]?.takeIf { it.toString() != "null" })
        assertNull(sent["crash"]?.takeIf { it.toString() != "null" })
        assertFalse(diagnostics.cleared)
    }

    @Test fun `maps rate limit, rejection, server and network failures`() = runTest {
        for ((status, expected) in listOf(
            HttpStatusCode.TooManyRequests to ReportResult.RateLimited,
            HttpStatusCode.BadRequest to ReportResult.Rejected,
            HttpStatusCode.PayloadTooLarge to ReportResult.Rejected,
            HttpStatusCode.BadGateway to ReportResult.Offline,
        )) {
            val client = createHttpClient("https://reports.test/", engine = engine { respond("{}", status) })
            try { assertEquals(expected, HttpReportSender(client, app, FakeDiagnostics()).send(ReportDraft(ReportKind.BUG, "T", "M"))) }
            finally { client.close() }
        }
        val offline = createHttpClient("https://reports.test/", engine = engine { throw IllegalStateException("no route") })
        try { assertEquals(ReportResult.Offline, HttpReportSender(offline, app, FakeDiagnostics()).send(ReportDraft(ReportKind.BUG, "T", "M"))) }
        finally { offline.close() }
    }

    @Test fun `cancellation propagates`() = runTest {
        val client = createHttpClient("https://reports.test/", engine = engine { throw CancellationException("stop") })
        try { assertFailsWith<CancellationException> { HttpReportSender(client, app, FakeDiagnostics()).send(ReportDraft(ReportKind.BUG, "T", "M")) } }
        finally { client.close() }
    }
}
