package com.example.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AdConfigDao {
    @Query("SELECT * FROM ads_config WHERE id = 1 LIMIT 1")
    fun getAdConfig(): Flow<AdConfigEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdConfig(config: AdConfigEntity)
}
