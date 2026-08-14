package com.beader.core.domain.usecase

import com.beader.core.common.result.DataResult
import com.beader.core.domain.model.PdfDocument
import com.beader.core.domain.repository.PdfRepository
import javax.inject.Inject

/**
 * One use case per user intent. ViewModels depend on use cases, never on
 * repositories directly — this keeps business rules out of the
 * presentation layer and independently testable.
 */
class OpenPdfDocumentUseCase
    @Inject
    constructor(
        private val pdfRepository: PdfRepository,
    ) {
        suspend operator fun invoke(uri: String): DataResult<PdfDocument> = pdfRepository.openDocument(uri)
    }
