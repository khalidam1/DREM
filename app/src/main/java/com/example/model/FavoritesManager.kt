package com.example.model

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class FavoritesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("FavoritesPrefs", Context.MODE_PRIVATE)
    private val _favorites = MutableStateFlow<List<FavoriteDrama>>(loadFavorites())
    val favorites: StateFlow<List<FavoriteDrama>> = _favorites.asStateFlow()

    private fun loadFavorites(): List<FavoriteDrama> {
        val jsonString = prefs.getString("favorites_list", null) ?: return emptyList()
        return try {
            Json.decodeFromString<List<FavoriteDrama>>(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveFavorites(list: List<FavoriteDrama>) {
        val sortedList = list.sortedByDescending { it.timestamp }
        val jsonString = Json.encodeToString(sortedList)
        prefs.edit().putString("favorites_list", jsonString).apply()
        _favorites.value = sortedList
    }

    fun addFavorite(favorite: FavoriteDrama) {
        val currentList = _favorites.value.toMutableList()
        if (currentList.none { it.id == favorite.id }) {
            currentList.add(favorite)
            saveFavorites(currentList)
        }
    }

    fun removeFavorite(id: String) {
        val currentList = _favorites.value.toMutableList()
        currentList.removeAll { it.id == id }
        saveFavorites(currentList)
    }

    fun isFavorite(id: String): Boolean {
        return _favorites.value.any { it.id == id }
    }
}
