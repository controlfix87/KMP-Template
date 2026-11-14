package com.cmptemplate.feature.about.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cmptemplate.core.designsystem.components.ContentMaxWidth
import com.cmptemplate.core.designsystem.components.ScreenHeader
import com.cmptemplate.core.designsystem.generated.resources.Res
import com.cmptemplate.core.designsystem.generated.resources.common_back
import com.cmptemplate.core.designsystem.generated.resources.common_close
import com.cmptemplate.core.designsystem.generated.resources.report_body
import com.cmptemplate.core.designsystem.generated.resources.report_crash_attached
import com.cmptemplate.core.designsystem.generated.resources.report_email
import com.cmptemplate.core.designsystem.generated.resources.report_failed_offline
import com.cmptemplate.core.designsystem.generated.resources.report_failed_rate
import com.cmptemplate.core.designsystem.generated.resources.report_failed_rejected
import com.cmptemplate.core.designsystem.generated.resources.report_field_contact
import com.cmptemplate.core.designsystem.generated.resources.report_field_message
import com.cmptemplate.core.designsystem.generated.resources.report_field_title
import com.cmptemplate.core.designsystem.generated.resources.report_kind_bug
import com.cmptemplate.core.designsystem.generated.resources.report_kind_crash
import com.cmptemplate.core.designsystem.generated.resources.report_kind_suggestion
import com.cmptemplate.core.designsystem.generated.resources.report_logs
import com.cmptemplate.core.designsystem.generated.resources.report_logs_hint
import com.cmptemplate.core.designsystem.generated.resources.report_no_mail
import com.cmptemplate.core.designsystem.generated.resources.report_send
import com.cmptemplate.core.designsystem.generated.resources.report_sending
import com.cmptemplate.core.designsystem.generated.resources.report_sent
import com.cmptemplate.core.designsystem.generated.resources.report_ticket
import com.cmptemplate.core.designsystem.generated.resources.report_title
import com.cmptemplate.feature.about.ControlFixLinks
import com.cmptemplate.feature.about.report.ReportKind
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ReportRoot(kind: ReportKind, onBack: () -> Unit, viewModel: ReportViewModel = koinViewModel { parametersOf(kind) }) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ReportScreen(state, viewModel::onAction, onBack, mailUri = viewModel::mailUri)
}

/** Crash report, bug report or suggestion, straight to the developer's ticket queue. */
@Composable
fun ReportScreen(
    state: ReportState,
    onAction: (ReportAction) -> Unit,
    onBack: () -> Unit,
    mailUri: () -> String,
    uriHandler: UriHandler = LocalUriHandler.current,
) {
    var noMailApp by rememberSaveable { mutableStateOf(false) }
    Column(
        Modifier.fillMaxSize().testTag("report_screen").background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding().imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val w = Modifier.fillMaxWidth().widthIn(max = ContentMaxWidth)
        ScreenHeader(stringResource(Res.string.report_title), onBack, stringResource(Res.string.common_back))

        val sent = state.status as? ReportStatus.Sent
        if (sent != null) {
            Text(stringResource(Res.string.report_sent), w.testTag("report_sent"), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
            if (sent.ticketId.isNotBlank()) {
                Text(stringResource(Res.string.report_ticket, sent.ticketId), w, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = onBack, Modifier.testTag("report_close")) { Text(stringResource(Res.string.common_close)) }
            return@Column
        }

        Text(stringResource(Res.string.report_body), w, color = MaterialTheme.colorScheme.onSurfaceVariant)
        FlowRow(w, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (state.crashAt != null || state.kind == ReportKind.CRASH) {
                FilterChip(state.kind == ReportKind.CRASH, { onAction(ReportAction.Kind(ReportKind.CRASH)) }, { Text(stringResource(Res.string.report_kind_crash)) }, Modifier.testTag("report_kind_crash"))
            }
            FilterChip(state.kind == ReportKind.BUG, { onAction(ReportAction.Kind(ReportKind.BUG)) }, { Text(stringResource(Res.string.report_kind_bug)) }, Modifier.testTag("report_kind_bug"))
            FilterChip(state.kind == ReportKind.SUGGESTION, { onAction(ReportAction.Kind(ReportKind.SUGGESTION)) }, { Text(stringResource(Res.string.report_kind_suggestion)) }, Modifier.testTag("report_kind_suggestion"))
        }
        OutlinedTextField(
            state.title, { onAction(ReportAction.Title(it)) }, w.testTag("report_field_title"),
            label = { Text(stringResource(Res.string.report_field_title)) }, singleLine = true,
        )
        OutlinedTextField(
            state.message, { onAction(ReportAction.Message(it)) }, w.heightIn(min = 140.dp).testTag("report_field_message"),
            label = { Text(stringResource(Res.string.report_field_message)) },
        )
        OutlinedTextField(
            state.contact, { onAction(ReportAction.Contact(it)) }, w.testTag("report_field_contact"),
            label = { Text(stringResource(Res.string.report_field_contact)) }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        Row(w, verticalAlignment = Alignment.CenterVertically) {
            Checkbox(state.includeLogs, { onAction(ReportAction.IncludeLogs(it)) }, Modifier.testTag("report_logs"))
            Column {
                Text(stringResource(Res.string.report_logs))
                Text(stringResource(Res.string.report_logs_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        val crashAt = state.crashAt
        if (crashAt != null && (state.includeLogs || state.kind == ReportKind.CRASH)) {
            Text(stringResource(Res.string.report_crash_attached, formatTime(crashAt)), w, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        val failed = state.status as? ReportStatus.Failed
        if (failed != null) {
            Text(
                stringResource(
                    when (failed.reason) {
                        SendFailure.OFFLINE -> Res.string.report_failed_offline
                        SendFailure.RATE_LIMITED -> Res.string.report_failed_rate
                        SendFailure.REJECTED -> Res.string.report_failed_rejected
                    },
                ),
                w.testTag("report_failed"),
                color = MaterialTheme.colorScheme.error,
            )
        }
        FlowRow(w, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onAction(ReportAction.Send) }, enabled = state.canSend, modifier = Modifier.testTag("report_send")) {
                Text(stringResource(if (state.status == ReportStatus.Sending) Res.string.report_sending else Res.string.report_send))
            }
            if (failed != null) {
                OutlinedButton(onClick = { noMailApp = !uriHandler.openSafely(mailUri()) }, Modifier.testTag("report_email")) {
                    Text(stringResource(Res.string.report_email))
                }
            }
        }
        if (noMailApp) {
            Text(stringResource(Res.string.report_no_mail, ControlFixLinks.SUPPORT_EMAIL), w, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}

/** Local "yyyy-MM-dd HH:mm"; numbers only, so no per-locale month names to translate. */
internal fun formatTime(epochMillis: Long): String {
    val t = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(TimeZone.currentSystemDefault())
    fun Int.two() = toString().padStart(2, '0')
    return "${t.year}-${t.monthNumber.two()}-${t.dayOfMonth.two()} ${t.hour.two()}:${t.minute.two()}"
}
