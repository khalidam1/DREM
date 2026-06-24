package com.example.model

import android.content.Context
import android.content.SharedPreferences
import com.example.model.supabase.SupabaseAdConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AdConfigManager(context: Context, private val supabase: SupabaseClient = SupabaseNetwork.client) {
    private val prefs: SharedPreferences = context.getSharedPreferences("AdConfigPrefs", Context.MODE_PRIVATE)

    suspend fun initializeConfig() {
        withContext(Dispatchers.IO) {
            try {
                // Fetch the entire config from Supabase (it's a very small row anyway)
                val fullConfig = supabase.from("ad_config")
                    .select {
                        filter {
                            eq("id", 1)
                        }
                    }.decodeSingleOrNull<SupabaseAdConfig>()

                if (fullConfig != null) {
                    saveConfigLocally(fullConfig)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveConfigLocally(config: SupabaseAdConfig) {
        prefs.edit().apply {
            putInt("config_version", config.configVersion)
            putBoolean("ads_enabled", config.adsEnabled)
            putString("spot_id", config.spotId)
            putBoolean("profile_ads_enabled", config.profileAdsEnabled)
            putString("profile_spot_id", config.profileSpotId)
            putBoolean("for_you_ads_enabled", config.forYouAdsEnabled)
            putString("for_you_spot_id", config.forYouSpotId)
            putString("updated_at", config.updatedAt ?: "")
            apply()
        }
    }

    fun getSpotId(): String {
        return prefs.getString("spot_id", "") ?: ""
    }

    fun isAdsEnabled(): Boolean {
        return prefs.getBoolean("ads_enabled", true)
    }

    fun getProfileSpotId(): String {
        return prefs.getString("profile_spot_id", "") ?: ""
    }

    fun isProfileAdsEnabled(): Boolean {
        return prefs.getBoolean("profile_ads_enabled", true)
    }

    fun getForYouSpotId(): String {
        return prefs.getString("for_you_spot_id", "") ?: ""
    }

    fun isForYouAdsEnabled(): Boolean {
        return prefs.getBoolean("for_you_ads_enabled", true)
    }

    // This is for Admin
    suspend fun getFullConfigFromSupabase(): SupabaseAdConfig? {
        return try {
            supabase.from("ad_config")
                .select { filter { eq("id", 1) } }
                .decodeSingleOrNull<SupabaseAdConfig>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // For Admin to publish changes
    suspend fun publishConfig(config: SupabaseAdConfig): Pair<Boolean, String> {
        return try {
            val newVersion = config.configVersion + 1
            val configToUpdate = config.copy(configVersion = newVersion)
            
            // Try updating first
            try {
                supabase.from("ad_config").update(
                    {
                        set("config_version", configToUpdate.configVersion)
                        set("ads_enabled", configToUpdate.adsEnabled)
                        set("spot_id", configToUpdate.spotId)
                        set("profile_ads_enabled", configToUpdate.profileAdsEnabled)
                        set("profile_spot_id", configToUpdate.profileSpotId)
                        set("for_you_ads_enabled", configToUpdate.forYouAdsEnabled)
                        set("for_you_spot_id", configToUpdate.forYouSpotId)
                        set("updated_at", configToUpdate.updatedAt ?: "")
                    }
                ) {
                    filter { eq("id", 1) }
                }
            } catch (e: Exception) {
                // Fallback to upsert if update fails (e.g., row doesn't exist)
                supabase.from("ad_config").upsert(configToUpdate)
            }
            
            // Re-fetch to update locally as admin testing its own config
            initializeConfig()
            Pair(true, "تم النشر بنجاح! سيتم تطبيق التغييرات فوراً للعملاء.")
        } catch (e: Exception) {
            e.printStackTrace()
            // Provide a clear error to the UI
            Pair(false, "خطأ: ${e.message}")
        }
    }


}
