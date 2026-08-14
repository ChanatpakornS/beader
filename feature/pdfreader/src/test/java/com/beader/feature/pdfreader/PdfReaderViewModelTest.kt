package com.beader.feature.pdfreader

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.beader.core.common.result.DataResult
import com.beader.core.domain.model.PdfDocument
import com.beader.core.domain.model.PdfPage
import com.beader.core.domain.usecase.ClosePdfDocumentUseCase
import com.beader.core.domain.usecase.LoadPdfPageUseCase
import com.beader.core.domain.usecase.OpenPdfDocumentUseCase
import com.beader.core.testing.MainDispatcherRule
import com.beader.core.testing.repository.FakePdfRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class PdfReaderViewModelTest {
    companion object {
        @JvmField
        @RegisterExtension
        val mainDispatcherRule = MainDispatcherRule()

        private const val DOCUMENT_URI = "content://documents/1"
        private const val WIDTH_PX = 200
    }

    private lateinit var fakeRepository: FakePdfRepository
    private lateinit var viewModel: PdfReaderViewModel

    @BeforeEach
    fun setUp() {
        fakeRepository = FakePdfRepository()
        viewModel =
            PdfReaderViewModel(
                savedStateHandle = SavedStateHandle(mapOf("uri" to DOCUMENT_URI)),
                openPdfDocument = OpenPdfDocumentUseCase(fakeRepository),
                loadPdfPage = LoadPdfPageUseCase(fakeRepository),
                closePdfDocument = ClosePdfDocumentUseCase(fakeRepository),
            )
    }

    @Test
    fun `onOpenDocument loads the first page on success`() =
        runTest {
            fakeRepository.nextOpenResult = DataResult.Success(PdfDocument(uri = DOCUMENT_URI, pageCount = 3))
            fakeRepository.nextPageResult = DataResult.Success(PdfPage(pageIndex = 0, imageBytes = byteArrayOf(1)))

            viewModel.uiState.test {
                assertTrue(awaitItem() is PdfReaderUiState.Loading)

                viewModel.onOpenDocument(WIDTH_PX)

                val loaded = awaitItem() as PdfReaderUiState.Success
                assertEquals(0, loaded.pageIndex)
                assertEquals(3, loaded.pageCount)
            }
        }

    @Test
    fun `onOpenDocument surfaces an Error state when opening fails`() =
        runTest {
            fakeRepository.nextOpenResult = DataResult.Error(IllegalStateException("corrupt"))

            viewModel.uiState.test {
                assertTrue(awaitItem() is PdfReaderUiState.Loading)

                viewModel.onOpenDocument(WIDTH_PX)

                assertTrue(awaitItem() is PdfReaderUiState.Error)
            }
        }

    @Test
    fun `onNextPage does nothing at the last page`() =
        runTest {
            fakeRepository.nextOpenResult = DataResult.Success(PdfDocument(uri = DOCUMENT_URI, pageCount = 1))
            fakeRepository.nextPageResult = DataResult.Success(PdfPage(pageIndex = 0, imageBytes = byteArrayOf(1)))

            viewModel.uiState.test {
                skipItems(1) // Loading
                viewModel.onOpenDocument(WIDTH_PX)
                skipItems(1) // Success page 0

                viewModel.onNextPage(WIDTH_PX)

                expectNoEvents()
            }
        }
}
