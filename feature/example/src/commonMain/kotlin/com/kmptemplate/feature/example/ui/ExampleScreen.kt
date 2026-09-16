package com.kmptemplate.feature.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kmptemplate.core.common.DataError
import com.kmptemplate.core.designsystem.generated.resources.Res
import com.kmptemplate.core.designsystem.generated.resources.example_error_generic
import com.kmptemplate.core.designsystem.generated.resources.example_error_no_internet
import com.kmptemplate.core.designsystem.generated.resources.example_loading
import com.kmptemplate.core.designsystem.generated.resources.example_retry
import com.kmptemplate.core.designsystem.generated.resources.example_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * The only place [DataError] gets turned into a string, and the only place
 * that imports anything Compose/resource-related in this feature. Everything
 * below the ViewModel stays commonTest-able with no Android and no Compose on
 * the classpath.
 */
@Composable
fun ExampleRoute(
    onNavigateToDetail: (String) -> Unit,
    viewModel: ExampleViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is ExampleEvent.NavigateToDetail -> onNavigateToDetail(event.itemId)
            }
        }
    }

    ExampleScreen(state = state, onAction = viewModel::onAction)
}

@Composable
private fun ExampleScreen(
    state: ExampleState,
    onAction: (ExampleAction) -> Unit,
) {
    Scaffold { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            when {
                state.isLoading -> CircularProgressIndicator()
                state.error != null -> ErrorContent(state.error, onAction)
                else -> ItemList(state)
            }
        }
    }
}

@Composable
private fun ErrorContent(error: DataError.Remote, onAction: (ExampleAction) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val message = when (error) {
            DataError.Remote.NO_INTERNET -> stringResource(Res.string.example_error_no_internet)
            else -> stringResource(Res.string.example_error_generic)
        }
        Text(message)
        Button(onClick = { onAction(ExampleAction.Retry) }) {
            Text(stringResource(Res.string.example_retry))
        }
    }
}

@Composable
private fun ItemList(state: ExampleState) {
    Column {
        Text(stringResource(Res.string.example_title), style = MaterialTheme.typography.titleLarge)
        LazyColumn {
            items(state.items) { item ->
                Text(item.title, modifier = Modifier.padding(16.dp))
            }
        }
    }
}
