package com.beader.feature.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.beader.core.designsystem.theme.BeaderTheme
import com.beader.core.domain.model.SampleItem
import com.beader.core.ui.FullScreenError
import com.beader.core.ui.FullScreenLoading

@Composable
fun SampleRoute(
    modifier: Modifier = Modifier,
    viewModel: SampleViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    SampleScreen(
        uiState = uiState,
        onToggleFavorite = viewModel::onToggleFavorite,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SampleScreen(
    uiState: SampleUiState,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Beader Sample") }) },
    ) { paddingValues ->
        when (uiState) {
            is SampleUiState.Loading -> FullScreenLoading(modifier = Modifier.padding(paddingValues))

            is SampleUiState.Error ->
                FullScreenError(
                    message = uiState.message,
                    onRetry = { /* re-collecting the Flow triggers automatically on resubscribe */ },
                    modifier = Modifier.padding(paddingValues),
                )

            is SampleUiState.Success ->
                SampleList(
                    items = uiState.items,
                    onToggleFavorite = onToggleFavorite,
                    modifier = Modifier.padding(paddingValues),
                )
        }
    }
}

@Composable
private fun SampleList(
    items: List<SampleItem>,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(items = items, key = { it.id }) { item ->
            ListItem(
                headlineContent = { Text(item.title) },
                supportingContent = { Text(item.description) },
                trailingContent = {
                    IconButton(onClick = { onToggleFavorite(item.id) }) {
                        Icon(
                            imageVector = if (item.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = if (item.isFavorite) "Unfavorite" else "Favorite",
                        )
                    }
                },
            )
        }
    }
}

@Preview
@Composable
private fun SampleScreenPreview() {
    BeaderTheme {
        SampleScreen(
            uiState =
                SampleUiState.Success(
                    items =
                        listOf(
                            SampleItem(id = "1", title = "First item", description = "Description one"),
                            SampleItem(
                                id = "2",
                                title = "Second item",
                                description = "Description two",
                                isFavorite = true,
                            ),
                        ),
                ),
            onToggleFavorite = {},
        )
    }
}
