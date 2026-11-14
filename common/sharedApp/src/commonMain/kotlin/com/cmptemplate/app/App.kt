package com.cmptemplate.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.*
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.cmptemplate.core.designsystem.CMPTemplateTheme
import com.cmptemplate.core.datastore.dataStoreModule
import com.cmptemplate.core.designsystem.i18n.AppLocaleProvider
import com.cmptemplate.core.designsystem.i18n.LocaleManager
import com.cmptemplate.core.designsystem.i18n.localeModule
import com.cmptemplate.core.designsystem.isDark
import com.cmptemplate.feature.about.di.featureAboutModule
import com.cmptemplate.feature.about.navigation.AboutRoute
import com.cmptemplate.feature.about.navigation.aboutEntries
import com.cmptemplate.feature.example.di.featureExampleModule
import com.cmptemplate.feature.example.navigation.ExampleRoute
import com.cmptemplate.feature.example.navigation.exampleEntries
import com.cmptemplate.feature.onboarding.di.featureOnboardingModule // [onboarding]
import com.cmptemplate.feature.onboarding.navigation.OnboardingRoute // [onboarding]
import com.cmptemplate.feature.onboarding.navigation.onboardingEntries // [onboarding]
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

// localeModule() is a platform-specific `fun`, not a `val`, because the language preference has to
// be readable synchronously from Android's attachBaseContext -- before DI or coroutines exist.
// Each host adds one more module: `single { AppInfo(...) }` (see AndroidApplication, desktop Main,
// MainViewController).
val appModules = listOf(
    localeModule(),
    dataStoreModule(),
    module { viewModelOf(::AppViewModel) },
    featureExampleModule,
    featureAboutModule,
    featureOnboardingModule, // [onboarding]
)

private val navigationState = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(ExampleRoute.List::class, ExampleRoute.List.serializer())
            subclass(ExampleRoute.Detail::class, ExampleRoute.Detail.serializer())
            subclass(AboutRoute.About::class, AboutRoute.About.serializer())
            subclass(AboutRoute.Report::class, AboutRoute.Report.serializer())
            subclass(OnboardingRoute::class, OnboardingRoute.serializer()) // [onboarding]
        }
    }
}

/** The app's home destination. Replace with the first real feature's root route. */
private val Home: NavKey = ExampleRoute.List

@Composable
fun App() {
    val localeManager: LocaleManager = koinInject()
    val appViewModel: AppViewModel = koinViewModel()
    val locale by localeManager.locale.collectAsStateWithLifecycle()
    val appState by appViewModel.state.collectAsStateWithLifecycle()
    // One DataStore read at start; until then draw only the background, never a guessed screen.
    val ready = appState as? AppUiState.Ready
    if (ready == null) {
        CMPTemplateTheme { Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) }
        return
    }

    CMPTemplateTheme(darkTheme = ready.theme.isDark()) {
        // The back stack and the entry decorators are hoisted ABOVE AppLocaleProvider deliberately.
        // AppLocaleProvider re-keys its subtree on a language change -- that is what makes
        // already-resolved strings reload -- and anything created inside it is rebuilt from scratch.
        // A `rememberNavBackStack` in there would reset to the start destination (switching language
        // from a detail screen, or from onboarding, would bounce the user back), and the decorators
        // hold the per-entry ViewModel stores and saveable UI state, which would be dropped with it.
        // None of that is language-dependent, so none of it belongs inside. Hoist session-scoped
        // state (login, sockets, in-flight work) the same way; keep only the UI inside.
        // The start destination is read once; rememberNavBackStack keeps (and restores) the stack after.
        val start = if (ready.onboardingDone) Home else OnboardingRoute // [onboarding]
        // val start = Home // without onboarding
        val backStack = rememberNavBackStack(navigationState, start)
        val goBack: () -> Unit = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) }
        val push: (NavKey) -> Unit = { destination -> if (backStack.lastOrNull() != destination) backStack.add(destination) }
        // Add first, then drop the rest: the stack is never empty, not even between two writes.
        val resetTo: (NavKey) -> Unit = { destination -> backStack.add(destination); while (backStack.size > 1) backStack.removeAt(0) }
        // Explicit <NavKey> because these are only being created here, not passed straight into
        // NavDisplay, so there is no argument position left to infer the entry type from.
        val saveableStateDecorator = rememberSaveableStateHolderNavEntryDecorator<NavKey>()
        val viewModelStoreDecorator = rememberViewModelStoreNavEntryDecorator<NavKey>()

        // Strings and layout direction both come from this one value, so they cannot disagree.
        AppLocaleProvider(locale) {
            NavDisplay(
                backStack = backStack,
                onBack = goBack,
                entryDecorators = listOf(saveableStateDecorator, viewModelStoreDecorator),
                entryProvider = entryProvider {
                    exampleEntries(
                        onDetail = { id -> push(ExampleRoute.Detail(id)) },
                        onBack = goBack,
                        onAbout = { push(AboutRoute.About) },
                    )
                    aboutEntries(
                        onBack = goBack,
                        onReport = { kind -> push(AboutRoute.Report(kind.wire)) },
                        // About's ViewModel clears "done" in DataStore, then this navigates.
                        onRunSetupAgain = { resetTo(OnboardingRoute) }, // [onboarding]
                        // onRunSetupAgain = null, // without onboarding
                    )
                    onboardingEntries(onDone = { resetTo(Home) }) // [onboarding]
                },
            )
        }
    }
}
