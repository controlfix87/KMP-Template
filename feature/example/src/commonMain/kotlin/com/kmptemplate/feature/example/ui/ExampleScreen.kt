package com.kmptemplate.feature.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kmptemplate.core.common.DataError
import com.kmptemplate.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ExampleRoot(onNavigateToDetail: (String) -> Unit, viewModel: ExampleViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ExampleScreen(state, viewModel::onAction, onNavigateToDetail)
}

/** Scaffold owns safe insets. Children consume them before adding IME padding. */
@Composable
fun ExampleScreen(state: ExampleState, onAction: (ExampleAction) -> Unit, onDetail: (String) -> Unit) {
    val listState = rememberLazyListState()
    Scaffold(contentWindowInsets = WindowInsets.safeDrawing) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding).imePadding(), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                modifier = Modifier.widthIn(max = 840.dp).fillMaxSize().testTag("example_list"),
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item(key = "header") { Text(stringResource(Res.string.example_title), style = MaterialTheme.typography.headlineSmall) }
                item(key = "search") {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = { onAction(ExampleAction.QueryChanged(it)) },
                        label = { Text(stringResource(Res.string.example_search)) },
                        modifier = Modifier.fillMaxWidth().testTag("example_search"),
                        singleLine = true,
                    )
                }
                if (state.isLoading) item(key = "loading") {
                    CircularProgressIndicator(Modifier.testTag("example_loading"))
                }
                state.error?.let { error ->
                    item(key = "error") {
                        Text(stringResource(if (error == DataError.Remote.NO_INTERNET) Res.string.example_error_no_internet else Res.string.example_error_generic))
                        Button(onClick = { onAction(ExampleAction.Retry) }, enabled = !state.isLoading) { Text(stringResource(Res.string.example_retry)) }
                    }
                }
                if (!state.isLoading && state.error == null && state.items.isEmpty()) item(key = "empty") {
                    Text(stringResource(Res.string.example_empty), Modifier.testTag("example_empty"))
                }
                items(state.items, key = { "item-${it.id}" }, contentType = { "example" }) { item ->
                    ListItem(
                        headlineContent = { Text(item.title) },
                        modifier = Modifier.testTag("example_item_${item.id}").clickable { onDetail(item.id) },
                    )
                }
            }
        }
    }
}

@Composable
fun ExampleDetailScreen(itemId: String, onBack: () -> Unit) {
    Scaffold(contentWindowInsets = WindowInsets.safeDrawing) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding), contentAlignment = Alignment.TopCenter) {
            LazyColumn(Modifier.widthIn(max = 840.dp).fillMaxSize().testTag("example_detail"), contentPadding = PaddingValues(16.dp)) {
                item { Text(stringResource(Res.string.example_detail, itemId), style = MaterialTheme.typography.headlineSmall) }
                item { Button(onClick = onBack, modifier = Modifier.testTag("detail_back")) { Text(stringResource(Res.string.example_back)) } }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun ExampleContentPreview() {
    com.kmptemplate.core.designsystem.KMPTemplateTheme {
        ExampleScreen(
            ExampleState(items = listOf(com.kmptemplate.core.model.ExampleItem("1", "Sample 1")), isLoading = false),
            onAction = {}, onDetail = {},
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun ExampleErrorPreview() {
    com.kmptemplate.core.designsystem.KMPTemplateTheme {
        ExampleScreen(ExampleState(isLoading = false, error = DataError.Remote.NO_INTERNET), {}, {})
    }
}
