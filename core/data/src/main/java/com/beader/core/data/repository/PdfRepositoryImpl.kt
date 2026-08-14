package com.beader.core.data.repository

import com.beader.core.common.result.DataResult
import com.beader.core.domain.model.PdfDocument
import com.beader.core.domain.model.PdfPage
import com.beader.core.domain.repository.PdfRepository
import com.beader.core.pdf.datasource.PdfRendererDataSource
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfRepositoryImpl
    @Inject
    constructor(
        private val pdfRendererDataSource: PdfRendererDataSource,
    ) : PdfRepository {
        override suspend fun openDocument(uri: String): DataResult<PdfDocument> =
            runCatching { pdfRendererDataSource.open(uri) }
                .fold(
                    onSuccess = { pageCount -> DataResult.Success(PdfDocument(uri = uri, pageCount = pageCount)) },
                    onFailure = { throwable ->
                        Timber.e(throwable, "Failed to open PDF document")
                        DataResult.Error(throwable)
                    },
                )

        override suspend fun loadPage(
            pageIndex: Int,
            widthPx: Int,
        ): DataResult<PdfPage> =
            runCatching { pdfRendererDataSource.renderPage(pageIndex, widthPx) }
                .fold(
                    onSuccess = { bytes -> DataResult.Success(PdfPage(pageIndex = pageIndex, imageBytes = bytes)) },
                    onFailure = { throwable ->
                        Timber.e(throwable, "Failed to render PDF page $pageIndex")
                        DataResult.Error(throwable)
                    },
                )

        override suspend fun closeDocument() {
            pdfRendererDataSource.close()
        }
    }
