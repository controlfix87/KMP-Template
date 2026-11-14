package com.cmptemplate.feature.about.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cmptemplate.core.model.ThemeMode
import com.cmptemplate.core.designsystem.components.AppIcons
import com.cmptemplate.core.designsystem.components.AppMark
import com.cmptemplate.core.designsystem.components.LanguageChoices
import com.cmptemplate.core.designsystem.components.ScreenHeader
import com.cmptemplate.core.designsystem.components.SectionCard
import com.cmptemplate.core.designsystem.components.SectionLabel
import com.cmptemplate.core.designsystem.components.ThemeChoices
import com.cmptemplate.core.designsystem.components.Wordmark
import com.cmptemplate.core.designsystem.generated.resources.Res
import com.cmptemplate.core.designsystem.generated.resources.about_body
import com.cmptemplate.core.designsystem.generated.resources.about_feedback
import com.cmptemplate.core.designsystem.generated.resources.about_made_by
import com.cmptemplate.core.designsystem.generated.resources.about_privacy
import com.cmptemplate.core.designsystem.generated.resources.about_report
import com.cmptemplate.core.designsystem.generated.resources.about_run_setup
import com.cmptemplate.core.designsystem.generated.resources.about_settings
import com.cmptemplate.core.designsystem.generated.resources.about_suggest
import com.cmptemplate.core.designsystem.generated.resources.about_support
import com.cmptemplate.core.designsystem.generated.resources.about_title
import com.cmptemplate.core.designsystem.generated.resources.about_version
import com.cmptemplate.core.designsystem.generated.resources.about_website
import com.cmptemplate.core.designsystem.generated.resources.app_name
import com.cmptemplate.core.designsystem.generated.resources.common_back
import com.cmptemplate.core.designsystem.generated.resources.report_no_mail
import com.cmptemplate.core.designsystem.i18n.AppLocale
import com.cmptemplate.core.model.AppInfo
import com.cmptemplate.feature.about.ControlFixLinks
import com.cmptemplate.feature.about.report.ReportKind
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Wires the ViewModel; [AboutScreen] itself is state in, callbacks out (and screenshot-testable). */
@Composable
fun AboutRoot(
    onBack: (() -> Unit)?,
    onReport: (ReportKind) -> Unit,
    onRunSetupAgain: (() -> Unit)?,
    viewModel: AboutViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AboutScreen(
        app = state.app,
        locale = state.locale,
        theme = state.theme,
        onLocale = { viewModel.onAction(AboutAction.SetLocale(it)) },
        onTheme = { viewModel.onAction(AboutAction.SetTheme(it)) },
        onBack = onBack,
        onReport = onReport,
        onRunSetupAgain = onRunSetupAgain?.let { navigate -> { viewModel.onAction(AboutAction.RunSetupAgain); navigate() } },
    )
}

/**
 * ControlFix-branded About: mark, name, version, "Made by ControlFix", the app's own line, website,
 * support e-mail, report / suggest, language, theme, privacy and (with onboarding) run setup again.
 */
@Composable
fun AboutScreen(
    app: AppInfo,
    locale: AppLocale,
    theme: ThemeMode,
    onLocale: (AppLocale) -> Unit,
    onTheme: (ThemeMode) -> Unit,
    onBack: (() -> Unit)?,
    onReport: (ReportKind) -> Unit,
    onRunSetupAgain: (() -> Unit)?,
    uriHandler: UriHandler = LocalUriHandler.current,
) {
    var noMailApp by rememberSaveable { mutableStateOf(false) }
    Column(
        Modifier.fillMaxSize().testTag("about_screen").background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ScreenHeader(stringResource(Res.string.about_title), onBack, stringResource(Res.string.common_back))

        SectionCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AppMark(size = 72.dp)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Wordmark(stringResource(Res.string.app_name))
                    Text(stringResource(Res.string.about_version, app.version), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        stringResource(Res.string.about_made_by),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(stringResource(Res.string.about_body), color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { uriHandler.openSafely(ControlFixLinks.WEBSITE) }, Modifier.testTag("about_website")) {
                    Text(stringResource(Res.string.about_website))
                }
                TextButton(onClick = { uriHandler.openSafely(ControlFixLinks.PRIVACY) }, Modifier.testTag("about_privacy")) {
                    Text(stringResource(Res.string.about_privacy))
                }
            }
        }

        SectionCard {
            SectionLabel(stringResource(Res.string.about_feedback), AppIcons.Mail)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onReport(ReportKind.BUG) }, Modifier.testTag("about_report")) {
                    Icon(AppIcons.Bug, contentDescription = null, Modifier.size(ButtonDefaults.IconSize))
                    Text(stringResource(Res.string.about_report), Modifier.padding(start = 8.dp))
                }
                OutlinedButton(onClick = { onReport(ReportKind.SUGGESTION) }, Modifier.testTag("about_suggest")) {
                    Icon(AppIcons.Lightbulb, contentDescription = null, Modifier.size(ButtonDefaults.IconSize))
                    Text(stringResource(Res.string.about_suggest), Modifier.padding(start = 8.dp))
                }
            }
            TextButton(
                onClick = { noMailApp = !uriHandler.openSafely("mailto:${ControlFixLinks.SUPPORT_EMAIL}") },
                modifier = Modifier.testTag("about_support"),
            ) { Text(stringResource(Res.string.about_support, ControlFixLinks.SUPPORT_EMAIL)) }
            if (noMailApp) {
                Text(
                    stringResource(Res.string.report_no_mail, ControlFixLinks.SUPPORT_EMAIL),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        SectionCard {
            SectionLabel(stringResource(Res.string.about_settings), AppIcons.Info)
            LanguageChoices(locale, onLocale)
            ThemeChoices(theme, onTheme)
            if (onRunSetupAgain != null) {
                OutlinedButton(onClick = onRunSetupAgain, Modifier.testTag("about_run_setup")) {
                    Text(stringResource(Res.string.about_run_setup))
                }
            }
        }
    }
}

/** Opens a link; false when nothing on the device can (no browser / no mail app). */
internal fun UriHandler.openSafely(uri: String): Boolean = try {
    openUri(uri)
    true
} catch (e: Exception) {
    false
}
