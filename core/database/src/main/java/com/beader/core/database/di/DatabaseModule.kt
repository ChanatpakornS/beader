package com.beader.core.database.di

import android.content.Context
import androidx.room.Room
import com.beader.core.database.BeaderDatabase
import com.beader.core.database.dao.ImportedPdfDao
import com.beader.core.database.dao.SampleItemDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val DATABASE_NAME = "beader-database"

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideBeaderDatabase(
        @ApplicationContext context: Context,
    ): BeaderDatabase =
        Room
            .databaseBuilder(context, BeaderDatabase::class.java, DATABASE_NAME)
            // Pre-1.0 scaffold: no shipped installs to migrate yet. Replace with real
            // Migration objects once this schema needs to survive an upgrade in the wild.
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideSampleItemDao(database: BeaderDatabase): SampleItemDao = database.sampleItemDao()

    @Provides
    fun provideImportedPdfDao(database: BeaderDatabase): ImportedPdfDao = database.importedPdfDao()
}
