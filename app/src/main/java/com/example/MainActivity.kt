package com.example

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import java.util.concurrent.TimeUnit
import com.example.model.AdConfigManager
import com.example.model.NotificationWorker
import com.example.ui.DramaApp
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen()
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Schedule background worker for notifications
    val notificationWorkRequest = PeriodicWorkRequestBuilder<NotificationWorker>(15, TimeUnit.MINUTES)
        .build()
        
    WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
        "drama_notification_work",
        ExistingPeriodicWorkPolicy.KEEP,
        notificationWorkRequest
    )

    // Initialize Ad Configuration
    val adConfigManager = AdConfigManager(this)
    lifecycleScope.launch {
        adConfigManager.initializeConfig()
    }

    setContent {
      MyApplicationTheme {
        DramaApp()
      }
    }
  }
}
