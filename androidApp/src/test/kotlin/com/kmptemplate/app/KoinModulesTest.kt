package com.kmptemplate.app

import android.content.Context
import com.kmptemplate.app.di.coreNetworkModule
import com.kmptemplate.feature.example.di.featureExampleModule
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.koinApplication
import org.koin.test.check.checkModules

/**
 * Catches a missing Koin binding at build time instead of at screen-open
 * time. The Android [Context] is a relaxed mock rather than a real one --
 * Robolectric is unusable on an aarch64 dev host (missing conscrypt native
 * lib), and this test only needs the dependency graph to resolve, not to run
 * real Android framework code. When a new feature module needs something
 * beyond a plain constructor call from a mocked Context, add a stub module
 * here the way :feature:example doesn't currently need one -- see
 * eizeseret's KoinModulesTest for a worked example of that with real DB/network
 * bindings, if this template's checkModules() call ever needs to grow one.
 */
class KoinModulesTest {

    @Suppress("DEPRECATION") // checkModules() is Koin 4.2's name for this; verify() is the eventual replacement.
    @Test
    fun `every module resolves without a missing binding`() {
        val context = mockk<Context>(relaxed = true)

        koinApplication {
            androidContext(context)
            modules(
                coreNetworkModule(baseUrl = "http://localhost/", enableLogging = false),
                featureExampleModule,
            )
        }.checkModules()
    }
}
