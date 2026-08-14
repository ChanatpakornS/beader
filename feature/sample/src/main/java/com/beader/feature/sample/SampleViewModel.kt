package com.beader.feature.sample

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beader.core.common.result.DataResult
import com.beader.core.domain.usecase.GetSampleItemsUseCase
import com.beader.core.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SampleViewModel
    @Inject
    constructor(
        getSampleItems: GetSampleItemsUseCase,
        private val toggleFavorite: ToggleFavoriteUseCase,
    ) : ViewModel() {
        val uiState: StateFlow<SampleUiState> =
            getSampleItems()
                .map { result ->
                    when (result) {
                        is DataResult.Loading -> {
                            SampleUiState.Loading
                        }

                        is DataResult.Success -> {
                            SampleUiState.Success(result.data)
                        }

                        is DataResult.Error -> {
                            SampleUiState.Error(
                                result.message ?: "Something went wrong",
                            )
                        }
                    }
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                    initialValue = SampleUiState.Loading,
                )

        fun onToggleFavorite(id: String) {
            viewModelScope.launch {
                toggleFavorite(id)
            }
        }

        private companion object {
            const val STOP_TIMEOUT_MILLIS = 5_000L
        }
    }
