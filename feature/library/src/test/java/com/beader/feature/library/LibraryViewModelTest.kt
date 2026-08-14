package com.beader.feature.library

import app.cash.turbine.test
import com.beader.core.common.result.DataResult
import com.beader.core.domain.model.PdfLibraryItem
import com.beader.core.domain.usecase.DeleteLibraryItemUseCase
import com.beader.core.domain.usecase.ImportPdfUseCase
import com.beader.core.domain.usecase.ObserveLibraryUseCase
import com.beader.core.testing.MainDispatcherRule
import com.beader.core.testing.repository.FakePdfLibraryRepository
import com.beader.core.testing.repository.FakePdfRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class LibraryViewModelTest {
    companion object {
        @JvmField
        @RegisterExtension
        val mainDispatcherRule = MainDispatcherRule()
    }

    private lateinit var fakePdfRepository: FakePdfRepository
    private lateinit var fakeLibraryRepository: FakePdfLibraryRepository
    private lateinit var viewModel: LibraryViewModel

    @BeforeEach
    fun setUp() {
        fakePdfRepository = FakePdfRepository()
        fakeLibraryRepository = FakePdfLibraryRepository()
        viewModel =
            LibraryViewModel(
                observeLibrary = ObserveLibraryUseCase(fakeLibraryRepository),
                importPdf = ImportPdfUseCase(fakePdfRepository, fakeLibraryRepository),
                deleteLibraryItem = DeleteLibraryItemUseCase(fakeLibraryRepository),
            )
    }

    @Test
    fun `uiState starts as Loading before the repository emits`() =
        runTest {
            viewModel.uiState.test {
                assertTrue(awaitItem() is LibraryUiState.Loading)
            }
        }

    @Test
    fun `uiState reflects library items once loaded`() =
        runTest {
            viewModel.uiState.test {
                assertTrue(awaitItem() is LibraryUiState.Loading)

                fakeLibraryRepository.emit(
                    listOf(
                        PdfLibraryItem(
                            id = 1L,
                            uri = "content://1",
                            fileName = "a.pdf",
                            pageCount = 3,
                            thumbnailBytes = byteArrayOf(1),
                            importedAtEpochMillis = 0L,
                        ),
                    ),
                )

                val loaded = awaitItem() as LibraryUiState.Success
                assertEquals(1, loaded.items.size)
            }
        }

    @Test
    fun `onDeleteDocument removes the item from the library`() =
        runTest {
            viewModel.uiState.test {
                skipItems(1) // Loading

                val item =
                    PdfLibraryItem(
                        id = 1L,
                        uri = "content://1",
                        fileName = "a.pdf",
                        pageCount = 3,
                        thumbnailBytes = byteArrayOf(1),
                        importedAtEpochMillis = 0L,
                    )
                fakeLibraryRepository.emit(listOf(item))
                skipItems(1) // initial Success

                viewModel.onDeleteDocument(item.id)

                val afterDelete = awaitItem() as LibraryUiState.Success
                assertTrue(afterDelete.items.isEmpty())
            }
        }

    @Test
    fun `onImportDocument surfaces a one-off error without replacing uiState`() =
        runTest {
            fakePdfRepository.nextOpenResult = DataResult.Error(IllegalStateException("bad file"))

            viewModel.importErrors.test {
                viewModel.onImportDocument(uri = "content://broken", fileName = "broken.pdf", thumbnailWidthPx = 100)

                assertEquals("bad file", awaitItem())
            }
        }
}
