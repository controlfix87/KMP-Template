package com.kmptemplate.feature.example.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.kmptemplate.feature.example.ui.ExampleRoot
import com.kmptemplate.feature.example.ui.ExampleDetailScreen

fun EntryProviderScope<NavKey>.exampleEntries(onDetail: (String) -> Unit, onBack: () -> Unit) {
    entry<ExampleRoute.List> { ExampleRoot(onNavigateToDetail = onDetail) }
    entry<ExampleRoute.Detail> { key -> ExampleDetailScreen(key.itemId, onBack) }
}
