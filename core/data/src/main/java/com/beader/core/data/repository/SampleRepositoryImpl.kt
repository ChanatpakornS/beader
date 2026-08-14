package com.beader.core.data.repository

import com.beader.core.common.result.DataResult
import com.beader.core.database.dao.SampleItemDao
import com.beader.core.database.entity.SampleItemEntity
import com.beader.core.domain.model.SampleItem
import com.beader.core.domain.repository.SampleRepository
import com.beader.core.network.model.SampleItemDto
import com.beader.core.network.service.SampleApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room is the single source of truth: reads always come from
 * [observeSampleItems], writes go through the network then land back in the
 * database, and the DAO's [Flow] pushes the update to every collector.
 */
@Singleton
class SampleRepositoryImpl
    @Inject
    constructor(
        private val sampleApiService: SampleApiService,
        private val sampleItemDao: SampleItemDao,
    ) : SampleRepository {
        override fun observeSampleItems(): Flow<DataResult<List<SampleItem>>> =
            sampleItemDao.observeAll()
                .map<List<SampleItemEntity>, DataResult<List<SampleItem>>> { entities ->
                    DataResult.Success(entities.map { it.toDomain() })
                }
                .onStart {
                    emit(DataResult.Loading)
                    refreshFromNetwork()
                }
                .catch { throwable ->
                    Timber.e(throwable, "Failed to observe sample items")
                    emit(DataResult.Error(throwable))
                }

        override suspend fun toggleFavorite(id: String) {
            sampleItemDao.toggleFavorite(id)
        }

        private suspend fun refreshFromNetwork() {
            runCatching { sampleApiService.getSampleItems() }
                .onSuccess { dtos ->
                    sampleItemDao.upsertAll(dtos.map { it.toEntity() })
                }
                .onFailure { throwable ->
                    Timber.w(throwable, "Network refresh failed, serving cached data")
                }
        }
    }

private fun SampleItemEntity.toDomain() =
    SampleItem(
        id = id,
        title = title,
        description = description,
        isFavorite = isFavorite,
    )

private fun SampleItemDto.toEntity() =
    SampleItemEntity(
        id = id,
        title = title,
        description = description,
    )
