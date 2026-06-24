package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "favorite_dramas")
data class FavoriteDrama(
    @PrimaryKey val id: String,
    val title: String,
    val imageUrl: String,
    val category: String,
    val timestamp: Long = System.currentTimeMillis()
)
