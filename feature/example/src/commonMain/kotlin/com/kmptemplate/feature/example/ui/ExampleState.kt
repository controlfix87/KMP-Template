package com.kmptemplate.feature.example.ui

import com.kmptemplate.core.common.DataError
import com.kmptemplate.core.model.ExampleItem

/**
 * No `UiText`, string resources, `Color`, or painters in here -- this class
 * must stay resolvable in commonTest with no Android/Compose on the
 * classpath. [error], being a [DataError] enum rather than a message string,
 * is what makes that possible; ExampleScreen is the one place that maps it
 * to a localized string via stringResource(...).
 */
data class ExampleState(
    val items: List<ExampleItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: DataError.Remote? = null,
)
