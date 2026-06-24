package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ads_config")
data class AdConfigEntity(
    @PrimaryKey val id: Int = 1,
    val adsEnabled: Boolean = true,
    val globalAdCode: String = "",
    val homeAdCode: String = "",
    val playerAdCode: String = ""
)
