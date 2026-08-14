package com.beader.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.beader.core.database.entity.SampleItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SampleItemDao {
    @Query("SELECT * FROM sample_items ORDER BY title ASC")
    fun observeAll(): Flow<List<SampleItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<SampleItemEntity>)

    @Query("UPDATE sample_items SET isFavorite = NOT isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: String)
}
