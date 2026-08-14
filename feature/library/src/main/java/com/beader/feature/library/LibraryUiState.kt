package com.beader.feature.library

import androidx.compose.runtime.Immutable
import com.beader.core.domain.model.PdfLibraryItem

/**
 * Exhaustive, immutable representation of everything the screen can render.
 * The Composable is a pure function of this type — it holds no state of its
 * own beyond transient UI-only concerns (e.g. scroll position).
 */
@Immutable
sealed interface LibraryUiState {
    data object Loading : LibraryUiState

    data class Success(
        val items: List<PdfLibraryItem>,
    ) : LibraryUiState

    data class Error(
        val message: String,
    ) : LibraryUiState
}
