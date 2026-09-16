package com.kmptemplate.feature.example.ui

/** User intents the Screen sends to the ViewModel. Never the other way round. */
sealed interface ExampleAction {
    data object Retry : ExampleAction
    data class ItemClicked(val itemId: String) : ExampleAction
}
