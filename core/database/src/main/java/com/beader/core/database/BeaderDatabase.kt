package com.beader.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.beader.core.database.dao.ImportedPdfDao
import com.beader.core.database.dao.SampleItemDao
import com.beader.core.database.entity.ImportedPdfEntity
import com.beader.core.database.entity.SampleItemEntity

@Database(
    entities = [SampleItemEntity::class, ImportedPdfEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class BeaderDatabase : RoomDatabase() {
    abstract fun sampleItemDao(): SampleItemDao

    abstract fun importedPdfDao(): ImportedPdfDao
}
