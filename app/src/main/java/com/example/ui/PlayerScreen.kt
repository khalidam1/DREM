package com.example.ui

import android.content.Intent
import android.content.ActivityNotFoundException
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(dramaId: String, initialEpisodeIndex: Int = 0, onBack: () -> Unit, onLoginClick: () -> Unit, onAllEpisodesClick: () -> Unit = {}, viewModel: DramaViewModel) {
    val drama by viewModel.fetchSupabaseDramaDetails(dramaId).collectAsState(initial = null)
    val episodes by viewModel.fetchEpisodesFromSupabase(dramaId).collectAsState(initial = emptyList())
    val isMovie = drama?.isMovie ?: false
    val maxEpisodes = if (isMovie) 1 else if (episodes.isNotEmpty()) episodes.size else 10
    
    var currentEpisodeIndex by remember(initialEpisodeIndex) { mutableStateOf(initialEpisodeIndex) }
    
    var showCommentsSheet by remember { mutableStateOf(false) }
    var showEpisodesSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    var localViews by remember(drama?.id) { mutableStateOf(drama?.views ?: 0) }
    var localLikes by remember(drama?.id) { mutableStateOf(drama?.likes ?: 0) }
    var userLiked by remember(drama?.id) { mutableStateOf(false) }
    var viewsIncremented by remember(drama?.id) { mutableStateOf(false) }

    LaunchedEffect(drama?.id) {
        val currentDrama = drama
        if (currentDrama?.id != null && !viewsIncremented) {
            viewsIncremented = true
            localViews = (currentDrama.views ?: 0) + 1
            viewModel.incrementViews(currentDrama.id, currentDrama.views ?: 0)
        }
    }
    
    val favoriteDrama by viewModel.isFavorite(dramaId).collectAsState(initial = null)
    val isMyList = favoriteDrama != null

    val shareAction = {
        val shareText = if (isMovie) {
            "شاهد هذا الفيلم الرائع على دراما بوكس! \nhttps://dramaboxarabia.com/play/$dramaId/"
        } else {
            "شاهد الحلقة ${currentEpisodeIndex + 1} من هذا المسلسل الرائع على دراما بوكس! \nhttps://dramaboxarabia.com/play/$dramaId/?episode=${currentEpisodeIndex + 1}"
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            setPackage("com.whatsapp")
        }
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            val fallback = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            context.startActivity(Intent.createChooser(fallback, "مشاركة"))
        }
    }

    val activeEmbedCode = if (isMovie) drama?.movieEmbedCode else if (episodes.isNotEmpty() && currentEpisodeIndex < episodes.size) episodes[currentEpisodeIndex].embedCode else null
    
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isLandscape) PureBlack else DarkGray)
            .then(if (isLandscape) Modifier else Modifier.statusBarsPadding())
    ) {
        if (!isLandscape) {
            // App Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "عودة", tint = WhiteText)
                }
                Text(
                    text = drama?.title ?: "",
                    color = WhiteText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // --- Video Player Container ---
        Box(
            modifier = if (isLandscape) {
                Modifier.fillMaxSize().background(PureBlack)
            } else {
                Modifier.fillMaxWidth().aspectRatio(16f / 9f).background(PureBlack)
            }
        ) {
            VideoPlayerView(embedCode = activeEmbedCode)
        }

        if (!isLandscape) {
            // --- Content Details Below the Video ---
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    Text(
                        text = if (isMovie) drama?.title ?: "" else "الحلقة ${currentEpisodeIndex + 1}: ${drama?.title ?: ""}",
                    color = WhiteText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Likes
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(PureBlack)
                                .clickable {
                                    if (!userLiked) {
                                        localLikes += 1
                                        userLiked = true
                                        drama?.id?.let { viewModel.incrementLikes(it, drama?.likes ?: 0) }
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Icon(Icons.Filled.Favorite, contentDescription = "إعجابات", tint = if (userLiked) Crimson else WhiteText, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = formatCountLocal(localLikes), color = WhiteText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Views
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(PureBlack)
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = "مشاهدات", tint = WhiteText, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = formatCountLocal(localViews), color = WhiteText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    
                    // Comments
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(PureBlack)
                                .clickable { showCommentsSheet = true }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Comment, contentDescription = "تعليق", tint = WhiteText, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "تعليق", color = WhiteText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    
                    // Share
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(PureBlack)
                                .clickable { shareAction() }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = "مشاركة", tint = WhiteText, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "مشاركة", color = WhiteText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    
                    // My List
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isMyList) Crimson else PureBlack)
                                .clickable {
                                    drama?.let { 
                                        val item = DramaItem(
                                            id = it.id,
                                            title = it.title,
                                            category = it.category,
                                            views = "${it.views}",
                                            badge = it.badge,
                                            imageUrl = it.imageUrl
                                        )
                                        viewModel.toggleFavorite(item, isMyList) 
                                    } 
                                }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = if (isMyList) Icons.Filled.Check else Icons.Filled.Add,
                                contentDescription = "قائمتي",
                                tint = WhiteText,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "قائمتي", color = WhiteText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = DarkGray)
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Episodes List Button (for series)
            if (!isMovie && maxEpisodes > 1) {
                item {
                    Button(
                        onClick = { showEpisodesSheet = true },
                        colors = ButtonDefaults.buttonColors(containerColor = PureBlack),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("باقي الحلقات", color = WhiteText, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            
            // Description / Extra info
            item {
                Text(
                    text = "الوصف",
                    color = WhiteText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "شاهد التفاصيل المشوقة واستمتع بالأحداث المليئة بالمفاجآت. #دراما #تشويق",
                    color = LightGrayInfo,
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
            }
            
            // Add padding for bottom navigation theoretically, or just to make list scroll past the bottom properly.
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

    if (showCommentsSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showCommentsSheet = false },
            sheetState = sheetState,
            containerColor = DarkGray
        ) {
            CommentsSection(dramaId = dramaId, viewModel = viewModel, onLoginClick = {
                showCommentsSheet = false
                onLoginClick()
            })
        }
    }
    
    if (showEpisodesSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showEpisodesSheet = false },
            sheetState = sheetState,
            containerColor = DarkGray
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f)
                    .padding(16.dp)
            ) {
                Text(
                    text = "باقي الحلقات",
                    color = WhiteText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(minOf(maxEpisodes, 15)) { index ->
                        val isSelected = index == currentEpisodeIndex
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Crimson else PureBlack)
                                .clickable {
                                    currentEpisodeIndex = index
                                    showEpisodesSheet = false
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "الحلقة ${index + 1}",
                                color = if (isSelected) WhiteText else LightGrayInfo,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                if (maxEpisodes > 15) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            showEpisodesSheet = false
                            onAllEpisodesClick()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Crimson),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("جميع الحلقات", color = WhiteText, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

fun formatCountLocal(count: Int): String {
    return if (count >= 1000) String.format("%.1fk", count / 1000.0)
    else "$count"
}

@Composable
fun VideoPlayerView(embedCode: String?, showAd: Boolean = true, customSpotId: String? = null) {
    val activeUrl = embedCode?.trim() ?: "https://voe.sx/6sxngkdskkfw"
    
    // Check if the content should be rendered in WebView
    val isHtml = activeUrl.startsWith("<") || activeUrl.contains("<iframe") || activeUrl.contains("<script")
    val isWebPage = activeUrl.startsWith("http") && !activeUrl.endsWith(".mp4") && !activeUrl.endsWith(".m3u8") && !activeUrl.contains(".mp4?") && !activeUrl.contains(".m3u8?")
    val useWebView = isHtml || isWebPage
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? androidx.activity.ComponentActivity
    val adConfigManager = remember { com.example.model.AdConfigManager(context) }
    
    var customView by remember { mutableStateOf<android.view.View?>(null) }
    var customViewCallback by remember { mutableStateOf<android.webkit.WebChromeClient.CustomViewCallback?>(null) }
    
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val view = androidx.compose.ui.platform.LocalView.current
    
    var lastLandscapeBackPressTime by remember { mutableLongStateOf(0L) }
    androidx.activity.compose.BackHandler(enabled = isLandscape && customView == null) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastLandscapeBackPressTime < 2000) {
            activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            lastLandscapeBackPressTime = currentTime
            android.widget.Toast.makeText(context, "اضغط مرة أخرى للخروج من الشاشة الكبيرة", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
    
    androidx.compose.runtime.DisposableEffect(isLandscape, customView != null) {
        val window = activity?.window
        val insetsController = window?.let { androidx.core.view.WindowCompat.getInsetsController(it, view) }
        if (isLandscape || customView != null) {
            insetsController?.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            insetsController?.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            insetsController?.show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
        }
        onDispose {
            insetsController?.show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
        }
    }
    
    val audioAttributes = remember {
        androidx.media3.common.AudioAttributes.Builder()
            .setUsage(androidx.media3.common.C.USAGE_MEDIA)
            .setContentType(androidx.media3.common.C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()
    }

    val exoPlayer = remember(activeUrl) {
        androidx.media3.exoplayer.ExoPlayer.Builder(context).build().apply {
            if (!useWebView) {
                val mediaItem = androidx.media3.common.MediaItem.fromUri(activeUrl)
                setMediaItem(mediaItem)
                setAudioAttributes(audioAttributes, true)
                prepare()
            }
        }
    }

    LaunchedEffect(useWebView) {
        if (!useWebView) {
            exoPlayer.playWhenReady = true
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }

    val adScript = if (showAd && adConfigManager.isAdsEnabled()) {
        customSpotId ?: adConfigManager.getSpotId()
    } else {
        ""
    }

    val htmlContent = if (isHtml) {
        "<html><head><meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no'><style>iframe { width: 100% !important; height: 100% !important; border: none; }</style></head><body style='margin:0;padding:0;background-color:#000;display:flex;justify-content:center;align-items:center;width:100vw;height:100vh;overflow:hidden;'>${adScript}<div style='width:100%;height:100%;display:flex;'>$activeUrl</div></body></html>"
    } else if (isWebPage) {
        "<html><head><meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no'></head><body style='margin:0;padding:0;background-color:#000;display:flex;justify-content:center;align-items:center;width:100vw;height:100vh;overflow:hidden;'>${adScript}<iframe src='$activeUrl' width='100%' height='100%' frameborder='0' allowfullscreen style='width:100%;height:100%;border:none;'></iframe></body></html>"
    } else ""

    Box(modifier = Modifier.fillMaxSize()) {
        // --- Main Video Content ---
        if (useWebView) {
            AndroidView(
                factory = { ctx ->
                    android.webkit.WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.mediaPlaybackRequiresUserGesture = false
                        settings.setSupportMultipleWindows(true)
                        settings.javaScriptCanOpenWindowsAutomatically = true
                        webChromeClient = object : android.webkit.WebChromeClient() {
                            override fun onShowCustomView(view: android.view.View?, callback: CustomViewCallback?) {
                                super.onShowCustomView(view, callback)
                                customView = view
                                customViewCallback = callback
                                activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                            }

                            private var lastHideTime = 0L

                            override fun onHideCustomView() {
                                val currentTime = System.currentTimeMillis()
                                if (currentTime - lastHideTime < 2000) {
                                    super.onHideCustomView()
                                    customView = null
                                    customViewCallback?.onCustomViewHidden()
                                    activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                } else {
                                    lastHideTime = currentTime
                                    android.widget.Toast.makeText(context, "اضغط مرة أخرى للخروج من الشاشة الكبيرة", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        webViewClient = android.webkit.WebViewClient()
                        layoutParams = android.view.ViewGroup.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                update = { webView ->
                    if (webView.tag != htmlContent) {
                        webView.tag = htmlContent
                        webView.loadDataWithBaseURL("https://example.com", htmlContent, "text/html", "UTF-8", null)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            AndroidView(
                factory = { ctx ->
                    androidx.media3.ui.PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = true
                        layoutParams = android.view.ViewGroup.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                update = { playerView ->
                    playerView.player = exoPlayer
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
    
    if (customView != null) {
        var lastBackPressTime by remember { mutableLongStateOf(0L) }
        
        androidx.activity.compose.BackHandler {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastBackPressTime < 2000) {
                customViewCallback?.onCustomViewHidden()
                customView = null
                activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else {
                lastBackPressTime = currentTime
                android.widget.Toast.makeText(context, "اضغط مرة أخرى للخروج من وضع ملء الشاشة", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PureBlack)
        ) {
            AndroidView(
                factory = { customView!! },
                modifier = Modifier.fillMaxSize()
            )
            // Close button that requires two clicks
            IconButton(
                onClick = {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastBackPressTime < 2000) {
                        customViewCallback?.onCustomViewHidden()
                        customView = null
                        activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    } else {
                        lastBackPressTime = currentTime
                        android.widget.Toast.makeText(context, "اضغط مرة أخرى للخروج من وضع ملء الشاشة", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(DarkGray.copy(alpha = 0.5f), androidx.compose.foundation.shape.CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close Fullscreen", tint = WhiteText)
            }
        }
    }
}

@Composable
fun CommentsSection(dramaId: String, viewModel: DramaViewModel, onLoginClick: () -> Unit) {
    var newComment by remember { mutableStateOf("") }
    
    val commentsFlow = remember(dramaId) { viewModel.fetchComments(dramaId) }
    val comments by commentsFlow.collectAsState(initial = emptyList())
    var localComments by remember(comments) { mutableStateOf(comments) }
    val context = LocalContext.current
    val authManager = remember { com.example.model.AuthManager(context) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.6f) 
            .padding(16.dp)
            .imePadding()
    ) {
        Text(
            text = "التعليقات (${localComments.size})",
            color = WhiteText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(localComments.size) { index ->
                val comment = localComments[index]
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Crimson),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = comment.userName.take(1),
                            color = WhiteText,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = comment.userName,
                            color = LightGrayInfo,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = comment.text,
                            color = WhiteText,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (authManager.isLoggedIn()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newComment,
                    onValueChange = { newComment = it },
                    placeholder = { Text("أضف تعليقاً...", color = LightGrayInfo) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Crimson,
                        unfocusedBorderColor = PureBlack,
                        focusedTextColor = WhiteText,
                        unfocusedTextColor = WhiteText,
                        cursorColor = Crimson
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { 
                        if (newComment.isNotBlank()) {
                            val userName = authManager.getUsername()
                            val newCommentObj = com.example.model.supabase.SupabaseComment(
                                dramaId = dramaId,
                                userName = userName,
                                text = newComment,
                                identifier = userName
                            )
                            // Optimistic UI update
                            localComments = listOf(newCommentObj) + localComments
                            
                            viewModel.postComment(dramaId, newComment, userName) {
                                // On success, we can refresh or let optimistic UI stand
                            }
                            newComment = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Crimson)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "إرسال",
                        tint = WhiteText
                    )
                }
            }
        } else {
            Button(
                onClick = onLoginClick,
                colors = ButtonDefaults.buttonColors(containerColor = Crimson),
                modifier = Modifier.fillMaxWidth().height(50.dp).padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("سجل الدخول لإضافة تعليقك", color = WhiteText, fontWeight = FontWeight.Bold)
            }
        }
    }
}
