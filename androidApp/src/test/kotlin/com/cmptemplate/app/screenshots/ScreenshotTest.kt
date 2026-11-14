package com.cmptemplate.app.screenshots

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cmptemplate.app.App
import com.cmptemplate.app.androidHostModule
import com.cmptemplate.app.appModules
import com.cmptemplate.core.designsystem.CMPTemplateTheme
import com.cmptemplate.core.designsystem.ThemeMode
import com.cmptemplate.core.designsystem.i18n.AppLocale
import com.cmptemplate.core.designsystem.settings.AppPreferences
import com.cmptemplate.core.model.AppInfo
import com.cmptemplate.feature.about.report.ReportKind
import com.cmptemplate.feature.about.ui.AboutScreen
import com.cmptemplate.feature.about.ui.ReportScreen
import com.cmptemplate.feature.about.ui.ReportState
import com.cmptemplate.feature.about.ui.ReportStatus
import com.cmptemplate.feature.about.ui.SendFailure
import com.cmptemplate.feature.onboarding.ui.OnboardingScreen // [onboarding]
import com.cmptemplate.feature.onboarding.ui.OnboardingState // [onboarding]
import com.cmptemplate.feature.onboarding.ui.OnboardingStep // [onboarding]
import com.cmptemplate.feature.onboarding.ui.OnboardingViewModel // [onboarding]
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.mp.KoinPlatform
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** A phone in portrait and the Galaxy Z Fold's inner screen (GameController sizes). */
private const val PHONE = "w411dp-h891dp-xxhdpi"
private const val FOLD = "w882dp-h736dp-land-xhdpi"
private const val DIR = "build/outputs/roborazzi"

/**
 * Shared screens drawn on the JVM, no phone needed: `./gradlew :androidApp:recordRoborazziDebug`
 * writes them to androidApp/build/outputs/roborazzi/, `verifyRoborazziDebug` compares against the
 * last recording. Screens are stateless (state in, callbacks out), so each shot sets its state
 * directly. GameController's screenshot tests found light-on-light text, overlapping buttons and
 * landscape overflow that no unit test saw: look at the images after every UI change.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
// A plain Application: the real one starts Koin itself, and Robolectric makes one per test.
@Config(sdk = [36], qualifiers = PHONE, application = Application::class)
class ScreenshotTest {
    @get:Rule val compose = createComposeRule()

    private val app = AppInfo("com.cmptemplate.app", "CMPTemplate", "1.0.0", "1", "android/debug", "android")

    @Before fun koin() {
        startKoin {
            val context = ApplicationProvider.getApplicationContext<Application>()
            androidContext(context)
            modules(appModules + androidHostModule(context))
        }
    }

    @After fun stop() = stopKoin()

    private fun shot(name: String, dark: Boolean = true, content: @Composable () -> Unit) {
        compose.setContent { CMPTemplateTheme(darkTheme = dark) { content() } }
        compose.waitForIdle()
        compose.onRoot().captureRoboImage("$DIR/$name.png")
    }

    @Test fun home() {
        OnboardingViewModel.markDone(KoinPlatform.getKoin().get<AppPreferences>()) // [onboarding]
        compose.setContent { App() }
        compose.waitForIdle()
        compose.onRoot().captureRoboImage("$DIR/home_phone.png")
    }

    // region onboarding
    @Test fun onboardingSteps() {
        // setContent may run once per test: switch the step through state instead.
        var step by mutableStateOf(OnboardingStep.WELCOME)
        compose.setContent {
            CMPTemplateTheme(darkTheme = true) { OnboardingScreen(OnboardingState(step), AppLocale.English, ThemeMode.SYSTEM, {}, {}, {}) }
        }
        for (s in OnboardingStep.entries) {
            compose.runOnIdle { step = s }
            compose.waitForIdle()
            compose.onRoot().captureRoboImage("$DIR/onboarding_${s.ordinal + 1}_${s.name.lowercase()}.png")
        }
    }

    @Test fun onboardingHebrewLight() = shot("onboarding_welcome_he_light", dark = false) {
        OnboardingScreen(OnboardingState(OnboardingStep.WELCOME), AppLocale.Hebrew, ThemeMode.LIGHT, {}, {}, {})
    }

    @Test @Config(qualifiers = FOLD) fun onboardingFold() = shot("onboarding_welcome_fold") {
        OnboardingScreen(OnboardingState(OnboardingStep.WELCOME), AppLocale.English, ThemeMode.DARK, {}, {}, {})
    }
    // endregion onboarding

    @Test fun about() = shot("about_dark") { about() }

    @Test fun aboutLight() = shot("about_light", dark = false) { about() }

    @Test @Config(qualifiers = FOLD) fun aboutFold() = shot("about_fold") { about() }

    @Test fun report() = shot("report_crash") {
        ReportScreen(
            ReportState(kind = ReportKind.CRASH, title = "Closes on start", message = "After the update", crashAt = 1_790_000_000_000L),
            onAction = {}, onBack = {}, mailUri = { "" },
        )
    }

    @Test fun reportFailed() = shot("report_failed", dark = false) {
        ReportScreen(
            ReportState(title = "Sync", message = "Never finishes", status = ReportStatus.Failed(SendFailure.OFFLINE)),
            onAction = {}, onBack = {}, mailUri = { "" },
        )
    }

    @Test fun reportSent() = shot("report_sent") {
        ReportScreen(ReportState(status = ReportStatus.Sent("20261114T101500-1a2b3c4d")), onAction = {}, onBack = {}, mailUri = { "" })
    }

    @Composable
    private fun about() = AboutScreen(
        app = app, locale = AppLocale.English, theme = ThemeMode.SYSTEM,
        onLocale = {}, onTheme = {}, onBack = {}, onReport = {},
        onRunSetupAgain = {}, // [onboarding]
        // onRunSetupAgain = null, // without onboarding
    )
}
