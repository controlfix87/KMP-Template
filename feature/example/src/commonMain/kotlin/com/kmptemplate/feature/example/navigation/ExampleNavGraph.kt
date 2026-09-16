package com.kmptemplate.feature.example.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.kmptemplate.feature.example.ui.ExampleRoute

/**
 * Every feature exposes exactly one `NavGraphBuilder` extension like this.
 * :androidApp's top-level NavHost composes these together and owns the
 * single shared NavController -- a feature module never creates its own.
 */
fun NavGraphBuilder.exampleNavGraph(onNavigateToDetail: (String) -> Unit) {
    composable<ExampleRoute.List> {
        ExampleRoute(onNavigateToDetail = onNavigateToDetail)
    }
    // ExampleRoute.Detail intentionally left unimplemented -- this template
    // stops at "one working screen + navigation event", not a full detail flow.
}
