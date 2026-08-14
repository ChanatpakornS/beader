package com.beader.feature.sample

import androidx.compose.runtime.Immutable
import com.beader.core.domain.model.SampleItem

/**
 * Exhaustive, immutable representation of everything the screen can render.
 * The Composable is a pure function of this type — it holds no state of its
 * own beyond transient UI-only concerns (e.g. scroll position).
 */
@Immutable
sealed interface SampleUiState {
    data object Loading : SampleUiState

    data class Success(
        val items: List<SampleItem>,
    ) : SampleUiState

    data class Error(
        val message: String,
    ) : SampleUiState
}
