package com.cmptemplate.feature.about.report

import com.cmptemplate.core.model.AppInfo
import com.cmptemplate.feature.about.ControlFixLinks
import io.ktor.http.encodeURLParameter

/**
 * The e-mail fallback when the reports service cannot be reached: a `mailto:` to support with the
 * subject, message and device details (no logs: a mail link cannot carry an attachment, and the
 * user sees and sends it from their own mail app). Kept under ~1800 characters so every mail app
 * and OS accepts the link.
 */
object SupportMail {
    private const val MAX_BODY = 1500

    fun uri(draft: ReportDraft, app: AppInfo, device: DeviceInfo, address: String = ControlFixLinks.SUPPORT_EMAIL): String {
        val label = when (draft.kind) {
            ReportKind.CRASH -> "Crash"
            ReportKind.BUG -> "Bug"
            ReportKind.SUGGESTION -> "Suggestion"
        }
        val subject = "[${app.name}] $label: ${draft.title.trim().ifBlank { label }}".take(160)
        val details = buildString {
            append("\n\n-- \n")
            append("App: ${app.name} ${app.version} (${app.code}) ${app.id} ${app.flavor}\n")
            append("Device: ${listOf(device.manufacturer, device.model, device.device).filter { it.isNotBlank() }.joinToString(" ")}\n")
            append("Type: ${device.type}\n")
            append("OS: ${device.os}${if (device.api.isNotBlank()) " (API ${device.api})" else ""}\n")
            append("Language: ${device.language}\n")
        }
        val body = Redactor.redact(draft.message.trim()).take(MAX_BODY - details.length) + details
        return "mailto:$address?subject=${subject.encodeURLParameter()}&body=${body.encodeURLParameter()}"
    }
}
