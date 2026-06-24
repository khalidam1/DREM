package com.example.model.supabase

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupabaseAdConfig(
    val id: Int = 1,
    @SerialName("config_version") val configVersion: Int = 1,
    @SerialName("ads_enabled") val adsEnabled: Boolean = true,
    @SerialName("spot_id") val spotId: String = "",
    @SerialName("profile_ads_enabled") val profileAdsEnabled: Boolean = true,
    @SerialName("profile_spot_id") val profileSpotId: String = "",
    @SerialName("for_you_ads_enabled") val forYouAdsEnabled: Boolean = true,
    @SerialName("for_you_spot_id") val forYouSpotId: String = "",
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class SupabaseDrama(
    val id: String,
    val title: String,
    val description: String = "",
    val category: String = "",
    @SerialName("is_movie") val isMovie: Boolean? = true,
    @SerialName("movie_embed_code") val movieEmbedCode: String? = null,
    @SerialName("image_url") val imageUrl: String = "",
    val views: Int? = 0,
    val likes: Int? = 0,
    val badge: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class SupabaseComment(
    val id: String? = null,
    @SerialName("drama_id") val dramaId: String,
    val identifier: String = "",
    @SerialName("user_name") val userName: String,
    @SerialName("comment_text") val text: String,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class SupabaseCommentInsert(
    @SerialName("drama_id") val dramaId: String,
    val identifier: String = "",
    @SerialName("user_name") val userName: String,
    @SerialName("comment_text") val text: String
)

@Serializable
data class SupabaseUser(
    val id: String? = null,
    val username: String,
    @SerialName("password_hash") val passwordHash: String,
    @SerialName("is_admin") val isAdmin: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class SupabaseEpisode(
    val id: String,
    @SerialName("drama_id") val dramaId: String,
    val title: String,
    @SerialName("embed_code") val embedCode: String,
    @SerialName("episode_number") val episodeNumber: Int,
    @SerialName("created_at") val createdAt: String? = null
)
