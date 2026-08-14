package com.beader.core.domain.model

/**
 * A single rendered PDF page as PNG-encoded [imageBytes]. Not a `data class`
 * because [ByteArray] breaks structural `equals`/`hashCode` — both are
 * implemented explicitly by content instead.
 */
class PdfPage(
    val pageIndex: Int,
    val imageBytes: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PdfPage) return false
        return pageIndex == other.pageIndex && imageBytes.contentEquals(other.imageBytes)
    }

    override fun hashCode(): Int {
        var result = pageIndex
        result = 31 * result + imageBytes.contentHashCode()
        return result
    }

    override fun toString(): String = "PdfPage(pageIndex=$pageIndex, imageBytes=${imageBytes.size} bytes)"
}
