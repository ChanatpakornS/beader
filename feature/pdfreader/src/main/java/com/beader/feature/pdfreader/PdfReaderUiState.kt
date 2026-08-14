package com.beader.feature.pdfreader

import androidx.compose.runtime.Immutable

/**
 * Exhaustive, immutable representation of everything the screen can render.
 * The Composable is a pure function of this type — it holds no state of its
 * own beyond transient UI-only concerns (e.g. pinch-zoom scale/offset).
 */
@Immutable
sealed interface PdfReaderUiState {
    data object Loading : PdfReaderUiState

    data class Success(
        val pageIndex: Int,
        val pageCount: Int,
        val pageImageBytes: ByteArray,
    ) : PdfReaderUiState {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Success) return false
            return pageIndex == other.pageIndex &&
                pageCount == other.pageCount &&
                pageImageBytes.contentEquals(other.pageImageBytes)
        }

        override fun hashCode(): Int {
            var result = pageIndex
            result = 31 * result + pageCount
            result = 31 * result + pageImageBytes.contentHashCode()
            return result
        }
    }

    data class Error(
        val message: String,
    ) : PdfReaderUiState
}
