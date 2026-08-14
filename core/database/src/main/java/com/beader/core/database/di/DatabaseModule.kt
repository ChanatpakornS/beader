package com.beader.core.database.di

import android.content.Context
import androidx.room.Room
import com.beader.core.database.BeaderDatabase
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
    fun provideBeaderDatabase(@ApplicationContext context: Context): BeaderDatabase =
        Room.databaseBuilder(context, BeaderDatabase::class.java, DATABASE_NAME).build()

    @Provides
    fun provideSampleItemDao(database: BeaderDatabase): SampleItemDao = database.sampleItemDao()
}
