package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.AppDatabase
import com.example.model.DramaRepository
import com.example.model.FavoriteDrama
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Count

import kotlinx.coroutines.flow.map

class DramaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: DramaRepository
    private val favoritesManager = com.example.model.FavoritesManager(application)
    
    val allFavorites: StateFlow<List<FavoriteDrama>> = favoritesManager.favorites

    init {
        val database = AppDatabase.getDatabase(application)
        repository = DramaRepository(
            database.favoriteDao(),
            database.userDao(),
            database.dramaDao(),
            database.adConfigDao()
        )
    }

    // Expose Data
    val allDramas = repository.allDramas.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val adConfig = repository.adConfig.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val _dramasFromSupabase = kotlinx.coroutines.flow.MutableStateFlow<List<com.example.model.supabase.SupabaseDrama>>(emptyList())
    val dramasFromSupabase: StateFlow<List<com.example.model.supabase.SupabaseDrama>> = _dramasFromSupabase

    private val _isLoadingDramas = kotlinx.coroutines.flow.MutableStateFlow(true)
    val isLoadingDramas: StateFlow<Boolean> = _isLoadingDramas

    private val _errorDramas = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    val errorDramas: StateFlow<String?> = _errorDramas

    init {
        fetchDramasFromSupabase()
    }

    fun fetchDramasFromSupabase() {
        viewModelScope.launch {
            _isLoadingDramas.value = true
            _errorDramas.value = null
            try {
                val result = com.example.model.SupabaseNetwork.client
                    .postgrest["dramas"]
                    .select()
                    .decodeList<com.example.model.supabase.SupabaseDrama>()
                _dramasFromSupabase.value = result
            } catch (e: Exception) {
                e.printStackTrace()
                _errorDramas.value = "فشل في تحميل البيانات. يرجى التحقق من اتصالك بالإنترنت."
            } finally {
                _isLoadingDramas.value = false
            }
        }
    }

    fun fetchEpisodesFromSupabase(dramaId: String): kotlinx.coroutines.flow.Flow<List<com.example.model.supabase.SupabaseEpisode>> = kotlinx.coroutines.flow.flow {
        try {
            val result = com.example.model.SupabaseNetwork.client
                .postgrest["episodes"]
                .select {
                    filter {
                        eq("drama_id", dramaId)
                    }
                    order("episode_number", io.github.jan.supabase.postgrest.query.Order.ASCENDING)
                }
                .decodeList<com.example.model.supabase.SupabaseEpisode>()
            emit(result)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }

    fun fetchSupabaseDramaDetails(dramaId: String): kotlinx.coroutines.flow.Flow<com.example.model.supabase.SupabaseDrama?> = kotlinx.coroutines.flow.flow {
        try {
            val result = com.example.model.SupabaseNetwork.client
                .postgrest["dramas"]
                .select {
                    filter {
                        eq("id", dramaId)
                    }
                }
                .decodeSingleOrNull<com.example.model.supabase.SupabaseDrama>()
            emit(result)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(null)
        }
    }

    // Comments & Analytics
    fun fetchComments(dramaId: String): kotlinx.coroutines.flow.Flow<List<com.example.model.supabase.SupabaseComment>> = kotlinx.coroutines.flow.flow {
        try {
            val result = com.example.model.SupabaseNetwork.client
                .postgrest["comments"]
                .select {
                    filter { eq("drama_id", dramaId) }
                    order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }
                .decodeList<com.example.model.supabase.SupabaseComment>()
            emit(result)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }

    fun postComment(dramaId: String, text: String, userName: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val comment = com.example.model.supabase.SupabaseCommentInsert(
                    dramaId = dramaId,
                    userName = userName,
                    text = text,
                    identifier = userName
                )
                com.example.model.SupabaseNetwork.client
                    .postgrest["comments"]
                    .insert(comment)
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun incrementViews(dramaId: String, currentViews: Int) {
        viewModelScope.launch {
            try {
                com.example.model.SupabaseNetwork.client
                    .postgrest["dramas"]
                    .update(
                        {
                            set("views", currentViews + 1)
                        }
                    ) {
                        filter { eq("id", dramaId) }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun incrementLikes(dramaId: String, currentLikes: Int) {
        viewModelScope.launch {
            try {
                com.example.model.SupabaseNetwork.client
                    .postgrest["dramas"]
                    .update(
                        {
                            set("likes", currentLikes + 1)
                        }
                    ) {
                        filter { eq("id", dramaId) }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun saveDramaToSupabase(
        drama: com.example.model.supabase.SupabaseDrama,
        episodes: List<com.example.model.supabase.SupabaseEpisode>,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                com.example.model.SupabaseNetwork.client
                    .postgrest["dramas"]
                    .upsert(drama)

                com.example.model.SupabaseNetwork.client
                    .postgrest["episodes"]
                    .delete {
                        filter {
                            eq("drama_id", drama.id)
                        }
                    }

                if (episodes.isNotEmpty()) {
                    com.example.model.SupabaseNetwork.client
                        .postgrest["episodes"]
                        .insert(episodes)
                }

                fetchDramasFromSupabase()
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e)
            }
        }
    }

    // Admin & Drama Actions
    fun getDrama(id: String) = repository.getDramaById(id)
    fun getEpisodes(dramaId: String) = repository.getEpisodesForDrama(dramaId)

    fun saveDrama(drama: com.example.model.DramaEntity, episodes: List<com.example.model.EpisodeEntity> = emptyList()) {
        viewModelScope.launch {
            if (episodes.isEmpty()) {
                repository.insertDrama(drama)
            } else {
                repository.insertDramaWithEpisodes(drama, episodes)
            }
        }
    }

    fun deleteDrama(id: String) {
        viewModelScope.launch {
            repository.deleteDramaById(id)
        }
    }

    // Ads
    fun saveAdConfig(config: com.example.model.AdConfigEntity) {
        viewModelScope.launch {
            repository.saveAdConfig(config)
        }
    }

    // Auth
    suspend fun login(username: String, passwordHash: String): Boolean {
        val user = repository.getUserByUsername(username)
        return user != null && user.passwordHash == passwordHash
    }

    suspend fun register(user: com.example.model.UserEntity): Boolean {
        val existing = repository.getUserByUsername(user.username)
        if (existing != null) return false
        repository.registerUser(user)
        return true
    }

    fun isFavorite(id: String) = allFavorites.map { list -> list.find { it.id == id } }

    fun toggleFavorite(dramaItem: DramaItem, isFav: Boolean) {
        viewModelScope.launch {
            if (isFav) {
                favoritesManager.removeFavorite(dramaItem.id)
            } else {
                favoritesManager.addFavorite(
                    FavoriteDrama(
                        id = dramaItem.id,
                        title = dramaItem.title,
                        imageUrl = dramaItem.imageUrl,
                        category = dramaItem.category
                    )
                )
            }
        }
    }

    // Supabase Test Connection
    private val _supabaseConnectionStatus = kotlinx.coroutines.flow.MutableStateFlow<String>("لم يتم الاختبار بعد")
    val supabaseConnectionStatus: StateFlow<String> = _supabaseConnectionStatus

    fun testSupabaseConnection() {
        viewModelScope.launch {
            _supabaseConnectionStatus.value = "جاري الاتصال..."
            try {
                // Trying to fetch count from 'dramas' table
                val result = com.example.model.SupabaseNetwork.client
                    .postgrest["dramas"]
                    .select() {
                        count(Count.EXACT)
                        limit(1)
                    }
                _supabaseConnectionStatus.value = "تم بنجاح! عدد الأعمال في قاعدة البيانات: ${result.countOrNull() ?: 0}"
            } catch (e: Exception) {
                _supabaseConnectionStatus.value = "خطأ في الاتصال: ${e.message}"
            }
        }
    }
}
