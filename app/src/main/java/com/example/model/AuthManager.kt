package com.example.model

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class AuthManager(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "AuthPrefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("is_logged_in", false)
    }

    fun isAdmin(): Boolean {
        return prefs.getBoolean("is_admin", false)
    }

    fun getUsername(): String {
        return prefs.getString("username", "Guest") ?: "Guest"
    }

    fun login(username: String, isAdmin: Boolean = false) {
        prefs.edit()
            .putBoolean("is_logged_in", true)
            .putBoolean("is_admin", isAdmin)
            .putString("username", username)
            .apply()
    }

    fun logout() {
        prefs.edit()
            .putBoolean("is_logged_in", false)
            .putBoolean("is_admin", false)
            .putString("username", "")
            .apply()
    }
}
