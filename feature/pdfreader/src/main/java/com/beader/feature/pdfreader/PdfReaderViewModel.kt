package com.beader.feature.pdfreader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beader.core.common.result.DataResult
import com.beader.core.domain.model.PdfLibraryItem
import com.beader.core.domain.usecase.ClosePdfDocumentUseCase
import com.beader.core.domain.usecase.LoadPdfPageUseCase
import com.beader.core.domain.usecase.ObserveLibraryUseCase
import com.beader.core.domain.usecase.OpenPdfDocumentUseCase
import com.beader.feature.pdfreader.navigation.PdfReaderArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PdfReaderViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val openPdfDocument: OpenPdfDocumentUseCase,
        private val loadPdfPage: LoadPdfPageUseCase,
        private val closePdfDocument: ClosePdfDocumentUseCase,
        observeLibrary: ObserveLibraryUseCase,
    ) : ViewModel() {
        private val documentUri: String = PdfReaderArgs(savedStateHandle).uri

        private val _uiState = MutableStateFlow<PdfReaderUiState>(PdfReaderUiState.Loading)
        val uiState: StateFlow<PdfReaderUiState> = _uiState.asStateFlow()

        /** Drives the navigation drawer's document list. */
        val libraryItems: StateFlow<List<PdfLibraryItem>> =
            observeLibrary()
                .map { result -> (result as? DataResult.Success)?.data.orEmpty() }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                    initialValue = emptyList(),
                )

        private val _scrollToPageEvents = MutableSharedFlow<Int>(extraBufferCapacity = 1)

        /** One-off programmatic scroll requests for continuous mode (mode switch, page jump) — kept as
         * events rather than state so the list's own natural scrolling never fights a synced state value. */
        val scrollToPageEvents: SharedFlow<Int> = _scrollToPageEvents.asSharedFlow()

        private var pageCount = 0
        private var hasOpened = false

        /**
         * Opens [documentUri] and loads its first page. Safe to call more than
         * once — only the first call does work.
         */
        fun onOpenDocument(widthPx: Int) {
            if (hasOpened) return
            hasOpened = true
            viewModelScope.launch {
                _uiState.value = PdfReaderUiState.Loading
                when (val result = openPdfDocument(documentUri)) {
                    is DataResult.Success -> {
                        pageCount = result.data.pageCount
                        loadSinglePage(pageIndex = 0, widthPx = widthPx)
                    }

                    is DataResult.Error -> {
                        _uiState.value = PdfReaderUiState.Error(result.message ?: "Unable to open PDF")
                    }

                    is DataResult.Loading -> {
                        Unit
                    }
                }
            }
        }

        fun onNextPage(widthPx: Int) {
            val current = _uiState.value
            if (current is PdfReaderUiState.Content &&
                current.readingMode == ReadingMode.SINGLE_PAGE &&
                current.currentPageIndex < pageCount - 1
            ) {
                viewModelScope.launch { loadSinglePage(current.currentPageIndex + 1, widthPx) }
            }
        }

        fun onPreviousPage(widthPx: Int) {
            val current = _uiState.value
            if (current is PdfReaderUiState.Content &&
                current.readingMode == ReadingMode.SINGLE_PAGE &&
                current.currentPageIndex > 0
            ) {
                viewModelScope.launch { loadSinglePage(current.currentPageIndex - 1, widthPx) }
            }
        }

        fun onToggleReadingMode(widthPx: Int) {
            val current = _uiState.value
            if (current !is PdfReaderUiState.Content) return
            when (current.readingMode) {
                ReadingMode.SINGLE_PAGE -> {
                    _uiState.value = current.copy(readingMode = ReadingMode.CONTINUOUS)
                    _scrollToPageEvents.tryEmit(current.currentPageIndex)
                    onRequestPage(current.currentPageIndex, widthPx)
                }

                ReadingMode.CONTINUOUS -> {
                    viewModelScope.launch { loadSinglePage(current.currentPageIndex, widthPx) }
                }
            }
        }

        /** Jumps to [pageNumber] (1-based, as typed by the user), clamped to the document's range. */
        fun onJumpToPage(
            pageNumber: Int,
            widthPx: Int,
        ) {
            val current = _uiState.value
            if (current !is PdfReaderUiState.Content) return
            val targetIndex = (pageNumber - 1).coerceIn(0, current.pageCount - 1)
            when (current.readingMode) {
                ReadingMode.SINGLE_PAGE -> {
                    viewModelScope.launch { loadSinglePage(targetIndex, widthPx) }
                }

                ReadingMode.CONTINUOUS -> {
                    _uiState.value = current.copy(currentPageIndex = targetIndex)
                    _scrollToPageEvents.tryEmit(targetIndex)
                    onRequestPage(targetIndex, widthPx)
                }
            }
        }

        /**
         * Renders and caches [pageIndex] if it isn't already loaded. Continuous-mode
         * pages call this as they scroll into view.
         */
        fun onRequestPage(
            pageIndex: Int,
            widthPx: Int,
        ) {
            val current = _uiState.value
            if (current !is PdfReaderUiState.Content || current.readingMode != ReadingMode.CONTINUOUS) return
            if (current.pages.containsKey(pageIndex)) return
            viewModelScope.launch {
                when (val result = loadPdfPage(pageIndex, widthPx)) {
                    is DataResult.Success -> {
                        val latest = _uiState.value
                        if (latest is PdfReaderUiState.Content && latest.readingMode == ReadingMode.CONTINUOUS) {
                            _uiState.value = latest.copy(pages = latest.pages + (pageIndex to result.data.imageBytes))
                        }
                    }

                    is DataResult.Error, is DataResult.Loading -> {
                        Unit
                    }
                }
            }
        }

        fun onRetry(widthPx: Int) {
            hasOpened = false
            onOpenDocument(widthPx)
        }

        fun onScreenClosed() {
            viewModelScope.launch { closePdfDocument() }
        }

        private suspend fun loadSinglePage(
            pageIndex: Int,
            widthPx: Int,
        ) {
            when (val result = loadPdfPage(pageIndex, widthPx)) {
                is DataResult.Success -> {
                    _uiState.value =
                        PdfReaderUiState.Content(
                            readingMode = ReadingMode.SINGLE_PAGE,
                            pageCount = pageCount,
                            currentPageIndex = pageIndex,
                            pages = mapOf(pageIndex to result.data.imageBytes),
                        )
                }

                is DataResult.Error -> {
                    _uiState.value = PdfReaderUiState.Error(result.message ?: "Unable to render page")
                }

                is DataResult.Loading -> {
                    Unit
                }
            }
        }

        private companion object {
            const val STOP_TIMEOUT_MILLIS = 5_000L
        }
    }
