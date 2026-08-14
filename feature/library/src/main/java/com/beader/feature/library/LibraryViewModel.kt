package com.beader.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beader.core.common.result.DataResult
import com.beader.core.domain.usecase.DeleteLibraryItemUseCase
import com.beader.core.domain.usecase.ImportPdfUseCase
import com.beader.core.domain.usecase.ObserveLibraryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel
    @Inject
    constructor(
        observeLibrary: ObserveLibraryUseCase,
        private val importPdf: ImportPdfUseCase,
        private val deleteLibraryItem: DeleteLibraryItemUseCase,
    ) : ViewModel() {
        val uiState: StateFlow<LibraryUiState> =
            observeLibrary()
                .map { result ->
                    when (result) {
                        is DataResult.Loading -> LibraryUiState.Loading
                        is DataResult.Success -> LibraryUiState.Success(result.data)
                        is DataResult.Error -> LibraryUiState.Error(result.message ?: "Something went wrong")
                    }
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                    initialValue = LibraryUiState.Loading,
                )

        private val _importErrors = MutableSharedFlow<String>()

        /** One-off import failures, surfaced as a snackbar rather than replacing [uiState]. */
        val importErrors: SharedFlow<String> = _importErrors.asSharedFlow()

        fun onImportDocument(
            uri: String,
            fileName: String,
            thumbnailWidthPx: Int,
        ) {
            viewModelScope.launch {
                val result = importPdf(uri, fileName, thumbnailWidthPx)
                if (result is DataResult.Error) {
                    _importErrors.emit(result.message ?: "Unable to import PDF")
                }
            }
        }

        fun onDeleteDocument(id: Long) {
            viewModelScope.launch {
                deleteLibraryItem(id)
            }
        }

        private companion object {
            const val STOP_TIMEOUT_MILLIS = 5_000L
        }
    }
