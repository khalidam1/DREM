package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dramas")
data class DramaEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String,
    val isMovie: Boolean,
    val movieEmbedCode: String? = null,
    val imageUrl: String,
    val views: String = "0",
    val badge: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
