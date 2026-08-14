package com.beader.feature.pdfreader

import androidx.compose.runtime.Immutable

enum class ReadingMode {
    SINGLE_PAGE,
    CONTINUOUS,
}

/**
 * Exhaustive, immutable representation of everything the screen can render.
 * The Composable is a pure function of this type — it holds no state of its
 * own beyond transient UI-only concerns (e.g. pinch-zoom scale/offset,
 * scroll position).
 */
@Immutable
sealed interface PdfReaderUiState {
    data object Loading : PdfReaderUiState

    data class Error(
        val message: String,
    ) : PdfReaderUiState

    /**
     * [pages] holds every page rendered so far, keyed by index. In
     * [ReadingMode.SINGLE_PAGE] it always holds exactly one entry (the
     * current page); in [ReadingMode.CONTINUOUS] it grows as pages scroll
     * into view. Not a `data class`-default-safe field — [ByteArray] breaks
     * structural equality, so `equals`/`hashCode` are implemented by content.
     */
    data class Content(
        val readingMode: ReadingMode,
        val pageCount: Int,
        val currentPageIndex: Int,
        val pages: Map<Int, ByteArray>,
    ) : PdfReaderUiState {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Content) return false
            return readingMode == other.readingMode &&
                pageCount == other.pageCount &&
                currentPageIndex == other.currentPageIndex &&
                pages.keys == other.pages.keys &&
                pages.all { (index, bytes) -> other.pages[index]?.contentEquals(bytes) == true }
        }

        override fun hashCode(): Int {
            var result = readingMode.hashCode()
            result = 31 * result + pageCount
            result = 31 * result + currentPageIndex
            result = 31 * result + pages.entries.sumOf { (index, bytes) -> index * 31 + bytes.contentHashCode() }
            return result
        }
    }
}
