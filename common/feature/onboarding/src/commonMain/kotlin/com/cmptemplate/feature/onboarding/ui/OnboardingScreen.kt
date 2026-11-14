package com.cmptemplate.feature.onboarding.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cmptemplate.core.designsystem.ThemeManager
import com.cmptemplate.core.designsystem.ThemeMode
import com.cmptemplate.core.designsystem.components.AppIcons
import com.cmptemplate.core.designsystem.components.AppMark
import com.cmptemplate.core.designsystem.components.ContentMaxWidth
import com.cmptemplate.core.designsystem.components.LanguageChoices
import com.cmptemplate.core.designsystem.components.StepProgress
import com.cmptemplate.core.designsystem.components.ThemeChoices
import com.cmptemplate.core.designsystem.generated.resources.Res
import com.cmptemplate.core.designsystem.generated.resources.app_name
import com.cmptemplate.core.designsystem.generated.resources.onb_app_body
import com.cmptemplate.core.designsystem.generated.resources.onb_app_title
import com.cmptemplate.core.designsystem.generated.resources.onb_back
import com.cmptemplate.core.designsystem.generated.resources.onb_done_body
import com.cmptemplate.core.designsystem.generated.resources.onb_done_title
import com.cmptemplate.core.designsystem.generated.resources.onb_next
import com.cmptemplate.core.designsystem.generated.resources.onb_skip
import com.cmptemplate.core.designsystem.generated.resources.onb_start
import com.cmptemplate.core.designsystem.generated.resources.onb_step_app
import com.cmptemplate.core.designsystem.generated.resources.onb_step_done
import com.cmptemplate.core.designsystem.generated.resources.onb_step_of
import com.cmptemplate.core.designsystem.generated.resources.onb_step_welcome
import com.cmptemplate.core.designsystem.generated.resources.onb_welcome_body
import com.cmptemplate.core.designsystem.generated.resources.onb_welcome_title
import com.cmptemplate.core.designsystem.i18n.AppLocale
import com.cmptemplate.core.designsystem.i18n.LocaleManager
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OnboardingRoot(onDone: () -> Unit, viewModel: OnboardingViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val localeManager: LocaleManager = koinInject()
    val themeManager: ThemeManager = koinInject()
    val locale by localeManager.locale.collectAsStateWithLifecycle()
    val theme by themeManager.mode.collectAsStateWithLifecycle()
    OnboardingScreen(
        state = state,
        locale = locale,
        theme = theme,
        onLocale = localeManager::setLocale,
        onTheme = themeManager::setMode,
        onAction = { if (viewModel.onAction(it)) onDone() },
    )
}

/**
 * First run (LockdownControl / GameController pattern): numbered, titled step bar; language and
 * theme first, as cards; Back, Skip (every step but the last) and Next; the last step says where
 * to run setup again.
 */
@Composable
fun OnboardingScreen(
    state: OnboardingState,
    locale: AppLocale,
    theme: ThemeMode,
    onLocale: (AppLocale) -> Unit,
    onTheme: (ThemeMode) -> Unit,
    onAction: (OnboardingAction) -> Unit,
) {
    val titles = OnboardingStep.entries.map {
        stringResource(
            when (it) {
                OnboardingStep.WELCOME -> Res.string.onb_step_welcome
                OnboardingStep.APP -> Res.string.onb_step_app
                OnboardingStep.DONE -> Res.string.onb_step_done
            },
        )
    }
    Box(
        Modifier.fillMaxSize().testTag("onboarding").background(MaterialTheme.colorScheme.background).safeDrawingPadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(Modifier.fillMaxSize().widthIn(max = ContentMaxWidth).padding(horizontal = 24.dp, vertical = 16.dp)) {
            Text(
                stringResource(Res.string.onb_step_of, state.index + 1, state.count),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            StepProgress(titles, state.index, onStep = { onAction(OnboardingAction.GoTo(it)) }, Modifier.padding(top = 4.dp))
            AnimatedContent(targetState = state.step, modifier = Modifier.weight(1f), label = "onboarding") { step ->
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    when (step) {
                        OnboardingStep.WELCOME -> WelcomeStep(locale, theme, onLocale, onTheme)
                        OnboardingStep.APP -> AppStep()
                        OnboardingStep.DONE -> DoneStep()
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (state.index > 0) {
                    OutlinedButton(onClick = { onAction(OnboardingAction.Back) }, Modifier.testTag("onboarding_back")) {
                        Text(stringResource(Res.string.onb_back))
                    }
                }
                Spacer(Modifier.weight(1f))
                if (!state.isLast) {
                    TextButton(onClick = { onAction(OnboardingAction.Skip) }, Modifier.testTag("onboarding_skip")) {
                        Text(stringResource(Res.string.onb_skip))
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { onAction(OnboardingAction.Next) }, Modifier.testTag("onboarding_next")) {
                        Text(stringResource(Res.string.onb_next))
                    }
                } else {
                    Button(onClick = { onAction(OnboardingAction.Finish) }, Modifier.testTag("onboarding_finish")) {
                        Text(stringResource(Res.string.onb_start))
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.WelcomeStep(locale: AppLocale, theme: ThemeMode, onLocale: (AppLocale) -> Unit, onTheme: (ThemeMode) -> Unit) {
    AppMark(Modifier.align(Alignment.CenterHorizontally), size = 88.dp)
    Text(
        stringResource(Res.string.onb_welcome_title, stringResource(Res.string.app_name)),
        Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onBackground,
    )
    Text(
        stringResource(Res.string.onb_welcome_body),
        Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
    LanguageChoices(locale, onLocale)
    ThemeChoices(theme, onTheme)
}

/** Template step: replace with the product's own (keep the title + body + controls shape). */
@Composable
private fun AppStep() {
    Text(stringResource(Res.string.onb_app_title), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
    Text(stringResource(Res.string.onb_app_body), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun ColumnScope.DoneStep() {
    Spacer(Modifier.height(24.dp))
    Icon(
        AppIcons.Check,
        contentDescription = null,
        modifier = Modifier.size(88.dp).align(Alignment.CenterHorizontally),
        tint = MaterialTheme.colorScheme.primary,
    )
    Text(
        stringResource(Res.string.onb_done_title),
        Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.headlineMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onBackground,
    )
    Text(
        stringResource(Res.string.onb_done_body),
        Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}
