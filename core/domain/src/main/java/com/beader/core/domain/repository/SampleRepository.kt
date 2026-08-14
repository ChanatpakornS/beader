package com.beader.core.domain.repository

import com.beader.core.common.result.DataResult
import com.beader.core.domain.model.SampleItem
import kotlinx.coroutines.flow.Flow

/**
 * Domain-owned contract. `:core:data` provides the implementation and is
 * bound to this interface via Hilt (`@Binds`) so this module never depends
 * on Retrofit, Room, or any other implementation detail.
 */
interface SampleRepository {
    fun observeSampleItems(): Flow<DataResult<List<SampleItem>>>

    suspend fun toggleFavorite(id: String)
}
