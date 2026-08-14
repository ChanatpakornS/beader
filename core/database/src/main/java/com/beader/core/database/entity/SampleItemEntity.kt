package com.beader.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sample_items")
data class SampleItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val isFavorite: Boolean = false,
)
