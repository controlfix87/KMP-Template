package com.kmptemplate.feature.example.ui

/**
 * One-off effects the Screen consumes exactly once (navigation, a snackbar) --
 * unlike [ExampleState], which the Screen re-renders from on every emission.
 * Collected from [ExampleViewModel.events] with `LaunchedEffect` in the Screen.
 */
sealed interface ExampleEvent {
    data class NavigateToDetail(val itemId: String) : ExampleEvent
}
