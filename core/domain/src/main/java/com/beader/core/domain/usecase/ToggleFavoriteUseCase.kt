package com.beader.core.domain.usecase

import com.beader.core.domain.repository.SampleRepository
import javax.inject.Inject

class ToggleFavoriteUseCase
    @Inject
    constructor(
        private val sampleRepository: SampleRepository,
    ) {
        suspend operator fun invoke(id: String) = sampleRepository.toggleFavorite(id)
    }
