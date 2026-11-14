package com.cmptemplate.feature.example.ui

sealed interface ExampleAction {
    data object Retry : ExampleAction
    data class QueryChanged(val query: String) : ExampleAction
}
