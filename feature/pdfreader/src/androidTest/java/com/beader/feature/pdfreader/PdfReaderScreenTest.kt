package com.beader.feature.pdfreader

import android.graphics.Bitmap
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Rule
import org.junit.Test
import java.io.ByteArrayOutputStream

class PdfReaderScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val noopActions =
        PdfReaderActions(
            onRetry = {},
            onNextPage = {},
            onPreviousPage = {},
            onToggleReadingMode = {},
            onJumpToPage = {},
            onRequestPage = {},
            onOpenDrawer = {},
        )

    @Test
    fun errorState_rendersErrorMessage() {
        composeTestRule.setContent {
            PdfReaderScreen(
                uiState = PdfReaderUiState.Error(message = "Unable to open PDF"),
                scrollToPageEvents = emptyFlow(),
                actions = noopActions,
            )
        }

        composeTestRule.onNodeWithText("Unable to open PDF").assertExists()
    }

    @Test
    fun singlePageMode_rendersPageIndicatorAndJumpField() {
        composeTestRule.setContent {
            PdfReaderScreen(
                uiState =
                    PdfReaderUiState.Content(
                        readingMode = ReadingMode.SINGLE_PAGE,
                        pageCount = 5,
                        currentPageIndex = 1,
                        pages = mapOf(1 to onePixelPng()),
                    ),
                scrollToPageEvents = emptyFlow(),
                actions = noopActions,
            )
        }

        composeTestRule.onNodeWithText("Page 2 of 5").assertExists()
        composeTestRule.onNodeWithText("Go to page").assertExists()
        composeTestRule.onNodeWithText("Continuous").assertExists()
    }

    @Test
    fun continuousMode_showsSinglePageToggleLabel() {
        composeTestRule.setContent {
            PdfReaderScreen(
                uiState =
                    PdfReaderUiState.Content(
                        readingMode = ReadingMode.CONTINUOUS,
                        pageCount = 5,
                        currentPageIndex = 0,
                        pages = mapOf(0 to onePixelPng()),
                    ),
                scrollToPageEvents = emptyFlow(),
                actions = noopActions,
            )
        }

        composeTestRule.onNodeWithText("Single page").assertExists()
    }

    private fun onePixelPng(): ByteArray {
        val bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        return ByteArrayOutputStream().use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.toByteArray()
        }
    }
}
