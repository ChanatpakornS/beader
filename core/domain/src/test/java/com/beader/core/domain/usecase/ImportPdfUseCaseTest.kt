package com.beader.core.domain.usecase

import com.beader.core.common.result.DataResult
import com.beader.core.domain.model.PdfDocument
import com.beader.core.domain.model.PdfLibraryItem
import com.beader.core.domain.model.PdfPage
import com.beader.core.domain.repository.PdfLibraryRepository
import com.beader.core.domain.repository.PdfRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ImportPdfUseCaseTest {
    private val pdfRepository: PdfRepository = mockk()
    private val pdfLibraryRepository: PdfLibraryRepository = mockk()
    private val importPdf = ImportPdfUseCase(pdfRepository, pdfLibraryRepository)

    @Test
    fun `invoke opens the document, renders a thumbnail, saves it, then closes the document`() =
        runTest {
            val uri = "content://documents/1"
            val document = PdfDocument(uri = uri, pageCount = 5)
            val thumbnail = PdfPage(pageIndex = 0, imageBytes = byteArrayOf(1, 2, 3))
            val savedItem =
                PdfLibraryItem(
                    id = 1L,
                    uri = uri,
                    fileName = "report.pdf",
                    pageCount = 5,
                    thumbnailBytes = thumbnail.imageBytes,
                    importedAtEpochMillis = 0L,
                )
            coEvery { pdfRepository.openDocument(uri) } returns DataResult.Success(document)
            coEvery { pdfRepository.loadPage(0, 200) } returns DataResult.Success(thumbnail)
            coEvery {
                pdfLibraryRepository.saveImportedDocument(uri, "report.pdf", 5, thumbnail.imageBytes)
            } returns DataResult.Success(savedItem)
            coEvery { pdfRepository.closeDocument() } returns Unit

            val result = importPdf(uri, "report.pdf", thumbnailWidthPx = 200)

            assertTrue(result is DataResult.Success)
            assertEquals(savedItem, (result as DataResult.Success).data)
            coVerify(exactly = 1) { pdfRepository.closeDocument() }
        }

    @Test
    fun `invoke returns the error and never saves when opening the document fails`() =
        runTest {
            val uri = "content://documents/broken"
            val failure = DataResult.Error(IllegalStateException("corrupt file"))
            coEvery { pdfRepository.openDocument(uri) } returns failure

            val result = importPdf(uri, "broken.pdf", thumbnailWidthPx = 200)

            assertEquals(failure, result)
            coVerify(exactly = 0) { pdfLibraryRepository.saveImportedDocument(any(), any(), any(), any()) }
        }
}
