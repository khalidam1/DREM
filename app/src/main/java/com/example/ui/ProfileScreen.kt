package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun ProfileScreen(onLoginClick: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Profile Info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onLoginClick() }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "الصورة الشخصية",
                    tint = LightGrayInfo,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = "تسجيل الدخول",
                    color = WhiteText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = null,
                tint = LightGrayInfo
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        val adConfigManager = remember { com.example.model.AdConfigManager(context) }
        var isProfileAdsEnabled by remember { mutableStateOf(adConfigManager.isProfileAdsEnabled()) }
        var profileBannerId by remember { mutableStateOf(adConfigManager.getProfileSpotId()) }

        androidx.compose.runtime.LaunchedEffect(Unit) {
            try {
                adConfigManager.initializeConfig()
                isProfileAdsEnabled = adConfigManager.isProfileAdsEnabled()
                profileBannerId = adConfigManager.getProfileSpotId()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Ad Banner
        if (isProfileAdsEnabled && profileBannerId.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.ui.viewinterop.AndroidView(
                    factory = { ctx ->
                        android.webkit.WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.databaseEnabled = true
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            }
                            settings.useWideViewPort = true
                            settings.loadWithOverviewMode = true
                            
                            // Important for ads
                            settings.mediaPlaybackRequiresUserGesture = false
                            
                            webViewClient = android.webkit.WebViewClient()
                            webChromeClient = android.webkit.WebChromeClient()
                            setBackgroundColor(0) // Transparent background
                            isVerticalScrollBarEnabled = false
                            isHorizontalScrollBarEnabled = false
                            scrollBarStyle = android.view.View.SCROLLBARS_INSIDE_OVERLAY
                        }
                    },
                    update = { webView ->
                                val html = """
                                    <html>
                                        <head>
                                            <meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no'>
                                            <style>
                                                html, body {
                                                    margin: 0;
                                                    padding: 0;
                                                    width: 100%;
                                                    height: 100%;
                                                    background-color: transparent;
                                                    direction: rtl;
                                                    text-align: center;
                                                }
                                                .ad-container {
                                                    display: flex;
                                                    justify-content: center;
                                                    align-items: center;
                                                    width: 100%;
                                                }
                                            </style>
                                        </head>
                                        <body>
                                            <div class='ad-container'>
                                                ${profileBannerId.replace("document.write", "console.log")}
                                            </div>
                                            <script type="text/javascript">
                                                // Handle document.write for ads that use it
                                                document.write = function(content) {
                                                    document.querySelector('.ad-container').innerHTML += content;
                                                };
                                            </script>
                                            $profileBannerId
                                        </body>
                                    </html>
                                """.trimIndent()
                        if (webView.tag != html) {
                            webView.tag = html
                            webView.loadDataWithBaseURL("https://example.com", html, "text/html", "UTF-8", null)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Settings Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkGray)
        ) {
            val context = androidx.compose.ui.platform.LocalContext.current
            
            ProfileMenuItem(title = "تحديث التطبيق", onClick = {
                android.widget.Toast.makeText(context, "البحث عن تحديثات...", android.widget.Toast.LENGTH_SHORT).show()
                // هنا يتم وضع منطق البحث عن الإصدار الجديد
            })
            
            val prefs = context.getSharedPreferences("DramaPrefs", android.content.Context.MODE_PRIVATE)
            var isNotificationsEnabled by remember { mutableStateOf(prefs.getBoolean("notifications_enabled", false)) }
            
            val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    isNotificationsEnabled = true
                    prefs.edit().putBoolean("notifications_enabled", true).apply()
                } else {
                    isNotificationsEnabled = false
                    prefs.edit().putBoolean("notifications_enabled", false).apply()
                    if (!androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(context as android.app.Activity, android.Manifest.permission.POST_NOTIFICATIONS)) {
                        android.widget.Toast.makeText(context, "الرجاء تفعيل الإشعارات من إعدادات الهاتف", android.widget.Toast.LENGTH_LONG).show()
                    }
                }
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الإشعارات (عند إضافة حلقات جديدة)",
                    color = WhiteText,
                    fontSize = 15.sp
                )
                Switch(
                    checked = isNotificationsEnabled,
                    onCheckedChange = { isChecked ->
                        if (isChecked && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                isNotificationsEnabled = true
                                prefs.edit().putBoolean("notifications_enabled", true).apply()
                            }
                        } else {
                            isNotificationsEnabled = isChecked
                            prefs.edit().putBoolean("notifications_enabled", isChecked).apply()
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = WhiteText,
                        checkedTrackColor = Crimson
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ProfileMenuItem(
    title: String,
    onClick: () -> Unit = { },
    iconOrValue: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = WhiteText,
            fontSize = 15.sp
        )
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (iconOrValue != null) {
                iconOrValue()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = null,
                tint = LightGrayInfo,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
