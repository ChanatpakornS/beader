package com.beader.core.domain.model

/**
 * One PDF the user has imported into the library, with a thumbnail of its
 * first page. Not a `data class` because [thumbnailBytes] is a [ByteArray]
 * and would break structural `equals`/`hashCode` — both are implemented
 * explicitly by content instead.
 */
class PdfLibraryItem(
    val id: Long,
    val uri: String,
    val fileName: String,
    val pageCount: Int,
    val thumbnailBytes: ByteArray,
    val importedAtEpochMillis: Long,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PdfLibraryItem) return false
        return id == other.id &&
            uri == other.uri &&
            fileName == other.fileName &&
            pageCount == other.pageCount &&
            thumbnailBytes.contentEquals(other.thumbnailBytes) &&
            importedAtEpochMillis == other.importedAtEpochMillis
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + uri.hashCode()
        result = 31 * result + fileName.hashCode()
        result = 31 * result + pageCount
        result = 31 * result + thumbnailBytes.contentHashCode()
        result = 31 * result + importedAtEpochMillis.hashCode()
        return result
    }

    override fun toString(): String = "PdfLibraryItem(id=$id, uri=$uri, fileName=$fileName, pageCount=$pageCount)"
}
