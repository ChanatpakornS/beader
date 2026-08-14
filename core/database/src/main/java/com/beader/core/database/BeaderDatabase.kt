package com.beader.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.beader.core.database.dao.SampleItemDao
import com.beader.core.database.entity.SampleItemEntity

@Database(
    entities = [SampleItemEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class BeaderDatabase : RoomDatabase() {
    abstract fun sampleItemDao(): SampleItemDao
}
