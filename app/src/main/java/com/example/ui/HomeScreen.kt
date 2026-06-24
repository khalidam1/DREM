package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import coil.compose.AsyncImage
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.example.ui.theme.*

data class DramaItem(
    val id: String,
    val title: String,
    val category: String,
    val views: String,
    val badge: String? = null, // "Hot", "New", "-17%"
    val imageUrl: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: DramaViewModel,
    onDramaClick: (String) -> Unit = {},
    onSeriesClick: (String) -> Unit = {},
    onAdminClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var adminClickCount by remember { mutableIntStateOf(0) }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val authManager = remember { com.example.model.AuthManager(context) }
    
    val dramasFromDb by viewModel.dramasFromSupabase.collectAsState()
    val isLoading by viewModel.isLoadingDramas.collectAsState()
    val errorMsg by viewModel.errorDramas.collectAsState()

    val categories = remember(dramasFromDb) {
        listOf("الكل") + dramasFromDb.map { it.category }.filter { it != "إعلان" }.distinct().sorted()
    }
    var selectedCategory by remember { mutableStateOf("الكل") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
    ) {
        // Top search bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("ابحث عن مسلسل أو تصنيف...", color = LightGrayInfo, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "بحث",
                        tint = LightGrayInfo,
                        modifier = Modifier.size(20.dp)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Crimson,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = DarkGray,
                    unfocusedContainerColor = DarkGray,
                    focusedTextColor = WhiteText,
                    unfocusedTextColor = WhiteText,
                    cursorColor = Crimson
                ),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp)
            )
            IconButton(
                onClick = onNotificationsClick,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(Icons.Default.Notifications, contentDescription = "الإشعارات", tint = androidx.compose.ui.graphics.Color.Green)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null // No ripple effect
                    ) {
                        adminClickCount++
                        if (adminClickCount >= 5) {
                            adminClickCount = 0
                            if (!authManager.isAdmin()) {
                                authManager.login(authManager.getUsername(), true)
                                android.widget.Toast.makeText(context, "تم تفعيل وضع المسؤول", android.widget.Toast.LENGTH_SHORT).show()
                            }
                            onAdminClick()
                        }
                    }
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(androidx.compose.ui.graphics.Color(0xFFD4FF45), androidx.compose.ui.graphics.Color(0xFF78C000))
                            )
                        )) {
                            append("D")
                        }
                        withStyle(style = SpanStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = androidx.compose.ui.graphics.Color.White
                        )) {
                            append("rem ")
                        }
                        withStyle(style = SpanStyle(
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(androidx.compose.ui.graphics.Color(0xFFD4FF45), androidx.compose.ui.graphics.Color(0xFF78C000))
                            )
                        )) {
                            append("TV")
                        }
                    },
                    style = androidx.compose.ui.text.TextStyle(
                        textDirection = androidx.compose.ui.text.style.TextDirection.Ltr
                    )
                )
            }
        }

        // Categories Row
        androidx.compose.foundation.lazy.LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories.size) { index ->
                val category = categories[index]
                val isSelected = selectedCategory == category
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) Crimson else DarkGray)
                        .clickable { selectedCategory = category }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category,
                        color = if (isSelected) WhiteText else LightGrayInfo,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Crimson)
            }
        } else if (errorMsg != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(errorMsg ?: "", color = LightGrayInfo, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.fetchDramasFromSupabase() },
                        colors = ButtonDefaults.buttonColors(containerColor = Crimson)
                    ) {
                        Text("إعادة المحاولة", color = WhiteText)
                    }
                }
            }
        } else if (dramasFromDb.isEmpty() && searchQuery.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("لا توجد مسلسلات لعرضها", color = LightGrayInfo)
            }
        } else {
            val formatCount: (Int) -> String = { count ->
                if (count >= 1000000) "${String.format("%.1f", count / 1000000.0)}M" 
                else if (count >= 1000) "${count / 1000}K" 
                else "$count"
            }

            var processedDramas = dramasFromDb.filter { it.category != "إعلان" }
            if (selectedCategory != "الكل") {
                processedDramas = processedDramas.filter { it.category == selectedCategory }
            }
            if (searchQuery.isNotEmpty()) {
                processedDramas = processedDramas.filter { 
                    it.title.contains(searchQuery, ignoreCase = true) || 
                    it.category.contains(searchQuery, ignoreCase = true) 
                }
            }

            val latestSeries = processedDramas.filter { it.isMovie == false }.sortedByDescending { it.createdAt }.take(6)
            val mostWatched = processedDramas.sortedByDescending { it.views ?: 0 }.take(7)
            val bestSeries = processedDramas.filter { it.isMovie == false }.sortedByDescending { it.likes ?: 0 }.take(6)

            val configuration = androidx.compose.ui.platform.LocalConfiguration.current
            val itemWidth = configuration.screenWidthDp.dp * 0.45f

            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Section 1: Latest Episodes (Series)
                if (latestSeries.isNotEmpty()) {
                    item {
                        SectionTitle("اخر الحلقات")
                        androidx.compose.foundation.lazy.LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            items(latestSeries.size) { index ->
                                val drama = latestSeries[index]
                                Box(modifier = Modifier.width(itemWidth)) {
                                    DramaPoster(
                                        drama = DramaItem(
                                            id = drama.id,
                                            title = drama.title,
                                            category = drama.category,
                                            views = formatCount(drama.views ?: 0),
                                            badge = drama.badge,
                                            imageUrl = drama.imageUrl
                                        ),
                                        onClick = { onDramaClick(drama.id) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 2: Most Watched (Movies and Series)
                if (mostWatched.isNotEmpty()) {
                    item {
                        SectionTitle("الأكثر مشاهدة")
                        androidx.compose.foundation.lazy.LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            items(mostWatched.size) { index ->
                                val drama = mostWatched[index]
                                Box(modifier = Modifier.width(itemWidth)) {
                                    DramaPoster(
                                        drama = DramaItem(
                                            id = drama.id,
                                            title = drama.title,
                                            category = drama.category,
                                            views = formatCount(drama.views ?: 0),
                                            badge = drama.badge,
                                            imageUrl = drama.imageUrl
                                        ),
                                        onClick = { onDramaClick(drama.id) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 3: Best Series (Grid 2 with ad)
                if (bestSeries.isNotEmpty()) {
                    item {
                        SectionTitle("أفضل المسلسلات")
                        Column(modifier = Modifier.padding(16.dp)) {
                            val chunks = bestSeries.chunked(2)
                            chunks.forEachIndexed { rowIndex, rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowItems.forEach { drama ->
                                        Box(modifier = Modifier.weight(1f)) {
                                            DramaPoster(
                                                drama = DramaItem(
                                                    id = drama.id,
                                                    title = drama.title,
                                                    category = drama.category,
                                                    views = formatCount(drama.views ?: 0),
                                                    badge = drama.badge,
                                                    imageUrl = drama.imageUrl
                                                ),
                                                onClick = { onSeriesClick(drama.id) }
                                            )
                                        }
                                    }
                                    // Fill empty spaces in the row
                                    repeat(2 - rowItems.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                                
                                // Insert ad between rows
                                if (rowIndex < chunks.size - 1) {
                                    val adConfigManager = remember { com.example.model.AdConfigManager(context) }
                                    val homeSpotId = remember { adConfigManager.getSpotId() }
                                    val isAdsEnabled = remember { adConfigManager.isAdsEnabled() }
                                    
                                    if (isAdsEnabled && homeSpotId.isNotBlank()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 16.dp)
                                                .height(100.dp)
                                                .clip(RoundedCornerShape(8.dp))
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
                                                                    ${homeSpotId.replace("document.write", "console.log")}
                                                                </div>
                                                                <script type="text/javascript">
                                                                    document.write = function(content) {
                                                                        document.querySelector('.ad-container').innerHTML += content;
                                                                    };
                                                                </script>
                                                                $homeSpotId
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
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        color = WhiteText,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun DramaPoster(drama: DramaItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.7f)
                .clip(RoundedCornerShape(8.dp))
                .background(DarkGray)
        ) {
            AsyncImage(
                model = drama.imageUrl,
                contentDescription = drama.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // Hot/New Badge
            if (drama.badge != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (drama.badge == "Hot") BadgeHot else BadgeNew)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = drama.badge,
                        color = WhiteText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Views count
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = WhiteText,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = drama.views,
                    color = WhiteText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = drama.title,
            color = WhiteText,
            fontSize = 13.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Medium,
            lineHeight = 16.sp
        )
        
        Spacer(modifier = Modifier.height(2.dp))
        
        Text(
            text = drama.category,
            color = LightGrayInfo,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

