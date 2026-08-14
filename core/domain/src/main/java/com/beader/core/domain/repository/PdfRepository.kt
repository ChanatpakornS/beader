package com.beader.core.domain.repository

import com.beader.core.common.result.DataResult
import com.beader.core.domain.model.PdfDocument
import com.beader.core.domain.model.PdfPage

/**
 * Domain-owned contract. `:core:data` provides the implementation, backed by
 * `:core:pdf`'s platform PDF renderer, and is bound to this interface via
 * Hilt (`@Binds`) so this module never depends on `android.graphics.pdf`.
 */
interface PdfRepository {
    suspend fun openDocument(uri: String): DataResult<PdfDocument>

    suspend fun loadPage(
        pageIndex: Int,
        widthPx: Int,
    ): DataResult<PdfPage>

    suspend fun closeDocument()
}
