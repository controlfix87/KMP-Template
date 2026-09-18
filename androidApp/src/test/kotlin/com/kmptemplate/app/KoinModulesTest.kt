package com.kmptemplate.app

import androidx.lifecycle.SavedStateHandle
import com.kmptemplate.core.designsystem.i18n.AppLocale
import com.kmptemplate.core.designsystem.i18n.LocaleManager
import com.kmptemplate.core.designsystem.i18n.LocaleStore
import com.kmptemplate.feature.example.domain.ExampleRepository
import com.kmptemplate.feature.example.ui.ExampleViewModel
import org.junit.jupiter.api.Test
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
}
