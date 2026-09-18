package com.kmptemplate.feature.example.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kmptemplate.core.common.DataError
import com.kmptemplate.core.common.onFailure
import com.kmptemplate.feature.example.domain.ExampleRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExampleViewModel(
    private val repository: ExampleRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val isLoading = MutableStateFlow(true)
    private val error = MutableStateFlow<DataError.Remote?>(null)
    private val query = savedStateHandle.getStateFlow("query", "")
    private var initialLoadStarted = false
    private var refreshJob: Job? = null

    val state = combine(repository.observeItems(), isLoading, error, query) { items, loading, err, filter ->
        ExampleState(items.filter { it.title.contains(filter, ignoreCase = true) }, loading, err, filter)
    }.onStart {
        if (!initialLoadStarted) {
            initialLoadStarted = true
            refresh()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExampleState(query = query.value))

    fun onAction(action: ExampleAction) {
        when (action) {
            ExampleAction.Retry -> refresh()
            is ExampleAction.QueryChanged -> savedStateHandle["query"] = action.query
        }
    }

    private fun refresh() {
        if (refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch {
            isLoading.value = true
            error.value = null
            try {
                repository.refresh().onFailure { error.value = it }
            } finally {
                isLoading.value = false
            }
        }
    }
}
