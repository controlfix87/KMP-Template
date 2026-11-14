package com.cmptemplate.feature.about.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cmptemplate.core.common.DefaultDispatcherProvider
import com.cmptemplate.core.common.DispatcherProvider
import com.cmptemplate.core.model.AppInfo
import com.cmptemplate.feature.about.report.Diagnostics
import com.cmptemplate.feature.about.report.ReportDraft
import com.cmptemplate.feature.about.report.ReportKind
import com.cmptemplate.feature.about.report.ReportResult
import com.cmptemplate.feature.about.report.ReportSender
import com.cmptemplate.feature.about.report.SupportMail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class SendFailure { OFFLINE, RATE_LIMITED, REJECTED }

sealed interface ReportStatus {
    data object Editing : ReportStatus
    data object Sending : ReportStatus
    data class Sent(val ticketId: String) : ReportStatus
    data class Failed(val reason: SendFailure) : ReportStatus
}

data class ReportState(
    val kind: ReportKind = ReportKind.BUG,
    val title: String = "",
    val message: String = "",
    val contact: String = "",
    val includeLogs: Boolean = true,
    /** When the app last crashed (epoch ms), if that crash was not reported yet. */
    val crashAt: Long? = null,
    val status: ReportStatus = ReportStatus.Editing,
) {
    val canSend: Boolean
        get() = status != ReportStatus.Sending && status !is ReportStatus.Sent &&
            (kind == ReportKind.CRASH || (title.isNotBlank() && message.isNotBlank()))
}

sealed interface ReportAction {
    data class Kind(val kind: ReportKind) : ReportAction
    data class Title(val value: String) : ReportAction
    data class Message(val value: String) : ReportAction
    data class Contact(val value: String) : ReportAction
    data class IncludeLogs(val on: Boolean) : ReportAction
    data object Send : ReportAction
}

/**
 * Drafts survive rotation and process death (SavedStateHandle); the send state does not, on
 * purpose: a report in flight when the process dies is simply not sent, and the user still has
 * the draft to try again.
 */
class ReportViewModel(
    initialKind: ReportKind,
    private val sender: ReportSender,
    private val diagnostics: Diagnostics,
    private val app: AppInfo,
    private val saved: SavedStateHandle,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider,
) : ViewModel() {
    private val kind = saved.getStateFlow(KEY_KIND, initialKind.wire)
    private val title = saved.getStateFlow(KEY_TITLE, "")
    private val message = saved.getStateFlow(KEY_MESSAGE, "")
    private val contact = saved.getStateFlow(KEY_CONTACT, "")
    private val logs = saved.getStateFlow(KEY_LOGS, true)
    private val crashAt = MutableStateFlow<Long?>(null)
    private val status = MutableStateFlow<ReportStatus>(ReportStatus.Editing)

    val state: StateFlow<ReportState> = combine(
        combine(kind, title, message, contact, logs) { k, t, m, c, l -> ReportState(ReportKind.ofWire(k), t, m, c, l) },
        crashAt,
        status,
    ) { fields, crash, s -> fields.copy(crashAt = crash, status = s) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReportState(kind = initialKind))

    init {
        // A disk read: off the main thread (StrictMode flags it on Android).
        viewModelScope.launch {
            crashAt.value = withContext(dispatchers.io) { diagnostics.lastCrash()?.atEpochMillis }
        }
    }

    fun onAction(action: ReportAction) {
        when (action) {
            is ReportAction.Kind -> saved[KEY_KIND] = action.kind.wire
            is ReportAction.Title -> saved[KEY_TITLE] = action.value.take(ReportSender.TITLE_MAX)
            is ReportAction.Message -> saved[KEY_MESSAGE] = action.value.take(ReportSender.MESSAGE_MAX)
            is ReportAction.Contact -> saved[KEY_CONTACT] = action.value.take(ReportSender.CONTACT_MAX)
            is ReportAction.IncludeLogs -> saved[KEY_LOGS] = action.on
            ReportAction.Send -> send()
        }
        if (action !is ReportAction.Send && status.value is ReportStatus.Failed) status.value = ReportStatus.Editing
    }

    /** The `mailto:` fallback for the current draft. */
    fun mailUri(): String = SupportMail.uri(draftFromSaved(), app, diagnostics.device())

    private fun send() {
        if (!currentFields().canSend) return
        status.value = ReportStatus.Sending
        viewModelScope.launch {
            val result = withContext(dispatchers.io) { sender.send(draftFromSaved()) }
            status.value = when (result) {
                is ReportResult.Sent -> ReportStatus.Sent(result.ticketId).also { crashAt.value = null }
                ReportResult.RateLimited -> ReportStatus.Failed(SendFailure.RATE_LIMITED)
                ReportResult.Offline -> ReportStatus.Failed(SendFailure.OFFLINE)
                ReportResult.Rejected -> ReportStatus.Failed(SendFailure.REJECTED)
            }
        }
    }

    // Read from SavedStateHandle directly: `state` is only live while the screen collects it.
    private fun currentFields() = ReportState(
        ReportKind.ofWire(kind.value), title.value, message.value, contact.value, logs.value, crashAt.value, status.value,
    )

    private fun draftFromSaved(): ReportDraft {
        val s = currentFields()
        return ReportDraft(s.kind, s.title.ifBlank { if (s.kind == ReportKind.CRASH) "Crash" else "" }, s.message, s.contact, s.includeLogs)
    }

    private companion object {
        const val KEY_KIND = "report_kind"
        const val KEY_TITLE = "report_title"
        const val KEY_MESSAGE = "report_message"
        const val KEY_CONTACT = "report_contact"
        const val KEY_LOGS = "report_logs"
    }
}
