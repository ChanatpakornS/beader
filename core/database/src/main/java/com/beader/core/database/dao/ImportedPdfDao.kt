package com.beader.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.beader.core.database.entity.ImportedPdfEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ImportedPdfDao {
    @Query("SELECT * FROM imported_pdfs ORDER BY importedAtEpochMillis DESC")
    fun observeAll(): Flow<List<ImportedPdfEntity>>

    @Insert
    suspend fun insert(entity: ImportedPdfEntity): Long

    @Query("DELETE FROM imported_pdfs WHERE id = :id")
    suspend fun deleteById(id: Long)
}
