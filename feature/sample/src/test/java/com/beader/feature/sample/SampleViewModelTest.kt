package com.beader.feature.sample

import app.cash.turbine.test
import com.beader.core.domain.model.SampleItem
import com.beader.core.domain.usecase.GetSampleItemsUseCase
import com.beader.core.domain.usecase.ToggleFavoriteUseCase
import com.beader.core.testing.MainDispatcherRule
import com.beader.core.testing.repository.FakeSampleRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class SampleViewModelTest {
    companion object {
        @JvmField
        @RegisterExtension
        val mainDispatcherRule = MainDispatcherRule()
    }

    private lateinit var fakeRepository: FakeSampleRepository
    private lateinit var viewModel: SampleViewModel

    @BeforeEach
    fun setUp() {
        fakeRepository = FakeSampleRepository()
        viewModel =
            SampleViewModel(
                getSampleItems = GetSampleItemsUseCase(fakeRepository),
                toggleFavorite = ToggleFavoriteUseCase(fakeRepository),
            )
    }

    @Test
    fun `uiState starts as Loading before the repository emits`() =
        runTest {
            viewModel.uiState.test {
                assertTrue(awaitItem() is SampleUiState.Loading)
            }
        }

    @Test
    fun `uiState reflects repository items once loaded`() =
        runTest {
            viewModel.uiState.test {
                assertTrue(awaitItem() is SampleUiState.Loading)

                fakeRepository.emit(listOf(SampleItem(id = "1", title = "Item", description = "Desc")))

                val loaded = awaitItem() as SampleUiState.Success
                assertEquals(1, loaded.items.size)
            }
        }

    @Test
    fun `onToggleFavorite flips the item's favorite flag`() =
        runTest {
            fakeRepository.emit(listOf(SampleItem(id = "1", title = "Item", description = "Desc")))

            viewModel.uiState.test {
                skipItems(1) // Loading
                skipItems(1) // initial Success

                viewModel.onToggleFavorite("1")

                val updated = awaitItem() as SampleUiState.Success
                assertTrue(updated.items.first().isFavorite)
            }
        }
}
