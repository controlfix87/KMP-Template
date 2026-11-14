package com.cmptemplate.feature.example.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.cmptemplate.feature.example.ui.ExampleRoot
import com.cmptemplate.feature.example.ui.ExampleDetailScreen

fun EntryProviderScope<NavKey>.exampleEntries(onDetail: (String) -> Unit, onBack: () -> Unit, onAbout: () -> Unit = {}) {
    entry<ExampleRoute.List> { ExampleRoot(onNavigateToDetail = onDetail, onAbout = onAbout) }
    entry<ExampleRoute.Detail> { key -> ExampleDetailScreen(key.itemId, onBack) }
}
