package com.beader.feature.library

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.beader.core.domain.model.PdfLibraryItem
import com.beader.core.ui.FullScreenError
import com.beader.core.ui.FullScreenLoading
import kotlinx.coroutines.flow.Flow

private const val THUMBNAIL_WIDTH_DP = 160
private const val FALLBACK_TITLE = "Untitled PDF"

@Composable
fun LibraryRoute(
    modifier: Modifier = Modifier,
    onOpenDocument: (String) -> Unit = {},
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val density = LocalDensity.current
    val thumbnailWidthPx = with(density) { THUMBNAIL_WIDTH_DP.dp.roundToPx() }

    val pickDocumentLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                viewModel.onImportDocument(
                    uri = uri.toString(),
                    fileName = queryDisplayName(context, uri),
                    thumbnailWidthPx = thumbnailWidthPx,
                )
            }
        }

    LibraryScreen(
        uiState = uiState,
        importErrors = viewModel.importErrors,
        actions =
            LibraryActions(
                onImportDocument = { pickDocumentLauncher.launch(arrayOf("application/pdf")) },
                onOpenDocument = onOpenDocument,
                onDeleteDocument = viewModel::onDeleteDocument,
            ),
        modifier = modifier,
    )
}

/**
 * Groups this screen's action callbacks so the top-level Composable stays
 * under the parameter-count lint threshold.
 */
@Immutable
data class LibraryActions(
    val onImportDocument: () -> Unit,
    val onOpenDocument: (String) -> Unit,
    val onDeleteDocument: (Long) -> Unit,
)

private fun queryDisplayName(
    context: Context,
    uri: Uri,
): String {
    context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex != -1 && cursor.moveToFirst()) {
            return cursor.getString(nameIndex) ?: FALLBACK_TITLE
        }
    }
    return uri.lastPathSegment ?: FALLBACK_TITLE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LibraryScreen(
    uiState: LibraryUiState,
    importErrors: Flow<String>,
    actions: LibraryActions,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(importErrors) {
        importErrors.collect { message -> snackbarHostState.showSnackbar(message) }
    }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Library") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = actions.onImportDocument) {
                Icon(Icons.Filled.Add, contentDescription = "Import PDF")
            }
        },
    ) { paddingValues ->
        when (uiState) {
            is LibraryUiState.Loading -> {
                FullScreenLoading(modifier = Modifier.padding(paddingValues))
            }

            is LibraryUiState.Error -> {
                FullScreenError(
                    message = uiState.message,
                    onRetry = actions.onImportDocument,
                    modifier = Modifier.padding(paddingValues),
                )
            }

            is LibraryUiState.Success -> {
                if (uiState.items.isEmpty()) {
                    LibraryEmptyState(modifier = Modifier.padding(paddingValues))
                } else {
                    LibraryGrid(
                        items = uiState.items,
                        onOpenDocument = actions.onOpenDocument,
                        onDeleteDocument = actions.onDeleteDocument,
                        modifier = Modifier.padding(paddingValues),
                    )
                }
            }
        }
    }
}

@Composable
private fun LibraryEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "No PDFs yet. Tap + to import one.", textAlign = TextAlign.Center)
    }
}

@Composable
private fun LibraryGrid(
    items: List<PdfLibraryItem>,
    onOpenDocument: (String) -> Unit,
    onDeleteDocument: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 120.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items = items, key = { it.id }) { item ->
            LibraryItemCard(
                item = item,
                onOpen = { onOpenDocument(item.uri) },
                onDelete = { onDeleteDocument(item.id) },
            )
        }
    }
}

@Composable
private fun LibraryItemCard(
    item: PdfLibraryItem,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val thumbnail =
        remember(item.thumbnailBytes) {
            val bitmap = BitmapFactory.decodeByteArray(item.thumbnailBytes, 0, item.thumbnailBytes.size)
            checkNotNull(bitmap) { "Stored thumbnail for ${item.fileName} is not a decodable image" }.asImageBitmap()
        }

    Card(modifier = modifier.clickable(onClick = onOpen)) {
        Box {
            Image(
                bitmap = thumbnail,
                contentDescription = item.fileName,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.7f),
            )
            IconButton(onClick = onDelete, modifier = Modifier.align(Alignment.TopEnd)) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete ${item.fileName}")
            }
        }
        Text(text = item.fileName, maxLines = 1, modifier = Modifier.padding(8.dp))
        Text(text = "${item.pageCount} pages", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
    }
}
