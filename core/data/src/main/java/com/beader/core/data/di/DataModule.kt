package com.beader.core.data.di

import com.beader.core.data.repository.PdfLibraryRepositoryImpl
import com.beader.core.data.repository.PdfRepositoryImpl
import com.beader.core.data.repository.SampleRepositoryImpl
import com.beader.core.domain.repository.PdfLibraryRepository
import com.beader.core.domain.repository.PdfRepository
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

    @Binds
    abstract fun bindPdfRepository(impl: PdfRepositoryImpl): PdfRepository

    @Binds
    abstract fun bindPdfLibraryRepository(impl: PdfLibraryRepositoryImpl): PdfLibraryRepository
}
