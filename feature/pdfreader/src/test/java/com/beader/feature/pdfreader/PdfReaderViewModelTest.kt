package com.beader.feature.pdfreader

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.beader.core.common.result.DataResult
import com.beader.core.domain.model.PdfDocument
import com.beader.core.domain.model.PdfPage
import com.beader.core.domain.usecase.ClosePdfDocumentUseCase
import com.beader.core.domain.usecase.LoadPdfPageUseCase
import com.beader.core.domain.usecase.ObserveLibraryUseCase
import com.beader.core.domain.usecase.OpenPdfDocumentUseCase
import com.beader.core.testing.MainDispatcherRule
import com.beader.core.testing.repository.FakePdfLibraryRepository
import com.beader.core.testing.repository.FakePdfRepository
import com.beader.feature.pdfreader.navigation.encodeUriArg
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
    private lateinit var fakeLibraryRepository: FakePdfLibraryRepository
    private lateinit var viewModel: PdfReaderViewModel

    @BeforeEach
    fun setUp() {
        fakeRepository = FakePdfRepository()
        fakeLibraryRepository = FakePdfLibraryRepository()
        viewModel =
            PdfReaderViewModel(
                savedStateHandle = SavedStateHandle(mapOf("uri" to encodeUriArg(DOCUMENT_URI))),
                openPdfDocument = OpenPdfDocumentUseCase(fakeRepository),
                loadPdfPage = LoadPdfPageUseCase(fakeRepository),
                closePdfDocument = ClosePdfDocumentUseCase(fakeRepository),
                observeLibrary = ObserveLibraryUseCase(fakeLibraryRepository),
            )
    }

    private fun openDocument(pageCount: Int = 3) {
        fakeRepository.nextOpenResult = DataResult.Success(PdfDocument(uri = DOCUMENT_URI, pageCount = pageCount))
    }

    @Test
    fun `onOpenDocument loads the first page in single-page mode`() =
        runTest {
            openDocument(pageCount = 3)
            fakeRepository.nextPageResult = DataResult.Success(PdfPage(pageIndex = 0, imageBytes = byteArrayOf(1)))

            viewModel.uiState.test {
                assertTrue(awaitItem() is PdfReaderUiState.Loading)

                viewModel.onOpenDocument(WIDTH_PX)

                val loaded = awaitItem() as PdfReaderUiState.Content
                assertEquals(ReadingMode.SINGLE_PAGE, loaded.readingMode)
                assertEquals(0, loaded.currentPageIndex)
                assertEquals(3, loaded.pageCount)
            }
        }

    @Test
    fun `onToggleReadingMode switches to continuous and keeps the current page`() =
        runTest {
            openDocument(pageCount = 5)
            fakeRepository.nextPageResult = DataResult.Success(PdfPage(pageIndex = 0, imageBytes = byteArrayOf(1)))

            viewModel.uiState.test {
                skipItems(1) // Loading
                viewModel.onOpenDocument(WIDTH_PX)
                skipItems(1) // single-page Content

                viewModel.onToggleReadingMode(WIDTH_PX)

                val continuous = awaitItem() as PdfReaderUiState.Content
                assertEquals(ReadingMode.CONTINUOUS, continuous.readingMode)
                assertEquals(0, continuous.currentPageIndex)
            }
        }

    @Test
    fun `onJumpToPage clamps to the document's page range`() =
        runTest {
            openDocument(pageCount = 3)
            fakeRepository.nextPageResult = DataResult.Success(PdfPage(pageIndex = 0, imageBytes = byteArrayOf(1)))

            viewModel.uiState.test {
                skipItems(1) // Loading
                viewModel.onOpenDocument(WIDTH_PX)
                skipItems(1) // page 0

                viewModel.onJumpToPage(99, WIDTH_PX)

                val jumped = awaitItem() as PdfReaderUiState.Content
                assertEquals(2, jumped.currentPageIndex) // clamped to last page (index 2 of 3)
            }
        }

    @Test
    fun `onRequestPage does not refetch an already-cached page`() =
        runTest {
            openDocument(pageCount = 3)
            fakeRepository.nextPageResult = DataResult.Success(PdfPage(pageIndex = 0, imageBytes = byteArrayOf(1)))

            viewModel.uiState.test {
                skipItems(1) // Loading
                viewModel.onOpenDocument(WIDTH_PX)
                skipItems(1) // single-page Content
                viewModel.onToggleReadingMode(WIDTH_PX)
                skipItems(1) // continuous Content, page 0 already cached from single-page mode

                viewModel.onRequestPage(0, WIDTH_PX)

                expectNoEvents()
            }
        }

    @Test
    fun `libraryItems reflects the library repository`() =
        runTest {
            viewModel.libraryItems.test {
                assertTrue(awaitItem().isEmpty())
            }
        }
}
