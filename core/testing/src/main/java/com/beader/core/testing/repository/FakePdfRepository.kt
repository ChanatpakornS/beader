package com.beader.core.testing.repository

import com.beader.core.common.result.DataResult
import com.beader.core.domain.model.PdfDocument
import com.beader.core.domain.model.PdfPage
import com.beader.core.domain.repository.PdfRepository

/**
 * In-memory [PdfRepository] fake for ViewModel tests. Tests drive behavior
 * by setting [nextOpenResult] / [nextPageResult] before invoking the
 * ViewModel, instead of mocking suspend functions by hand.
 */
class FakePdfRepository : PdfRepository {
    var nextOpenResult: DataResult<PdfDocument> = DataResult.Loading
    var nextPageResult: DataResult<PdfPage> = DataResult.Loading
    var closeCallCount: Int = 0
        private set

    override suspend fun openDocument(uri: String): DataResult<PdfDocument> = nextOpenResult

    override suspend fun loadPage(
        pageIndex: Int,
        widthPx: Int,
    ): DataResult<PdfPage> = nextPageResult

    override suspend fun closeDocument() {
        closeCallCount++
    }
}
