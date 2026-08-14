package com.beader.feature.library

import android.graphics.Bitmap
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.beader.core.domain.model.PdfLibraryItem
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Rule
import org.junit.Test
import java.io.ByteArrayOutputStream

class LibraryScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val noopActions =
        LibraryActions(onImportDocument = {}, onOpenDocument = {}, onDeleteDocument = {})

    @Test
    fun loadingState_rendersLoadingIndicator() {
        composeTestRule.setContent {
            LibraryScreen(
                uiState = LibraryUiState.Loading,
                importErrors = emptyFlow(),
                actions = noopActions,
            )
        }

        composeTestRule.onNodeWithText("Library").assertExists()
    }

    @Test
    fun errorState_rendersErrorMessage() {
        composeTestRule.setContent {
            LibraryScreen(
                uiState = LibraryUiState.Error(message = "Database unavailable"),
                importErrors = emptyFlow(),
                actions = noopActions,
            )
        }

        composeTestRule.onNodeWithText("Database unavailable").assertExists()
    }

    @Test
    fun emptySuccessState_rendersEmptyPrompt() {
        composeTestRule.setContent {
            LibraryScreen(
                uiState = LibraryUiState.Success(items = emptyList()),
                importErrors = emptyFlow(),
                actions = noopActions,
            )
        }

        composeTestRule.onNodeWithText("No PDFs yet. Tap + to import one.").assertExists()
    }

    @Test
    fun successState_rendersImportedFileName() {
        composeTestRule.setContent {
            LibraryScreen(
                uiState = LibraryUiState.Success(items = listOf(fakeLibraryItem())),
                importErrors = emptyFlow(),
                actions = noopActions,
            )
        }

        composeTestRule.onNodeWithText("report.pdf").assertExists()
    }

    private fun fakeLibraryItem(): PdfLibraryItem =
        PdfLibraryItem(
            id = 1L,
            uri = "content://documents/1",
            fileName = "report.pdf",
            pageCount = 3,
            thumbnailBytes = onePixelPng(),
            importedAtEpochMillis = 0L,
        )

    private fun onePixelPng(): ByteArray {
        val bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        return ByteArrayOutputStream().use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.toByteArray()
        }
    }
}
