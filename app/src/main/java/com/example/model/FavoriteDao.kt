package com.example.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorite_dramas ORDER BY timestamp DESC")
    fun getAllFavorites(): Flow<List<FavoriteDrama>>

    @Query("SELECT * FROM favorite_dramas WHERE id = :id LIMIT 1")
    fun getFavoriteById(id: String): Flow<FavoriteDrama?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(drama: FavoriteDrama)

    @Query("DELETE FROM favorite_dramas WHERE id = :id")
    suspend fun deleteFavoriteById(id: String)
}
