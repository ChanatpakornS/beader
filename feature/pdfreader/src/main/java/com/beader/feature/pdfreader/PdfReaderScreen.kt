package com.beader.feature.pdfreader

import android.graphics.BitmapFactory
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.beader.core.domain.model.PdfLibraryItem
import com.beader.core.ui.FullScreenError
import com.beader.core.ui.FullScreenLoading
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

private const val MIN_ZOOM = 1f
private const val MAX_ZOOM = 5f

@Composable
fun PdfReaderRoute(
    modifier: Modifier = Modifier,
    onNavigateToDocument: (uri: String) -> Unit = {},
    onNavigateToLibrary: () -> Unit = {},
    viewModel: PdfReaderViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val libraryItems by viewModel.libraryItems.collectAsState()
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val pageWidthPx = with(density) { configuration.screenWidthDp.dp.roundToPx() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) { viewModel.onOpenDocument(pageWidthPx) }

    DisposableEffect(Unit) {
        onDispose { viewModel.onScreenClosed() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                PdfReaderDrawerContent(
                    libraryItems = libraryItems,
                    onOpenDocument = { uri ->
                        coroutineScope.launch { drawerState.close() }
                        onNavigateToDocument(uri)
                    },
                    onOpenLibrary = {
                        coroutineScope.launch { drawerState.close() }
                        onNavigateToLibrary()
                    },
                )
            }
        },
        modifier = modifier,
    ) {
        PdfReaderScreen(
            uiState = uiState,
            scrollToPageEvents = viewModel.scrollToPageEvents,
            actions =
                PdfReaderActions(
                    onRetry = { viewModel.onRetry(pageWidthPx) },
                    onNextPage = { viewModel.onNextPage(pageWidthPx) },
                    onPreviousPage = { viewModel.onPreviousPage(pageWidthPx) },
                    onToggleReadingMode = { viewModel.onToggleReadingMode(pageWidthPx) },
                    onJumpToPage = { page -> viewModel.onJumpToPage(page, pageWidthPx) },
                    onRequestPage = { index -> viewModel.onRequestPage(index, pageWidthPx) },
                    onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                ),
        )
    }
}

/** Groups this screen's action callbacks so the top-level Composable stays under the parameter-count lint threshold. */
@Immutable
data class PdfReaderActions(
    val onRetry: () -> Unit,
    val onNextPage: () -> Unit,
    val onPreviousPage: () -> Unit,
    val onToggleReadingMode: () -> Unit,
    val onJumpToPage: (pageNumber: Int) -> Unit,
    val onRequestPage: (pageIndex: Int) -> Unit,
    val onOpenDrawer: () -> Unit,
)

@Composable
private fun PdfReaderDrawerContent(
    libraryItems: List<PdfLibraryItem>,
    onOpenDocument: (String) -> Unit,
    onOpenLibrary: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxHeight().padding(16.dp)) {
        Text(text = "Library", style = MaterialTheme.typography.titleMedium)
        TextButton(onClick = onOpenLibrary, modifier = Modifier.fillMaxWidth()) {
            Text("Back to Library")
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        LazyColumn {
            items(items = libraryItems, key = { it.id }) { item ->
                DrawerLibraryItemRow(item = item, onClick = { onOpenDocument(item.uri) })
            }
        }
    }
}

@Composable
private fun DrawerLibraryItemRow(
    item: PdfLibraryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val thumbnail =
        remember(item.thumbnailBytes) {
            val bitmap = BitmapFactory.decodeByteArray(item.thumbnailBytes, 0, item.thumbnailBytes.size)
            checkNotNull(bitmap) { "Stored thumbnail for ${item.fileName} is not a decodable image" }.asImageBitmap()
        }

    Row(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            bitmap = thumbnail,
            contentDescription = null,
            modifier = Modifier.width(48.dp).aspectRatio(0.7f),
        )
        Text(text = item.fileName, maxLines = 1, modifier = Modifier.padding(start = 8.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
internal fun PdfReaderScreen(
    uiState: PdfReaderUiState,
    scrollToPageEvents: Flow<Int>,
    actions: PdfReaderActions,
    modifier: Modifier = Modifier,
) {
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(scrollToPageEvents) {
        scrollToPageEvents.collect { pageIndex ->
            coroutineScope.launch { lazyListState.animateScrollToItem(pageIndex) }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            PdfReaderTopBar(
                readingMode = (uiState as? PdfReaderUiState.Content)?.readingMode,
                onToggleReadingMode = actions.onToggleReadingMode,
                onOpenDrawer = actions.onOpenDrawer,
            )
        },
    ) { paddingValues ->
        when (uiState) {
            is PdfReaderUiState.Loading -> {
                FullScreenLoading(modifier = Modifier.padding(paddingValues))
            }

            is PdfReaderUiState.Error -> {
                FullScreenError(
                    message = uiState.message,
                    onRetry = actions.onRetry,
                    modifier = Modifier.padding(paddingValues),
                )
            }

            is PdfReaderUiState.Content -> {
                when (uiState.readingMode) {
                    ReadingMode.SINGLE_PAGE -> {
                        SinglePageContent(
                            state = uiState,
                            onNextPage = actions.onNextPage,
                            onPreviousPage = actions.onPreviousPage,
                            onJumpToPage = actions.onJumpToPage,
                            modifier = Modifier.padding(paddingValues),
                        )
                    }

                    ReadingMode.CONTINUOUS -> {
                        ContinuousContent(
                            state = uiState,
                            lazyListState = lazyListState,
                            onRequestPage = actions.onRequestPage,
                            onJumpToPage = actions.onJumpToPage,
                            modifier = Modifier.padding(paddingValues),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PdfReaderTopBar(
    readingMode: ReadingMode?,
    onToggleReadingMode: () -> Unit,
    onOpenDrawer: () -> Unit,
) {
    TopAppBar(
        title = { Text("PDF Reader") },
        navigationIcon = {
            IconButton(onClick = onOpenDrawer) {
                Icon(Icons.Filled.Menu, contentDescription = "Open library navigation")
            }
        },
        actions = {
            if (readingMode != null) {
                TextButton(onClick = onToggleReadingMode) {
                    Text(if (readingMode == ReadingMode.SINGLE_PAGE) "Continuous" else "Single page")
                }
            }
        },
    )
}

@Composable
private fun SinglePageContent(
    state: PdfReaderUiState.Content,
    onNextPage: () -> Unit,
    onPreviousPage: () -> Unit,
    onJumpToPage: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        val pageBytes = state.pages[state.currentPageIndex]
        if (pageBytes != null) {
            ZoomablePdfPage(
                imageBytes = pageBytes,
                pageIndex = state.currentPageIndex,
                modifier = Modifier.fillMaxWidth().weight(1f),
            )
        } else {
            FullScreenLoading(modifier = Modifier.fillMaxWidth().weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onPreviousPage, enabled = state.currentPageIndex > 0) {
                Text("Previous")
            }
            Text("Page ${state.currentPageIndex + 1} of ${state.pageCount}")
            TextButton(onClick = onNextPage, enabled = state.currentPageIndex < state.pageCount - 1) {
                Text("Next")
            }
        }
        PageJumpField(onJumpToPage = onJumpToPage, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ContinuousContent(
    state: PdfReaderUiState.Content,
    lazyListState: LazyListState,
    onRequestPage: (Int) -> Unit,
    onJumpToPage: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val visiblePage = (lazyListState.firstVisibleItemIndex + 1).coerceIn(1, state.pageCount)
    val currentOnRequestPage by rememberUpdatedState(onRequestPage)

    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = lazyListState,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = lazyListState),
            modifier = Modifier.fillMaxWidth().weight(1f),
        ) {
            items(count = state.pageCount, key = { it }) { index ->
                LaunchedEffect(index) { currentOnRequestPage(index) }
                val pageBytes = state.pages[index]
                if (pageBytes != null) {
                    ZoomablePdfPage(
                        imageBytes = pageBytes,
                        pageIndex = index,
                        modifier = Modifier.fillParentMaxSize(),
                    )
                } else {
                    FullScreenLoading(modifier = Modifier.fillParentMaxSize())
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text("Page $visiblePage of ${state.pageCount}")
        }
        PageJumpField(onJumpToPage = onJumpToPage, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
    }
}

@Composable
private fun PageJumpField(
    onJumpToPage: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var pageInput by remember { mutableStateOf("") }

    Row(
        modifier = modifier.padding(bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        OutlinedTextField(
            value = pageInput,
            onValueChange = { pageInput = it.filter(Char::isDigit) },
            label = { Text("Go to page") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.width(120.dp),
        )
        Button(
            onClick = { pageInput.toIntOrNull()?.let(onJumpToPage) },
            modifier = Modifier.padding(start = 8.dp),
        ) {
            Text("Go")
        }
    }
}

@Composable
private fun ZoomablePdfPage(
    imageBytes: ByteArray,
    pageIndex: Int,
    modifier: Modifier = Modifier,
) {
    val imageBitmap =
        remember(imageBytes) {
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            checkNotNull(bitmap) { "Rendered PDF page $pageIndex is not a decodable image" }.asImageBitmap()
        }
    var scale by remember(pageIndex) { mutableFloatStateOf(MIN_ZOOM) }
    var offset by remember(pageIndex) { mutableStateOf(Offset.Zero) }

    Image(
        bitmap = imageBitmap,
        contentDescription = "PDF page ${pageIndex + 1}",
        modifier =
            modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y,
                ).pointerInput(pageIndex) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(MIN_ZOOM, MAX_ZOOM)
                        offset += pan
                    }
                },
    )
}
