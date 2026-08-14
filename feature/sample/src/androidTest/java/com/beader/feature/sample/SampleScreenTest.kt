package com.beader.feature.sample

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.beader.core.domain.model.SampleItem
import org.junit.Rule
import org.junit.Test

class SampleScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun successState_rendersItemTitles() {
        composeTestRule.setContent {
            SampleScreen(
                uiState =
                    SampleUiState.Success(
                        items = listOf(SampleItem(id = "1", title = "First item", description = "Desc")),
                    ),
                onToggleFavorite = {},
            )
        }

        composeTestRule.onNodeWithText("First item").assertExists()
    }

    @Test
    fun errorState_rendersErrorMessage() {
        composeTestRule.setContent {
            SampleScreen(
                uiState = SampleUiState.Error(message = "Network unavailable"),
                onToggleFavorite = {},
            )
        }

        composeTestRule.onNodeWithText("Network unavailable").assertExists()
    }
}
