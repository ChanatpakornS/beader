package com.beader.core.domain.usecase

import com.beader.core.common.result.DataResult
import com.beader.core.domain.model.PdfLibraryItem
import com.beader.core.domain.repository.PdfLibraryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * One use case per user intent. ViewModels depend on use cases, never on
 * repositories directly — this keeps business rules out of the
 * presentation layer and independently testable.
 */
class ObserveLibraryUseCase
    @Inject
    constructor(
        private val pdfLibraryRepository: PdfLibraryRepository,
    ) {
        operator fun invoke(): Flow<DataResult<List<PdfLibraryItem>>> = pdfLibraryRepository.observeLibrary()
    }
