package com.beader.core.domain.usecase

import com.beader.core.domain.repository.PdfLibraryRepository
import javax.inject.Inject

/**
 * One use case per user intent. ViewModels depend on use cases, never on
 * repositories directly — this keeps business rules out of the
 * presentation layer and independently testable.
 */
class DeleteLibraryItemUseCase
    @Inject
    constructor(
        private val pdfLibraryRepository: PdfLibraryRepository,
    ) {
        suspend operator fun invoke(id: Long) = pdfLibraryRepository.deleteDocument(id)
    }
