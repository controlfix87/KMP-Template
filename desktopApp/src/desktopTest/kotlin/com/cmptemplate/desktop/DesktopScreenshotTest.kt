package com.cmptemplate.desktop

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runDesktopComposeUiTest
import com.cmptemplate.app.App
import com.cmptemplate.app.appModules
import com.cmptemplate.core.designsystem.CMPTemplateTheme
import com.cmptemplate.core.designsystem.ThemeMode
import com.cmptemplate.core.designsystem.i18n.AppLocale
import com.cmptemplate.core.designsystem.i18n.LocaleStore
import com.cmptemplate.core.designsystem.settings.AppPreferences
import com.cmptemplate.core.designsystem.settings.InMemoryPreferences
import com.cmptemplate.core.model.AppInfo
import com.cmptemplate.feature.about.ui.AboutScreen
import com.cmptemplate.feature.onboarding.ui.OnboardingScreen // [onboarding]
import com.cmptemplate.feature.onboarding.ui.OnboardingState // [onboarding]
import com.cmptemplate.feature.onboarding.ui.OnboardingStep // [onboarding]
import com.cmptemplate.feature.onboarding.ui.OnboardingViewModel // [onboarding]
import io.github.takahirom.roborazzi.captureRoboImage
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

private const val DIR = "build/outputs/roborazzi"

/**
 * The same shared screens rendered by Compose Desktop (Skia on the JVM): a desktop window size,
 * no Robolectric. `./gradlew :desktopApp:recordRoborazziDesktop` writes desktopApp/build/outputs/roborazzi/.
 */
@OptIn(ExperimentalTestApi::class)
class DesktopScreenshotTest {
    // In-memory settings and language: a test must never write the developer's real preferences.
    private val preferences = InMemoryPreferences()

    @BeforeTest fun koin() {
        val memoryLocale = object : LocaleStore {
            var code: String? = null
            override fun read() = code
            override fun write(code: String) { this.code = code }
        }
        startKoin {
            modules(appModules + desktopHostModule() + module {
                single<AppPreferences> { preferences }
                single<LocaleStore> { memoryLocale }
            })
        }
    }

    @AfterTest fun stop() = stopKoin()

    @Test fun home() = runDesktopComposeUiTest(width = 1024, height = 760) {
        OnboardingViewModel.markDone(preferences) // [onboarding]
        setContent { App() }
        waitForIdle()
        onRoot().captureRoboImage("$DIR/desktop_home.png")
    }

    @Test fun about() = runDesktopComposeUiTest(width = 1024, height = 900) {
        setContent {
            CMPTemplateTheme(darkTheme = true) {
                AboutScreen(
                    app = AppInfo("com.cmptemplate.app", "CMPTemplate", "1.0.0", "1", "desktop", "desktop"),
                    locale = AppLocale.English, theme = ThemeMode.DARK,
                    onLocale = {}, onTheme = {}, onBack = {}, onReport = {},
                    onRunSetupAgain = {}, // [onboarding]
                    // onRunSetupAgain = null, // without onboarding
                )
            }
        }
        waitForIdle()
        onRoot().captureRoboImage("$DIR/desktop_about.png")
    }

    // region onboarding
    @Test fun onboarding() = runDesktopComposeUiTest(width = 1024, height = 760) {
        setContent {
            CMPTemplateTheme(darkTheme = false) {
                OnboardingScreen(OnboardingState(OnboardingStep.WELCOME), AppLocale.French, ThemeMode.LIGHT, {}, {}, {})
            }
        }
        waitForIdle()
        onRoot().captureRoboImage("$DIR/desktop_onboarding_welcome.png")
    }
    // endregion onboarding
}
