package com.beader.feature.pdfreader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beader.core.common.result.DataResult
import com.beader.core.domain.usecase.ClosePdfDocumentUseCase
import com.beader.core.domain.usecase.LoadPdfPageUseCase
import com.beader.core.domain.usecase.OpenPdfDocumentUseCase
import com.beader.feature.pdfreader.navigation.PdfReaderArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    ) : ViewModel() {
        private val documentUri: String = PdfReaderArgs(savedStateHandle).uri

        private val _uiState = MutableStateFlow<PdfReaderUiState>(PdfReaderUiState.Loading)
        val uiState: StateFlow<PdfReaderUiState> = _uiState.asStateFlow()

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
                        loadPage(pageIndex = 0, widthPx = widthPx)
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
            if (current is PdfReaderUiState.Success && current.pageIndex < pageCount - 1) {
                viewModelScope.launch { loadPage(current.pageIndex + 1, widthPx) }
            }
        }

        fun onPreviousPage(widthPx: Int) {
            val current = _uiState.value
            if (current is PdfReaderUiState.Success && current.pageIndex > 0) {
                viewModelScope.launch { loadPage(current.pageIndex - 1, widthPx) }
            }
        }

        fun onRetry(widthPx: Int) {
            hasOpened = false
            onOpenDocument(widthPx)
        }

        fun onScreenClosed() {
            viewModelScope.launch { closePdfDocument() }
        }

        private suspend fun loadPage(
            pageIndex: Int,
            widthPx: Int,
        ) {
            when (val result = loadPdfPage(pageIndex, widthPx)) {
                is DataResult.Success -> {
                    _uiState.value =
                        PdfReaderUiState.Success(
                            pageIndex = pageIndex,
                            pageCount = pageCount,
                            pageImageBytes = result.data.imageBytes,
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
    }
