package com.beader.core.testing.repository

import com.beader.core.common.result.DataResult
import com.beader.core.domain.model.SampleItem
import com.beader.core.domain.repository.SampleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory [SampleRepository] fake for ViewModel and use case tests.
 * Tests drive state through [emit] / [emitError] instead of mocking Flow
 * emissions by hand.
 */
class FakeSampleRepository : SampleRepository {

    private val state = MutableStateFlow<DataResult<List<SampleItem>>>(DataResult.Loading)
    private val items = mutableListOf<SampleItem>()

    fun emit(newItems: List<SampleItem>) {
        items.clear()
        items.addAll(newItems)
        state.value = DataResult.Success(items.toList())
    }

    fun emitError(throwable: Throwable) {
        state.value = DataResult.Error(throwable)
    }

    override fun observeSampleItems() = state.asStateFlow()

    override suspend fun toggleFavorite(id: String) {
        val index = items.indexOfFirst { it.id == id }
        if (index != -1) {
            items[index] = items[index].copy(isFavorite = !items[index].isFavorite)
            state.value = DataResult.Success(items.toList())
        }
    }
}
