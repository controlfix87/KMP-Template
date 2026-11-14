package com.cmptemplate.feature.about.report

import com.cmptemplate.core.model.AppInfo
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlin.coroutines.cancellation.CancellationException

/**
 * Sends to the shared ControlFix reports service. [client] is created with
 * `createHttpClient(ControlFixLinks.REPORTS_BASE)` (HTTPS only, expectSuccess, timeouts).
 * The `x-report-app` header must equal `app.id`; the ticket lands under that id.
 */
class HttpReportSender(
    private val client: HttpClient,
    private val app: AppInfo,
    private val diagnostics: Diagnostics,
    private val extra: () -> Map<String, String> = { emptyMap() },
) : ReportSender {

    override suspend fun send(draft: ReportDraft): ReportResult {
        val crash = if (draft.includeLogs || draft.kind == ReportKind.CRASH) diagnostics.lastCrash() else null
        val log = if (draft.includeLogs) diagnostics.recentLog() else null
        val payload = ReportPayload(
            kind = draft.kind.wire,
            title = draft.title.trim().take(ReportSender.TITLE_MAX),
            message = draft.message.trim().take(ReportSender.MESSAGE_MAX),
            contact = draft.contact.trim().take(ReportSender.CONTACT_MAX),
            app = AppPayload(app.id, app.name, app.version, app.code, app.flavor, app.platform),
            device = diagnostics.device(),
            extra = extra().entries.take(20).associate { (k, v) -> k.take(40) to Redactor.redact(v).take(500) },
            crash = crash?.trace?.let { Redactor.redact(it).take(ReportSender.CRASH_MAX) },
            log = log?.let { Redactor.redact(it).takeLast(ReportSender.LOG_MAX) },
        )
        return try {
            val response = client.post("v1/reports") {
                contentType(ContentType.Application.Json)
                header("x-report-app", app.id)
                setBody(payload)
            }
            // 2xx: the ticket exists. A body we cannot read must not turn that into a failure.
            val id = try { response.body<ReportAnswer>().id } catch (e: CancellationException) { throw e } catch (e: Exception) { "" }
            if (crash != null) diagnostics.clearCrash()
            ReportResult.Sent(id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.TooManyRequests) ReportResult.RateLimited else ReportResult.Rejected
        } catch (e: ResponseException) {
            ReportResult.Offline
        } catch (e: Exception) {
            ReportResult.Offline
        }
    }
}
