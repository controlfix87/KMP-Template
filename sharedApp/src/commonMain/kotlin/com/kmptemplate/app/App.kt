package com.kmptemplate.app

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.*
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.kmptemplate.core.designsystem.KMPTemplateTheme
import com.kmptemplate.feature.example.di.featureExampleModule
import com.kmptemplate.feature.example.navigation.ExampleRoute
import com.kmptemplate.feature.example.navigation.exampleEntries
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

val appModules = listOf(featureExampleModule)

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
    KMPTemplateTheme {
        val backStack = rememberNavBackStack(navigationState, ExampleRoute.List)
        val goBack: () -> Unit = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) }
        NavDisplay(
            backStack = backStack,
            onBack = goBack,
            entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator(), rememberViewModelStoreNavEntryDecorator()),
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
