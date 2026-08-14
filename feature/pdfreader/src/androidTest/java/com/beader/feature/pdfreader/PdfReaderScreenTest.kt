package com.beader.feature.pdfreader

import android.graphics.Bitmap
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import java.io.ByteArrayOutputStream

class PdfReaderScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun errorState_rendersErrorMessage() {
        composeTestRule.setContent {
            PdfReaderScreen(
                uiState = PdfReaderUiState.Error(message = "Unable to open PDF"),
                onRetry = {},
                onNextPage = {},
                onPreviousPage = {},
            )
        }

        composeTestRule.onNodeWithText("Unable to open PDF").assertExists()
    }

    @Test
    fun successState_rendersPageIndicator() {
        composeTestRule.setContent {
            PdfReaderScreen(
                uiState =
                    PdfReaderUiState.Success(
                        pageIndex = 1,
                        pageCount = 5,
                        pageImageBytes = onePixelPng(),
                    ),
                onRetry = {},
                onNextPage = {},
                onPreviousPage = {},
            )
        }

        composeTestRule.onNodeWithText("Page 2 of 5").assertExists()
    }

    private fun onePixelPng(): ByteArray {
        val bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        return ByteArrayOutputStream().use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.toByteArray()
        }
    }
}
