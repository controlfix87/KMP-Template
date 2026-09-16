package com.kmptemplate.feature.example.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe Navigation Compose destinations (androidx.navigation 2.8+ /
 * org.jetbrains.androidx.navigation) -- pass these to `navController.navigate(...)`
 * instead of a hand-built route string.
 */
sealed interface ExampleRoute {
    @Serializable
    data object List : ExampleRoute

    @Serializable
    data class Detail(val itemId: String) : ExampleRoute
}
