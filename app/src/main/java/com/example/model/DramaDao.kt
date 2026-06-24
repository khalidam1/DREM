package com.example.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface DramaDao {
    @Query("SELECT * FROM dramas ORDER BY timestamp DESC")
    fun getAllDramas(): Flow<List<DramaEntity>>

    @Query("SELECT * FROM dramas WHERE id = :id LIMIT 1")
    fun getDramaById(id: String): Flow<DramaEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrama(drama: DramaEntity)

    @Query("DELETE FROM dramas WHERE id = :id")
    suspend fun deleteDramaById(id: String)

    // Episodes
    @Query("SELECT * FROM episodes WHERE dramaId = :dramaId ORDER BY orderIndex ASC")
    fun getEpisodesForDrama(dramaId: String): Flow<List<EpisodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodes(episodes: List<EpisodeEntity>)

    @Query("DELETE FROM episodes WHERE dramaId = :dramaId")
    suspend fun deleteEpisodesForDrama(dramaId: String)
    
    @Transaction
    suspend fun insertDramaWithEpisodes(drama: DramaEntity, episodes: List<EpisodeEntity>) {
        insertDrama(drama)
        deleteEpisodesForDrama(drama.id)
        insertEpisodes(episodes)
    }
}
