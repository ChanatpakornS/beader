package com.beader.core.pdf.di

import com.beader.core.common.di.BeaderDispatchers
import com.beader.core.common.di.Dispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * First real binding for the `@Dispatcher` qualifier declared in
 * `:core:common` — [PdfRendererDataSource][com.beader.core.pdf.datasource.PdfRendererDataSource]
 * is the first data source in this codebase that needs to move blocking
 * work off the calling thread itself, so it needed a home. Move this module
 * to a shared location if a second consumer needs the binding.
 */
@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {
    @Provides
    @Dispatcher(BeaderDispatchers.Io)
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
