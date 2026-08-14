package com.beader.core.testing.repository

import com.beader.core.common.result.DataResult
import com.beader.core.domain.model.PdfLibraryItem
import com.beader.core.domain.repository.PdfLibraryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory [PdfLibraryRepository] fake for ViewModel tests. Tests drive
 * state through [emit] / [emitError] instead of mocking Flow emissions by
 * hand.
 */
class FakePdfLibraryRepository : PdfLibraryRepository {
    private val state = MutableStateFlow<DataResult<List<PdfLibraryItem>>>(DataResult.Loading)
    private val items = mutableListOf<PdfLibraryItem>()
    private var nextId = 1L

    var nextSaveResult: DataResult<PdfLibraryItem>? = null

    fun emit(newItems: List<PdfLibraryItem>) {
        items.clear()
        items.addAll(newItems)
        state.value = DataResult.Success(items.toList())
    }

    fun emitError(throwable: Throwable) {
        state.value = DataResult.Error(throwable)
    }

    override fun observeLibrary() = state.asStateFlow()

    override suspend fun saveImportedDocument(
        uri: String,
        fileName: String,
        pageCount: Int,
        thumbnailBytes: ByteArray,
    ): DataResult<PdfLibraryItem> {
        nextSaveResult?.let { return it }
        val item =
            PdfLibraryItem(
                id = nextId++,
                uri = uri,
                fileName = fileName,
                pageCount = pageCount,
                thumbnailBytes = thumbnailBytes,
                importedAtEpochMillis = 0L,
            )
        items.add(item)
        state.value = DataResult.Success(items.toList())
        return DataResult.Success(item)
    }

    override suspend fun deleteDocument(id: Long) {
        items.removeAll { it.id == id }
        state.value = DataResult.Success(items.toList())
    }
}
