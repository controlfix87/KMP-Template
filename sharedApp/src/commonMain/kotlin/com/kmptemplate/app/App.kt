package com.kmptemplate.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.*
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.kmptemplate.core.designsystem.KMPTemplateTheme
import com.kmptemplate.core.designsystem.i18n.AppLocaleProvider
import com.kmptemplate.core.designsystem.i18n.LocaleManager
import com.kmptemplate.core.designsystem.i18n.localeModule
import com.kmptemplate.feature.example.di.featureExampleModule
import com.kmptemplate.feature.example.navigation.ExampleRoute
import com.kmptemplate.feature.example.navigation.exampleEntries
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.koin.compose.koinInject

// localeModule() is a platform-specific `fun`, not a `val`, because the language preference has to
// be readable synchronously from Android's attachBaseContext -- before DI or coroutines exist.
val appModules = listOf(localeModule(), featureExampleModule)

private val navigationState = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(ExampleRoute.List::class, ExampleRoute.List.serializer())
            subclass(ExampleRoute.Detail::class, ExampleRoute.Detail.serializer())
        }
    }
}

@Composable
fun App() {
    val localeManager: LocaleManager = koinInject()
    val locale by localeManager.locale.collectAsStateWithLifecycle()

    KMPTemplateTheme {
        // The back stack and the entry decorators are hoisted ABOVE AppLocaleProvider deliberately.
        // AppLocaleProvider re-keys its subtree on a language change -- that is what makes
        // already-resolved strings reload -- and anything created inside it is rebuilt from scratch.
        // A `rememberNavBackStack` in there would reset to the start destination (switching language
        // from a detail screen would bounce the user back to the list), and the decorators hold the
        // per-entry ViewModel stores and saveable UI state, which would be dropped with it. None of
        // that is language-dependent, so none of it belongs inside. Hoist session-scoped state
        // (login, sockets, in-flight work) the same way; keep only the UI inside.
        val backStack = rememberNavBackStack(navigationState, ExampleRoute.List)
        val goBack: () -> Unit = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) }
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
                        onDetail = { id ->
                            val destination = ExampleRoute.Detail(id)
                            if (backStack.lastOrNull() != destination) backStack.add(destination)
                        },
                        onBack = goBack,
                    )
                },
            )
        }
    }
}
