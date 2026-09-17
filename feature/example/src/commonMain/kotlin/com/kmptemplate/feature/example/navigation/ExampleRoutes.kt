package com.kmptemplate.feature.example.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface ExampleRoute : NavKey {
    @Serializable data object List : ExampleRoute
    @Serializable data class Detail(val itemId: String) : ExampleRoute
}
