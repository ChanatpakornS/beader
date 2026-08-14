package com.beader.core.domain.usecase

import app.cash.turbine.test
import com.beader.core.common.result.DataResult
import com.beader.core.domain.model.SampleItem
import com.beader.core.domain.repository.SampleRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class GetSampleItemsUseCaseTest {
    private val sampleRepository: SampleRepository = mockk()
    private val getSampleItems = GetSampleItemsUseCase(sampleRepository)

    @Test
    fun `invoke emits items from the repository unchanged`() = runTest {
        val items = listOf(SampleItem(id = "1", title = "First", description = "Desc"))
        every { sampleRepository.observeSampleItems() } returns flowOf(DataResult.Success(items))

        getSampleItems().test {
            val emission = awaitItem()
            assert(emission is DataResult.Success && emission.data == items)
            awaitComplete()
        }
    }
}
