package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(viewModel: DramaViewModel, onBack: () -> Unit, onDramaClick: (String) -> Unit) {
    val dramas by viewModel.dramasFromSupabase.collectAsState()
    
    // Create notifications based on the latest added dramas within 24 hours
    val now = System.currentTimeMillis()
    val recentDramas = dramas.filter { drama ->
        if (drama.category == "إعلان") return@filter false
        var isWithin24Hours = false
        try {
            drama.createdAt?.let { createdAtStr ->
                val instant = java.time.Instant.parse(createdAtStr)
                isWithin24Hours = (now - instant.toEpochMilli()) <= 24 * 60 * 60 * 1000L
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        isWithin24Hours
    }
        .sortedByDescending { it.createdAt }
        .take(50)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الإشعارات", color = WhiteText, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "عودة", tint = WhiteText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PureBlack)
            )
        },
        containerColor = PureBlack
    ) { paddingValues ->
        if (recentDramas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text(text = "لا توجد إشعارات حالياً", color = LightGrayInfo)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(recentDramas) { drama ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkGray)
                            .clickable { onDramaClick(drama.id) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(PureBlack),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = drama.imageUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "تم إضافة ${if (drama.isMovie == true) "فيلم" else "مسلسل"} جديد!",
                                color = Crimson,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = drama.title,
                                color = WhiteText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = LightGrayInfo, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}
