package com.beader.feature.pdfreader

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.beader.core.ui.FullScreenError
import com.beader.core.ui.FullScreenLoading

private const val MIN_ZOOM = 1f
private const val MAX_ZOOM = 5f

@Composable
fun PdfReaderRoute(
    modifier: Modifier = Modifier,
    viewModel: PdfReaderViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val pageWidthPx = with(density) { configuration.screenWidthDp.dp.roundToPx() }

    LaunchedEffect(Unit) { viewModel.onOpenDocument(pageWidthPx) }

    DisposableEffect(Unit) {
        onDispose { viewModel.onScreenClosed() }
    }

    PdfReaderScreen(
        uiState = uiState,
        onRetry = { viewModel.onRetry(pageWidthPx) },
        onNextPage = { viewModel.onNextPage(pageWidthPx) },
        onPreviousPage = { viewModel.onPreviousPage(pageWidthPx) },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PdfReaderScreen(
    uiState: PdfReaderUiState,
    onRetry: () -> Unit,
    onNextPage: () -> Unit,
    onPreviousPage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("PDF Reader") }) },
    ) { paddingValues ->
        when (uiState) {
            is PdfReaderUiState.Loading -> {
                FullScreenLoading(modifier = Modifier.padding(paddingValues))
            }

            is PdfReaderUiState.Error -> {
                FullScreenError(
                    message = uiState.message,
                    onRetry = onRetry,
                    modifier = Modifier.padding(paddingValues),
                )
            }

            is PdfReaderUiState.Success -> {
                PdfReaderContent(
                    state = uiState,
                    onNextPage = onNextPage,
                    onPreviousPage = onPreviousPage,
                    modifier = Modifier.padding(paddingValues),
                )
            }
        }
    }
}

@Composable
private fun PdfReaderContent(
    state: PdfReaderUiState.Success,
    onNextPage: () -> Unit,
    onPreviousPage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val imageBitmap =
        remember(state.pageImageBytes) {
            val bitmap = BitmapFactory.decodeByteArray(state.pageImageBytes, 0, state.pageImageBytes.size)
            checkNotNull(bitmap) { "Rendered PDF page ${state.pageIndex} is not a decodable image" }.asImageBitmap()
        }
    var scale by remember(state.pageIndex) { mutableFloatStateOf(MIN_ZOOM) }
    var offset by remember(state.pageIndex) { mutableStateOf(Offset.Zero) }

    Column(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            Image(
                bitmap = imageBitmap,
                contentDescription = "PDF page ${state.pageIndex + 1}",
                modifier =
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y,
                        ).pointerInput(state.pageIndex) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(MIN_ZOOM, MAX_ZOOM)
                                offset += pan
                            }
                        },
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextButton(onClick = onPreviousPage, enabled = state.pageIndex > 0) {
                Text("Previous")
            }
            Text("Page ${state.pageIndex + 1} of ${state.pageCount}")
            TextButton(onClick = onNextPage, enabled = state.pageIndex < state.pageCount - 1) {
                Text("Next")
            }
        }
    }
}
