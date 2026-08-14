package com.beader.core.domain.repository

import com.beader.core.common.result.DataResult
import com.beader.core.domain.model.PdfLibraryItem
import kotlinx.coroutines.flow.Flow

/**
 * Domain-owned contract for the persisted list of imported PDFs. `:core:data`
 * provides the implementation, backed by Room, and is bound to this
 * interface via Hilt (`@Binds`).
 */
interface PdfLibraryRepository {
    fun observeLibrary(): Flow<DataResult<List<PdfLibraryItem>>>

    suspend fun saveImportedDocument(
        uri: String,
        fileName: String,
        pageCount: Int,
        thumbnailBytes: ByteArray,
    ): DataResult<PdfLibraryItem>

    suspend fun deleteDocument(id: Long)
}
