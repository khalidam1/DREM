package com.example.model

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.model.supabase.SupabaseDrama
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.first

class NotificationWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("DramaPrefs", Context.MODE_PRIVATE)
        val lastCheckTime = prefs.getLong("last_notification_check", 0L)
        val currentTime = System.currentTimeMillis()
        
        try {
            if (!prefs.getBoolean("notifications_enabled", true)) {
                return Result.success()
            }
            
            val dramasFromDb = com.example.model.SupabaseNetwork.client
                .postgrest["dramas"]
                .select()
                .decodeList<SupabaseDrama>()
            
            // Get favorites
            val favoritesManager = FavoritesManager(applicationContext)
            val favorites = favoritesManager.favorites.first()
            
            val favoritedIds = favorites.map { it.id }.toSet()
            
            var newContentFound = false
            var latestDramaTitle = ""
            for (drama in dramasFromDb) {
                if (favoritedIds.contains(drama.id)) {
                    newContentFound = true
                    latestDramaTitle = drama.title
                    break
                }
            }
            
            if (newContentFound && (currentTime - lastCheckTime > 24 * 60 * 60 * 1000L)) {
                 showNotification("$latestDramaTitle - من مسلسلك المفضل", "تم إضافة حلقات جديدة.")
                 prefs.edit().putLong("last_notification_check", currentTime).apply()
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }

        return Result.success()
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "drama_notifications"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "إشعارات المسلسلات",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent, pendingIntentFlags
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            // Use standard icon since custom one is not ready
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }
}
