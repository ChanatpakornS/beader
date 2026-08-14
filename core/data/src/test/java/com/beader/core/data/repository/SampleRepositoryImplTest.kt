package com.beader.core.data.repository

import app.cash.turbine.test
import com.beader.core.common.result.DataResult
import com.beader.core.database.dao.SampleItemDao
import com.beader.core.database.entity.SampleItemEntity
import com.beader.core.network.model.SampleItemDto
import com.beader.core.network.service.SampleApiService
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class SampleRepositoryImplTest {
    private val sampleApiService: SampleApiService = mockk()
    private val sampleItemDao: SampleItemDao = mockk()
    private val repository = SampleRepositoryImpl(sampleApiService, sampleItemDao)

    @Test
    fun `observeSampleItems refreshes from network and emits cached rows`() =
        runTest {
            val cached =
                MutableStateFlow(
                    listOf(SampleItemEntity(id = "1", title = "Cached", description = "From Room")),
                )
            every { sampleItemDao.observeAll() } returns cached
            coEvery { sampleApiService.getSampleItems() } returns
                listOf(SampleItemDto(id = "1", title = "Cached", description = "From Room"))
            coEvery { sampleItemDao.upsertAll(any()) } returns Unit

            repository.observeSampleItems().test {
                val loading = awaitItem()
                assert(loading is DataResult.Loading)

                val success = awaitItem()
                assert(success is DataResult.Success && success.data.first().title == "Cached")

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `observeSampleItems falls back to cache when the network call fails`() =
        runTest {
            val cached =
                MutableStateFlow(
                    listOf(SampleItemEntity(id = "1", title = "Cached", description = "From Room")),
                )
            every { sampleItemDao.observeAll() } returns cached
            coEvery { sampleApiService.getSampleItems() } throws java.io.IOException("offline")

            repository.observeSampleItems().test {
                awaitItem() // Loading
                val success = awaitItem()
                assert(success is DataResult.Success && success.data.size == 1)
                cancelAndIgnoreRemainingEvents()
            }
        }
}
