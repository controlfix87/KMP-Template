package com.kmptemplate.feature.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kmptemplate.core.common.onFailure
import com.kmptemplate.feature.example.domain.ExampleRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExampleViewModel(
    private val repository: ExampleRepository,
) : ViewModel() {

    private val isLoading = MutableStateFlow(true)
    private val error = MutableStateFlow<com.kmptemplate.core.common.DataError.Remote?>(null)

    val state: StateFlow<ExampleState> = combine(
        repository.observeItems(),
        isLoading,
        error,
    ) { items, loading, err ->
        ExampleState(items = items, isLoading = loading, error = err)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExampleState())

    private val eventChannel = Channel<ExampleEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    init {
        load()
    }

    fun onAction(action: ExampleAction) {
        when (action) {
            ExampleAction.Retry -> load()
            is ExampleAction.ItemClicked -> viewModelScope.launch {
                eventChannel.send(ExampleEvent.NavigateToDetail(action.itemId))
            }
        }
    }

    private fun load() {
        isLoading.value = true
        error.value = null
        viewModelScope.launch {
            repository.refresh()
                .onFailure { error.value = it }
            isLoading.value = false
        }
    }
}
