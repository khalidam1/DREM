package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "episodes",
    foreignKeys = [
        ForeignKey(
            entity = DramaEntity::class,
            parentColumns = ["id"],
            childColumns = ["dramaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("dramaId")]
)
data class EpisodeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dramaId: String,
    val title: String,
    val embedCode: String,
    val orderIndex: Int
)
