package com.beader.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One row per PDF the user has imported into the library. [thumbnailBytes]
 * is a PNG-encoded render of the document's first page, stored inline as a
 * BLOB — small enough at thumbnail resolution that a separate file store
 * isn't worth the complexity.
 */
@Entity(tableName = "imported_pdfs")
data class ImportedPdfEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uri: String,
    val fileName: String,
    val pageCount: Int,
    val thumbnailBytes: ByteArray,
    val importedAtEpochMillis: Long,
)
