package com.example.model

import kotlinx.coroutines.flow.Flow

class DramaRepository(
    private val favoriteDao: FavoriteDao,
    private val userDao: UserDao,
    private val dramaDao: DramaDao,
    private val adConfigDao: AdConfigDao
) {
    // Favorites
    val allFavorites: Flow<List<FavoriteDrama>> = favoriteDao.getAllFavorites()

    fun isFavorite(id: String): Flow<FavoriteDrama?> {
        return favoriteDao.getFavoriteById(id)
    }

    suspend fun addFavorite(drama: FavoriteDrama) {
        favoriteDao.insertFavorite(drama)
    }

    suspend fun removeFavorite(id: String) {
        favoriteDao.deleteFavoriteById(id)
    }

    // Users
    suspend fun getUserByUsername(username: String): UserEntity? {
        return userDao.getUserByUsername(username)
    }

    suspend fun registerUser(user: UserEntity) {
        userDao.insertUser(user)
    }

    // Dramas
    val allDramas: Flow<List<DramaEntity>> = dramaDao.getAllDramas()

    fun getDramaById(id: String): Flow<DramaEntity?> {
        return dramaDao.getDramaById(id)
    }

    suspend fun insertDrama(drama: DramaEntity) {
        dramaDao.insertDrama(drama)
    }

    suspend fun insertDramaWithEpisodes(drama: DramaEntity, episodes: List<EpisodeEntity>) {
        dramaDao.insertDramaWithEpisodes(drama, episodes)
    }

    suspend fun deleteDramaById(id: String) {
        dramaDao.deleteDramaById(id)
    }

    fun getEpisodesForDrama(dramaId: String): Flow<List<EpisodeEntity>> {
        return dramaDao.getEpisodesForDrama(dramaId)
    }

    // Ads
    val adConfig: Flow<AdConfigEntity?> = adConfigDao.getAdConfig()

    suspend fun saveAdConfig(config: AdConfigEntity) {
        adConfigDao.insertAdConfig(config)
    }
}
