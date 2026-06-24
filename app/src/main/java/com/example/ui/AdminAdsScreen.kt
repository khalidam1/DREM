package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AdConfigManager
import com.example.model.supabase.SupabaseAdConfig
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAdsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val adConfigManager = remember { AdConfigManager(context) }
    val scope = rememberCoroutineScope()
    
    var adsEnabled by remember { mutableStateOf(true) }
    var spotId by remember { mutableStateOf("") }
    var profileAdsEnabled by remember { mutableStateOf(true) }
    var profileSpotId by remember { mutableStateOf("") }
    var forYouAdsEnabled by remember { mutableStateOf(true) }
    var forYouSpotId by remember { mutableStateOf("") }
    var currentConfig by remember { mutableStateOf<SupabaseAdConfig?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var saveStatus by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val config = adConfigManager.getFullConfigFromSupabase()
        if (config != null) {
            currentConfig = config
            adsEnabled = config.adsEnabled
            spotId = config.spotId
            profileAdsEnabled = config.profileAdsEnabled
            profileSpotId = config.profileSpotId
            forYouAdsEnabled = config.forYouAdsEnabled
            forYouSpotId = config.forYouSpotId
        } else {
            // Provide deafult setup if none exists
            currentConfig = SupabaseAdConfig()
        }
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .statusBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "عودة",
                    tint = WhiteText
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "إدارة الإعلانات عن بعد",
                color = WhiteText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Crimson)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "تفعيل الإعلانات في التطبيق",
                            color = WhiteText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Switch(
                            checked = adsEnabled,
                            onCheckedChange = { adsEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Crimson, checkedTrackColor = CrimsonDark)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    Text(
                        text = "كود الإعلان كامل (OVERLAY) لصفحة مشاهدة الفيلم/المسلسل",
                        color = WhiteText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = spotId,
                        onValueChange = { spotId = it },
                        minLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Crimson,
                            unfocusedBorderColor = DarkGray,
                            focusedTextColor = WhiteText,
                            unfocusedTextColor = WhiteText
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                item {
                    Divider(color = DarkGray, modifier = Modifier.padding(vertical = 16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "تفعيل إعلان صفحة الحساب",
                            color = WhiteText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Switch(
                            checked = profileAdsEnabled,
                            onCheckedChange = { profileAdsEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Crimson, checkedTrackColor = CrimsonDark)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    Text(
                        text = "كود الإعلان كامل لصفحة الحساب (HTML/JS)",
                        color = WhiteText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = profileSpotId,
                        onValueChange = { profileSpotId = it },
                        minLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Crimson,
                            unfocusedBorderColor = DarkGray,
                            focusedTextColor = WhiteText,
                            unfocusedTextColor = WhiteText
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                item {
                    Divider(color = DarkGray, modifier = Modifier.padding(vertical = 16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "تفعيل إعلان قسم من أجلك",
                            color = WhiteText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Switch(
                            checked = forYouAdsEnabled,
                            onCheckedChange = { forYouAdsEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Crimson, checkedTrackColor = CrimsonDark)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    Text(
                        text = "رابط VAST (VAST Tag URL) لقسم من أجلك",
                        color = WhiteText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = forYouSpotId,
                        onValueChange = { forYouSpotId = it },
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Crimson,
                            unfocusedBorderColor = DarkGray,
                            focusedTextColor = WhiteText,
                            unfocusedTextColor = WhiteText
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                item {
                    if (saveStatus.isNotEmpty()) {
                        Text(text = saveStatus, color = if (saveStatus.contains("نجاح")) LightGrayInfo else Crimson)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                val configToSave = currentConfig?.copy(
                                    adsEnabled = adsEnabled,
                                    spotId = spotId,
                                    profileAdsEnabled = profileAdsEnabled,
                                    profileSpotId = profileSpotId,
                                    forYouAdsEnabled = forYouAdsEnabled,
                                    forYouSpotId = forYouSpotId,
                                    updatedAt = System.currentTimeMillis().toString()
                                ) ?: SupabaseAdConfig(
                                    adsEnabled = adsEnabled,
                                    spotId = spotId,
                                    profileAdsEnabled = profileAdsEnabled,
                                    profileSpotId = profileSpotId,
                                    forYouAdsEnabled = forYouAdsEnabled,
                                    forYouSpotId = forYouSpotId,
                                    updatedAt = System.currentTimeMillis().toString()
                                )
                                
                                saveStatus = "جاري الحفظ ونشر الإعدادات..."
                                val (success, message) = adConfigManager.publishConfig(configToSave)
                                saveStatus = message
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Crimson),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Publish Ad Configuration", color = WhiteText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
