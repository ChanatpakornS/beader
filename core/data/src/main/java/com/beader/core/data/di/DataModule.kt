package com.beader.core.data.di

import com.beader.core.data.repository.SampleRepositoryImpl
import com.beader.core.domain.repository.SampleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    abstract fun bindSampleRepository(impl: SampleRepositoryImpl): SampleRepository
}
