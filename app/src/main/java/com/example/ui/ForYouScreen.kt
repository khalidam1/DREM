package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.*
import kotlinx.coroutines.launch

sealed class ReelItem {
    data class MovieAd(val drama: com.example.model.supabase.SupabaseDrama) : ReelItem()
}

@Composable
fun ForYouScreen(viewModel: DramaViewModel, onDramaClick: (String) -> Unit) {
    val dramasFromDb by viewModel.dramasFromSupabase.collectAsState()
    val ads = dramasFromDb.filter { it.category == "إعلان" }
    
    var selectedReelIndex by remember { mutableStateOf<Int?>(null) }
    
    val reelItems = remember(ads) {
        val items = mutableListOf<ReelItem>()
        ads.forEach { ad ->
            items.add(ReelItem.MovieAd(ad))
        }
        items
    }
    
    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val adConfigManager = remember { com.example.model.AdConfigManager(context) }
    
    var forYouAdsEnabled by remember { mutableStateOf(adConfigManager.isForYouAdsEnabled()) }
    var forYouSpotId by remember { mutableStateOf(adConfigManager.getForYouSpotId()) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        try {
            adConfigManager.initializeConfig()
            forYouAdsEnabled = adConfigManager.isForYouAdsEnabled()
            forYouSpotId = adConfigManager.getForYouSpotId()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
    ) {
        if (selectedReelIndex == null) {
            // Grid View
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "إعلانات الأفلام والمسلسلات",
                        color = WhiteText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                
                if (ads.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                        Text(text = "لا توجد إعلانات لعرضها", color = LightGrayInfo)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(ads) { index, ad ->
                            Box(
                                modifier = Modifier
                                    .aspectRatio(2f / 3f)
                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                                    .clickable {
                                        val numMonetizationAdsBefore = index / 3
                                        selectedReelIndex = index + numMonetizationAdsBefore
                                    }
                            ) {
                                AsyncImage(
                                    model = ad.imageUrl,
                                    contentDescription = ad.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Reels View
            val configuration = androidx.compose.ui.platform.LocalConfiguration.current
            val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
            
            val pagerState = rememberPagerState(
                initialPage = selectedReelIndex ?: 0,
                pageCount = { reelItems.size }
            )
            
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = !isLandscape, // Disable scrolling while in landscape video
                beyondViewportPageCount = 1
            ) { page ->
                val item = reelItems[page]
                Box(modifier = Modifier.fillMaxSize()) {
                    when (item) {
                        is ReelItem.MovieAd -> {
                            val ad = item.drama
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(PureBlack)
                                    .let {
                                        if (!isLandscape) it.statusBarsPadding() else it
                                    }
                            ) {
                                // --- Video Player Container (Same as PlayerScreen) ---
                                Box(
                                    modifier = if (isLandscape) {
                                        Modifier.fillMaxSize().background(PureBlack)
                                    } else {
                                        Modifier.fillMaxWidth().aspectRatio(16f / 9f).background(PureBlack)
                                    }
                                ) {
                                    if (kotlin.math.abs(pagerState.currentPage - page) <= 1) {
                                        VideoPlayerView(embedCode = ad.movieEmbedCode, showAd = forYouAdsEnabled, customSpotId = forYouSpotId)
                                    } else {
                                        Box(modifier = Modifier.fillMaxSize().background(PureBlack))
                                    }
                                }
                                
                                if (!isLandscape) {
                                    // --- Content Details Below the Video ---
                                    androidx.compose.foundation.lazy.LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        contentPadding = PaddingValues(16.dp)
                                    ) {
                                        item {
                                            Text(
                                                text = ad.title,
                                                color = WhiteText,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            
                                            Text(
                                                text = ad.description ?: "",
                                                color = WhiteText.copy(alpha = 0.9f),
                                                fontSize = 14.sp,
                                                lineHeight = 22.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            if (!isLandscape) {
                // Close Button for Reels Mode
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(16.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    IconButton(
                        onClick = { selectedReelIndex = null },
                        modifier = Modifier.background(PureBlack.copy(alpha = 0.5f), androidx.compose.foundation.shape.CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = WhiteText)
                    }
                }
            }
        }
    }
}
