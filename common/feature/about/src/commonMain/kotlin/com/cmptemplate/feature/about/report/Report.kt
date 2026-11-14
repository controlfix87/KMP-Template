package com.cmptemplate.feature.about.report

import kotlinx.serialization.Serializable

enum class ReportKind(val wire: String) {
    CRASH("crash"), BUG("bug"), SUGGESTION("suggestion");

    companion object {
        fun ofWire(wire: String?): ReportKind = entries.firstOrNull { it.wire == wire } ?: BUG
    }
}

data class ReportDraft(
    val kind: ReportKind,
    val title: String,
    val message: String,
    /** Optional e-mail for a reply; sent only when the user typed one. */
    val contact: String = "",
    /** Device details always go; this adds the redacted recent log and the last crash. */
    val includeLogs: Boolean = true,
)

sealed interface ReportResult {
    data class Sent(val ticketId: String) : ReportResult
    /** The service allows 10 reports per client per hour. */
    data object RateLimited : ReportResult
    /** No connection, timeout, or the service is down: offer the e-mail fallback. */
    data object Offline : ReportResult
    /** The service refused the report (bad fields, too large). Also offers the e-mail fallback. */
    data object Rejected : ReportResult
}

interface ReportSender {
    suspend fun send(draft: ReportDraft): ReportResult

    companion object {
        const val TITLE_MAX = 120
        const val MESSAGE_MAX = 4000
        const val CONTACT_MAX = 120
        const val LOG_MAX = 512 * 1024
        const val CRASH_MAX = 64 * 1024
    }
}

/** Wire format of `POST /v1/reports`; see AgentsHub tools/reports/README.md. */
@Serializable
internal data class ReportPayload(
    val kind: String,
    val title: String,
    val message: String,
    val contact: String,
    val app: AppPayload,
    val device: DeviceInfo,
    val extra: Map<String, String> = emptyMap(),
    val crash: String? = null,
    val log: String? = null,
)

@Serializable
internal data class AppPayload(
    val id: String,
    val name: String,
    val version: String,
    val code: String,
    val flavor: String,
    val platform: String,
)

@Serializable
data class DeviceInfo(
    val manufacturer: String = "",
    val model: String = "",
    val device: String = "",
    val os: String = "",
    val api: String = "",
    /** "phone", "tablet" or "desktop". */
    val type: String = "",
    val language: String = "",
)

@Serializable
internal data class ReportAnswer(val id: String = "")
