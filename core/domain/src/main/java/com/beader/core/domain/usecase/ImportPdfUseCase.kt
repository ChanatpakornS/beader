package com.beader.core.domain.usecase

import com.beader.core.common.result.DataResult
import com.beader.core.common.result.flatMap
import com.beader.core.domain.model.PdfLibraryItem
import com.beader.core.domain.repository.PdfLibraryRepository
import com.beader.core.domain.repository.PdfRepository
import javax.inject.Inject

/**
 * Opens [uri] to read its page count and render a first-page thumbnail, then
 * persists it to the library. Coordinates two repositories — rendering
 * ([PdfRepository]) and persistence ([PdfLibraryRepository]) — because
 * "import" is a single user intent spanning both concerns.
 */
class ImportPdfUseCase
    @Inject
    constructor(
        private val pdfRepository: PdfRepository,
        private val pdfLibraryRepository: PdfLibraryRepository,
    ) {
        suspend operator fun invoke(
            uri: String,
            fileName: String,
            thumbnailWidthPx: Int,
        ): DataResult<PdfLibraryItem> =
            pdfRepository.openDocument(uri).flatMap { document ->
                pdfRepository.loadPage(pageIndex = 0, widthPx = thumbnailWidthPx).flatMap { thumbnail ->
                    val saveResult =
                        pdfLibraryRepository.saveImportedDocument(
                            uri = uri,
                            fileName = fileName,
                            pageCount = document.pageCount,
                            thumbnailBytes = thumbnail.imageBytes,
                        )
                    pdfRepository.closeDocument()
                    saveResult
                }
            }
    }
