package com.kmptemplate.app

import androidx.lifecycle.SavedStateHandle
import com.kmptemplate.feature.example.domain.ExampleRepository
import com.kmptemplate.feature.example.ui.ExampleViewModel
import org.junit.jupiter.api.Test
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
}
