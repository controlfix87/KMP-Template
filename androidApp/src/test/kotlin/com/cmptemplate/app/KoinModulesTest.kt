package com.cmptemplate.app

import androidx.lifecycle.SavedStateHandle
import com.cmptemplate.core.designsystem.i18n.AppLocale
import com.cmptemplate.core.designsystem.i18n.LocaleManager
import com.cmptemplate.core.designsystem.i18n.LocaleStore
import com.cmptemplate.feature.example.domain.ExampleRepository
import com.cmptemplate.feature.example.ui.ExampleViewModel
import com.cmptemplate.core.datastore.InMemoryUserPreferences
import com.cmptemplate.core.datastore.UserPreferencesRepository
import com.cmptemplate.feature.about.ui.AboutViewModel
import com.cmptemplate.core.model.AppInfo
import com.cmptemplate.feature.about.report.Diagnostics
import com.cmptemplate.feature.about.report.NoDiagnostics
import com.cmptemplate.feature.about.report.ReportKind
import com.cmptemplate.feature.about.report.ReportSender
import com.cmptemplate.feature.about.ui.ReportViewModel
import com.cmptemplate.feature.onboarding.ui.OnboardingViewModel // [onboarding]
import org.junit.jupiter.api.Test
import org.koin.core.parameter.parametersOf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.koin.dsl.koinApplication
import org.koin.dsl.module

class KoinModulesTest {
    @Test fun `production composition resolves sample repository and view model`() {
        val app = koinApplication {
            modules(appModules + module { factory { SavedStateHandle() } })
        }
        try {
            assertNotNull(app.koin.get<ExampleRepository>())
            assertNotNull(app.koin.get<ExampleViewModel>())
        } finally { app.close() }
    }

    @Test fun `production composition resolves the locale manager`() {
        // The real Android LocaleStore needs a Context, which a plain JVM unit test cannot build.
        // Overriding that one seam keeps this a graph test rather than a Robolectric test, while
        // still proving localeModule() is actually in appModules and that LocaleManager resolves
        // through it -- the language never reaching the UI would otherwise fail only at runtime.
        val store = object : LocaleStore {
            var stored: String? = AppLocale.Russian.code
            override fun read(): String? = stored
            override fun write(code: String) { stored = code }
        }
        val app = koinApplication {
            modules(appModules + module { single<LocaleStore> { store } })
        }
        try {
            assertEquals(AppLocale.Russian, app.koin.get<LocaleManager>().locale.value)
        } finally { app.close() }
    }

    /** Host-only bindings replaced by fakes: Context-backed stores (DataStore, language) and AppInfo. */
    private val hostFakes = module {
        single<UserPreferencesRepository> { InMemoryUserPreferences() }
        single<LocaleStore> { object : LocaleStore { override fun read(): String? = null; override fun write(code: String) = Unit } }
        single<Diagnostics> { NoDiagnostics }
        single { AppInfo("com.cmptemplate.app", "CMPTemplate", "1", "1", "test", "android") }
        factory { SavedStateHandle() }
    }

    @Test fun `production composition resolves app, about, report and onboarding view models`() {
        val app = koinApplication { modules(appModules + hostFakes) }
        try {
            assertNotNull(app.koin.get<AppViewModel>())
            assertNotNull(app.koin.get<AboutViewModel>())
            assertNotNull(app.koin.get<ReportSender>())
            assertNotNull(app.koin.get<ReportViewModel> { parametersOf(ReportKind.BUG) })
            assertNotNull(app.koin.get<OnboardingViewModel>()) // [onboarding]
        } finally { app.close() }
    }
}
