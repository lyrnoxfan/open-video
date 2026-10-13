package com.example.lxvideo.ui

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Environment
import android.view.ViewConfiguration
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.example.lxvideo.R
import com.example.lxvideo.data.PlayerCache
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.lxvideo.data.VideoItem
import com.example.lxvideo.data.VideoStore
import com.example.lxvideo.ui.theme.Blue
import com.example.lxvideo.ui.theme.FavGold
import com.example.lxvideo.ui.theme.PinkBlueBrush
import com.example.lxvideo.ui.theme.Sub
import com.example.lxvideo.ui.theme.TimeStyle
import com.example.lxvideo.ui.theme.TimeTotalStyle
import com.example.lxvideo.ui.theme.glassDark
import com.example.lxvideo.util.fmtSize
import com.example.lxvideo.util.fmtTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/** 底部悬浮导航预留，避免遮挡进度条 */
private val NavBarReserve = 100.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoFeedScreen(
    vm: VideoFeedViewModel,
    paused: Boolean,
    jumpId: String?,
    onJumpDone: () -> Unit,
    isFullscreen: Boolean,
    onFullscreenChange: (Boolean) -> Unit,
    onZoomedChange: (Boolean) -> Unit,
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val store = remember(ctx) { VideoStore.get(ctx) }
    val activity = ctx as? Activity

    // 双指缩放状态（hoist 到本层：翻页/退出全屏时重置；>1x 时禁用翻页并上报给 MainActivity 藏底栏）
    val zoom = remember { ZoomState() }
    val zoomed = zoom.zoomed
    LaunchedEffect(zoomed) { onZoomedChange(zoomed) }

    LaunchedEffect(Unit) {
        if (vm.videos.isEmpty() && !vm.loading) vm.refresh()
    }

    // 单个 ExoPlayer 实例，页间复用；离开页面释放。
    // 磁盘缓存 2GB：播过的内容按分片存本地，重复看/拖进度直接读本地。
    // 缓存 key 用视频 id（签名 URL 7 天一换，id 稳定，缓存长期有效）。
    // 缓冲策略按慢网络调优：尽快起播（1.5s），播起来后多囤（30s~120s）扛抖动。
    val player = remember(ctx) {
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(30_000, 120_000, 1_500, 3_000)
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()
        val cacheFactory = CacheDataSource.Factory()
            .setCache(PlayerCache.get(ctx.applicationContext))
            .setUpstreamDataSourceFactory(
                DefaultHttpDataSource.Factory().setUserAgent(
                    "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/120 Mobile Safari/537.36 LXVideo/1.3.1"
                )
            )
        ExoPlayer.Builder(ctx)
            .setLoadControl(loadControl)
            .setMediaSourceFactory(DefaultMediaSourceFactory(cacheFactory))
            .build()
    }
    DisposableEffect(Unit) {
        onDispose { player.release() }
    }

    // 切到"我"页时暂停播放（保留进度和页码）
    LaunchedEffect(paused) {
        if (paused) player.pause()
    }

    val videos = vm.videos
    // customCacheKey 用视频 id：签名 URL 会过期轮换，id 稳定，磁盘缓存长期有效
    val items = remember(videos) {
        videos.map { MediaItem.Builder().setUri(it.url).setCustomCacheKey(it.id).build() }
    }
    val pagerState = rememberPagerState(pageCount = { videos.size })
    val page = pagerState.currentPage
    var lastPage by remember { mutableStateOf(0) }

    // 切页：先保存切走页面的进度；换 mediaItem；断点续播；自动播放；缩放重置回 1x
    LaunchedEffect(page, items) {
        if (items.isEmpty() || page >= items.size) return@LaunchedEffect
        zoom.reset()
        videos.getOrNull(lastPage)?.let { prev ->
            val p = player.currentPosition
            val d = player.duration
            store.saveWatch(prev.id, if (d > 0 && p >= d - 3000) 0L else p)
        }
        player.stop()
        player.clearMediaItems()
        player.setMediaItem(items[page])
        // 断点续播：上次看到超过 5 秒且没播完，就接着播
        val vid = videos[page]
        val saved = store.position(vid.id).first()
        if (saved > 5000) {
            player.seekTo(saved)
            Toast.makeText(ctx, "已从 ${fmtFeedMs(saved)} 继续", Toast.LENGTH_SHORT).show()
        }
        items.getOrNull(page + 1)?.let { player.addMediaItem(it) }
        player.prepare()
        // 切页重置为 1x（长按 2x 只在按住期间生效）
        player.playbackParameters = PlaybackParameters(1f)
        player.playWhenReady = true
        vm.setIndex(page)
        lastPage = page
    }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    var searching by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var showList by remember { mutableStateOf(false) }
    var showLib by remember { mutableStateOf(false) }
    // 控制层（顶栏/右侧栏/底部条）显隐：点按显示，播放中 3 秒无操作自动淡出
    var controlsVisible by remember { mutableStateOf(true) }

    // 全屏极简控制层显隐（轻点画面切换，3 秒无操作自动淡出）
    var fsControlsVisible by remember { mutableStateOf(false) }
    LaunchedEffect(isFullscreen, fsControlsVisible) {
        if (isFullscreen && fsControlsVisible) {
            delay(3000)
            fsControlsVisible = false
        }
    }

    /** 按当前视频横竖屏设置全屏方向：横屏视频转横屏，竖屏保持竖屏 */
    fun applyFsOrientation() {
        val vs = player.videoSize
        activity?.requestedOrientation = if (vs.width > vs.height && vs.height > 0) {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        }
    }

    // 进入/退出全屏：重置缩放、收起弹窗、切控制层、转方向
    LaunchedEffect(isFullscreen) {
        zoom.reset()
        if (isFullscreen) {
            showList = false
            showLib = false
            searching = false
            controlsVisible = false
            fsControlsVisible = true
            applyFsOrientation()
        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            controlsVisible = true
        }
    }

    // 全屏时视频元数据后到（如 moov 在尾部）：按实际横竖屏修正方向
    val fsNow by rememberUpdatedState(isFullscreen)
    DisposableEffect(player) {
        val sizeListener = object : Player.Listener {
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                if (fsNow && videoSize.width > 0) {
                    activity?.requestedOrientation =
                        if (videoSize.width > videoSize.height) {
                            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                        } else {
                            ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                        }
                }
            }
        }
        player.addListener(sizeListener)
        onDispose { player.removeListener(sizeListener) }
    }

    /** 跳到指定视频 id 所在的页 */
    fun jumpTo(id: String) {
        val i = videos.indexOfFirst { it.id == id }
        if (i < 0) {
            Toast.makeText(ctx, "视频不在当前列表", Toast.LENGTH_SHORT).show()
            return
        }
        showList = false
        showLib = false
        searching = false
        vm.setIndex(i)
        scope.launch { pagerState.animateScrollToPage(i) }
    }

    // 外部跳播请求（从"我"页点条目过来）
    LaunchedEffect(jumpId) {
        if (jumpId != null) {
            jumpTo(jumpId)
            onJumpDone()
        }
    }

    /** 打开抽屉里的收藏弹窗：先关抽屉再弹 */
    fun openSheet() {
        scope.launch {
            drawerState.close()
            showLib = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        // 不透的深色 scrim：防止底下视频页的控件透出虚影
        scrimColor = Color.Black.copy(alpha = 0.62f),
        drawerContent = {
            ModalDrawerSheet(
                // 毛玻璃质感：半透明纵向渐变（与顶栏胶囊统一），容器本身透明
                drawerContainerColor = Color.Transparent,
                drawerContentColor = Color.White,
                modifier = Modifier.statusBarsPadding(),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF182642).copy(alpha = 0.96f),
                                    Color(0xFF0B1220).copy(alpha = 0.98f),
                                ),
                            ),
                        ),
                ) {
                // 抽屉头部：粉蓝渐变标题 + 装饰线
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                ) {
                    Text(
                        "LX视频",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        style = TextStyle(
                            brush = PinkBlueBrush,
                        ),
                        letterSpacing = 1.sp,
                    )
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier.width(36.dp).height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(PinkBlueBrush),
                    )
                }
                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.08f),
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
                Spacer(Modifier.height(12.dp))
                DrawerRow(
                    icon = Icons.Filled.Shuffle,
                    label = "播放顺序",
                    trailing = if (vm.order == FeedOrder.SHUFFLE) "随机" else "最新",
                    onClick = {
                        vm.toggleOrder()
                        scope.launch {
                            drawerState.close()
                            pagerState.scrollToPage(0)
                        }
                    },
                )
                DrawerRow(
                    icon = Icons.AutoMirrored.Filled.List,
                    label = "选择视频",
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            showList = true
                        }
                    },
                )
                DrawerRow(
                    icon = Icons.Filled.Star,
                    label = "我的收藏",
                    onClick = { openSheet() },
                )
                }
            }
        },
    ) {
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            when {
                vm.loading && videos.isEmpty() -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    LoadingPlaceholder("正在加载视频…")
                }
                vm.error != null && videos.isEmpty() -> Box(
                    Modifier.fillMaxSize().padding(48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    ErrorPlaceholder(
                        message = vm.error ?: "加载失败",
                        icon = Icons.Filled.VideocamOff,
                        onRetry = { vm.refresh() },
                    )
                }
                videos.isEmpty() -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        EmptyHint(
                            text = "这里空空如也",
                            subtitle = "服务端还没有视频，去传一点吧",
                            icon = Icons.Filled.VideocamOff,
                        )
                        GlassTextButton("刷新", onClick = { vm.refresh() })
                    }
                }
                else -> {
                    VerticalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        // 放大时禁用翻页（单指改为拖动画布），回到 1x 恢复
                        userScrollEnabled = !zoomed,
                    ) { idx ->
                        FeedPage(
                            video = videos[idx],
                            player = if (idx == page) player else null,
                            isCurrent = idx == page,
                            store = store,
                            controlsVisible = controlsVisible,
                            onControlsChange = { controlsVisible = it },
                            onEnterFullscreen = { onFullscreenChange(true) },
                            isFullscreen = isFullscreen,
                            fsControlsVisible = fsControlsVisible,
                            onFsControlsChange = { fsControlsVisible = it },
                            onExitFullscreen = { onFullscreenChange(false) },
                            zoom = zoom,
                        )
                    }

                    // 顶栏：悬浮毛玻璃胶囊（左菜单 / 右全屏+搜索）；全屏或放大时隐藏
                    AnimatedVisibility(
                        visible = controlsVisible && !searching && !isFullscreen && !zoomed,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier.fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .height(52.dp)
                                .glassDark(999.dp, alpha = 0.38f)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Filled.Menu, "菜单", tint = Color.White)
                            }
                            Spacer(Modifier.weight(1f))
                            IconButton(onClick = { searching = true }) {
                                Icon(Icons.Filled.Search, "搜索", tint = Color.White)
                            }
                        }
                    }
                    if (searching) {
                        // 搜索模式：返回 + 输入框（毛玻璃胶囊）
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .glassDark(999.dp, alpha = 0.55f)
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(onClick = { searching = false; query = "" }) {
                                Icon(Icons.Filled.Close, "关闭搜索", tint = Color.White)
                            }
                            TextField(
                                value = query,
                                onValueChange = { query = it },
                                placeholder = { Text("搜索视频名", color = Sub, fontSize = 15.sp) },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = Blue,
                                ),
                                modifier = Modifier.weight(1f),
                            )
                        }
                        // 搜索结果：按视频名本地过滤
                        val results = remember(query, videos) {
                            if (query.isBlank()) emptyList()
                            else videos.filter { it.name.contains(query, ignoreCase = true) }
                        }
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth()
                                .padding(top = 104.dp)
                                .padding(horizontal = 12.dp)
                                .glassDark(20.dp, alpha = 0.72f),
                        ) {
                            if (results.isEmpty()) {
                                item {
                                    Box(
                                        Modifier.fillMaxWidth().padding(24.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text("没有匹配的视频", color = Sub, fontSize = 14.sp)
                                    }
                                }
                            } else {
                                items(results.size) { i ->
                                    val v = results[i]
                                    VideoEntryRow(
                                        title = v.name,
                                        subtitle = fmtSize(v.size),
                                        onClick = { jumpTo(v.id) },
                                    )
                                }
                            }
                            item { Spacer(Modifier.height(16.dp)) }
                        }
                    }
                }
            }

            // 选视频列表
            if (showList && videos.isNotEmpty()) {
                VideoListSheet(
                    title = "选择视频",
                    onDismiss = { showList = false },
                ) {
                    itemsIndexed(videos) { i, v ->
                        VideoEntryRow(
                            title = v.name,
                            subtitle = fmtSize(v.size),
                            onClick = {
                                showList = false
                                vm.setIndex(i)
                                scope.launch { pagerState.animateScrollToPage(i) }
                            },
                        )
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }

            // 收藏弹窗
            if (showLib && videos.isNotEmpty()) {
                VideoListSheet(
                    title = "我的收藏",
                    onDismiss = { showLib = false },
                ) {
                    item {
                        LibEntries(
                            videos = videos,
                            store = store,
                            onPick = { jumpTo(it) },
                        )
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

/** 抽屉里的一行 */
@Composable
private fun DrawerRow(
    icon: ImageVector,
    label: String,
    trailing: String? = null,
    onClick: () -> Unit,
) {
    NavigationDrawerItem(
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    label,
                    color = Color.White.copy(alpha = 0.92f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                )
                if (trailing != null) {
                    // 状态胶囊：粉蓝渐变描边
                    Box(
                        modifier = Modifier
                            .border(
                                1.dp,
                                PinkBlueBrush,
                                RoundedCornerShape(999.dp),
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            trailing,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            style = TextStyle(brush = PinkBlueBrush),
                        )
                    }
                }
            }
        },
        icon = {
            // 图标底：柔和圆角方块
            Box(
                modifier = Modifier.size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    icon,
                    null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(20.dp),
                )
            }
        },
        selected = false,
        onClick = onClick,
        colors = NavigationDrawerItemDefaults.colors(
            unselectedContainerColor = Color.Transparent,
        ),
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp).height(60.dp),
    )
}

/** 深色毛玻璃底部弹窗容器 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VideoListSheet(
    title: String?,
    onDismiss: () -> Unit,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF0D1420).copy(alpha = 0.94f),
        scrimColor = Color.Black.copy(alpha = 0.55f),
        dragHandle = {
            Box(
                Modifier.padding(top = 10.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.3f)),
            )
        },
    ) {
        if (title != null) {
            Text(
                title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        LazyColumn(Modifier.fillMaxWidth(), content = content)
    }
}

/** 收藏条目列表（抽屉弹窗用） */
@Composable
private fun LibEntries(
    videos: List<VideoItem>,
    store: VideoStore,
    onPick: (String) -> Unit,
) {
    val favIds by remember { store.favorites() }.collectAsState(initial = emptyList())
    val favs = remember(favIds, videos) {
        favIds.mapNotNull { id -> videos.find { it.id == id } }
    }
    if (favs.isEmpty()) {
        EmptyHint("还没有收藏")
    } else {
        favs.forEach { v ->
            VideoEntryRow(
                title = v.name,
                subtitle = fmtSize(v.size),
                onClick = { onPick(v.id) },
                trailing = {
                    Icon(Icons.Filled.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(18.dp))
                },
            )
        }
    }
}

/** 右侧操作栏按钮：毛玻璃圆钮 + 下方小字计数，激活态带光晕 */
@Composable
private fun ActionItem(
    icon: ImageVector? = null,
    painter: Painter? = null,
    desc: String,
    tint: Color = Color.White,
    glow: Color? = null,
    count: String? = null,
    onClick: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(50.dp)
                .then(if (glow != null) Modifier.shadow(8.dp, CircleShape, ambientColor = glow.copy(alpha = 0.35f)) else Modifier)
                .glassDark(999.dp, alpha = 0.42f, borderWidth = 1.5.dp)
                .pressScale(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            if (painter != null) {
                Icon(painter, desc, tint = tint, modifier = Modifier.size(26.dp))
            } else if (icon != null) {
                Icon(icon, desc, tint = tint, modifier = Modifier.size(26.dp))
            }
        }
        if (count != null) {
            Spacer(Modifier.height(2.dp))
            Text(
                count,
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f),
                    fontFeatureSettings = "tnum",
                ),
            )
        }
    }
}

/** 单页：当前页才挂载 player，切走后 detach */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedPage(
    video: VideoItem,
    player: ExoPlayer?,
    isCurrent: Boolean,
    store: VideoStore,
    controlsVisible: Boolean,
    onControlsChange: (Boolean) -> Unit,
    onEnterFullscreen: () -> Unit,
    isFullscreen: Boolean,
    fsControlsVisible: Boolean,
    onFsControlsChange: (Boolean) -> Unit,
    onExitFullscreen: () -> Unit,
    zoom: ZoomState,
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val fav by remember(video.id) { store.isFav(video.id) }.collectAsState(initial = false)
    val favTotal by remember { store.favorites() }.collectAsState(initial = emptyList())

    var playing by remember { mutableStateOf(false) }
    var buffering by remember { mutableStateOf(true) }
    var duration by remember { mutableLongStateOf(0L) }
    var position by remember { mutableLongStateOf(0L) }
    var buffered by remember { mutableLongStateOf(0L) }
    var seeking by remember { mutableStateOf(false) }
    var seekPos by remember { mutableLongStateOf(0L) }
    var errMsg by remember { mutableStateOf<String?>(null) }

    // 长按两侧 2x：播放区左/右约 1/3 宽区域长按 → 2x，松开恢复 1x
    var boxW by remember { mutableStateOf(0) }
    var boosting by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val viewConfig = remember(ctx) { ViewConfiguration.get(ctx) }

    // 播放中 3 秒无操作，控制层自动淡出；点按任意处唤回
    LaunchedEffect(controlsVisible, playing, isCurrent) {
        if (controlsVisible && playing && isCurrent && errMsg == null) {
            delay(3000)
            onControlsChange(false)
        }
    }

    DisposableEffect(player) {
        val p = player ?: return@DisposableEffect onDispose {}
        playing = p.isPlaying
        buffering = p.playbackState == Player.STATE_BUFFERING
        duration = p.duration.coerceAtLeast(0L)
        position = p.currentPosition
        buffered = p.bufferedPosition
        errMsg = p.playerError?.message
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                buffering = state == Player.STATE_BUFFERING
                if (state == Player.STATE_READY) {
                    duration = p.duration.coerceAtLeast(0L)
                    buffering = false
                }
            }
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                playing = isPlaying
            }
            override fun onPlayerError(e: PlaybackException) {
                errMsg = e.message
            }
        }
        p.addListener(listener)
        onDispose { p.removeListener(listener) }
    }

    LaunchedEffect(playing, seeking) {
        val p = player ?: return@LaunchedEffect
        while (playing && !seeking) {
            position = p.currentPosition
            buffered = p.bufferedPosition
            delay(500)
        }
    }

    // 播放中每 5 秒存一次进度，用于断点续播
    LaunchedEffect(isCurrent, player) {
        if (!isCurrent || player == null) return@LaunchedEffect
        while (true) {
            delay(5000)
            if (!player.isPlaying) continue
            val pos = player.currentPosition
            val d = player.duration
            store.saveWatch(video.id, if (d > 0 && pos >= d - 3000) 0L else pos)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
            .onSizeChanged { boxW = it.width }
            .pointerInput(isCurrent, errMsg, isFullscreen, fsControlsVisible) {
                // 单指手势：单击（播放/暂停）+ 两侧长按 2x。
                // 双指/放大拖动由内层 ZoomableVideo 消费，这里只观察不消费，互不干扰。
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val w = boxW
                    val inSide = w > 0 &&
                        (down.position.x < w / 3f || down.position.x > w * 2f / 3f)
                    val downPos = down.position
                    val slop = viewConfig.scaledTouchSlop.toFloat()
                    // 等待：超时=长按触发；提前松开=点按；位移超阈值/第二指按下=取消
                    val tap = withTimeoutOrNull(500L) {
                        var moved = false
                        do {
                            val event = awaitPointerEvent()
                            val pressed = event.changes.filter { it.pressed }
                            if (pressed.size > 1) return@withTimeoutOrNull "cancel"
                            if (!moved && (pressed.firstOrNull()?.position?.minus(downPos)
                                    ?.getDistance() ?: 0f) > slop
                            ) moved = true
                        } while (event.changes.any { it.pressed })
                        if (moved) "cancel" else "tap"
                    }
                    when {
                        tap == "tap" && isCurrent && errMsg == null -> {
                            // 单击：全屏里只切换极简控制层；普通模式先唤出控制层，再播放/暂停
                            if (isFullscreen) {
                                onFsControlsChange(!fsControlsVisible)
                            } else {
                                onControlsChange(true)
                                player?.let { if (it.isPlaying) it.pause() else it.play() }
                            }
                        }
                        tap == null && isCurrent && errMsg == null && inSide -> {
                            // 两侧长按：切 2x + 触觉反馈，松开恢复 1x
                            boosting = true
                            player?.playbackParameters = PlaybackParameters(2f)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            do {
                                val event = awaitPointerEvent()
                            } while (event.changes.any { it.pressed })
                            boosting = false
                            player?.playbackParameters = PlaybackParameters(1f)
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        // 可缩放播放器（TextureView + 双指缩放；放大时纯画面）
        ZoomableVideo(
            player = player,
            zoom = zoom,
            modifier = Modifier.fillMaxSize(),
        )

        if (isCurrent && buffering && errMsg == null) {
            RefinedSpinner()
        }

        // 暂停指示器：小号深色圆 + 白三角，200ms 淡入淡出（轻量化，不挡画面）
        AnimatedVisibility(
            visible = isCurrent && !playing && !buffering && errMsg == null,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200)),
        ) {
            Box(
                modifier = Modifier.size(72.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f))
                    .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
                    .shadow(12.dp, CircleShape, ambientColor = Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.PlayArrow, "播放", tint = Color.White, modifier = Modifier.size(34.dp))
            }
        }

        // 长按 2x 指示器：顶部中央，松开即消失
        AnimatedVisibility(
            visible = isCurrent && boosting && errMsg == null,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(150)),
            modifier = Modifier.align(Alignment.TopCenter),
        ) {
            Box(
                modifier = Modifier.statusBarsPadding()
                    .padding(top = 10.dp)
                    .glassDark(999.dp, alpha = 0.55f)
                    .padding(horizontal = 16.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("2x", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        errMsg?.let { msg ->
            if (isCurrent) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(40.dp),
                ) {
                    ErrorPlaceholder(
                        message = msg,
                        icon = Icons.Filled.VideocamOff,
                        onRetry = {
                            errMsg = null
                            player?.let {
                                it.seekTo(0)
                                it.prepare()
                                it.play()
                            }
                        },
                    )
                }
            }
        }

        // 右侧竖排操作栏：毛玻璃圆钮，点按控制层时淡入淡出
        AnimatedVisibility(
            visible = isCurrent && errMsg == null && controlsVisible && !isFullscreen && !zoom.zoomed,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.CenterEnd),
        ) {
            Column(
                modifier = Modifier.padding(end = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ActionItem(
                    icon = if (fav) Icons.Filled.Star else Icons.Filled.StarBorder,
                    desc = "收藏",
                    tint = if (fav) FavGold else Color.White,
                    glow = if (fav) FavGold else null,
                    count = favTotal.size.toString(),
                    onClick = { scope.launch { store.setFav(video.id, !fav) } },
                )
                Spacer(Modifier.height(12.dp))
                ActionItem(
                    icon = Icons.Filled.IosShare,
                    desc = "分享",
                    onClick = { shareVideo(ctx, video) },
                )
                Spacer(Modifier.height(12.dp))
                ActionItem(
                    icon = Icons.Filled.Download,
                    desc = "下载",
                    onClick = { downloadVideo(ctx, video) },
                )
                Spacer(Modifier.height(12.dp))
                ActionItem(
                    painter = painterResource(R.drawable.ic_fullscreen),
                    desc = "全屏",
                    onClick = onEnterFullscreen,
                )
            }
        }

        // 底部：等宽时间 + 发光细进度条（可拖动），随控制层淡入淡出
        AnimatedVisibility(
            visible = isCurrent && errMsg == null && controlsVisible && !isFullscreen && !zoom.zoomed,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                        ),
                    )
                    .padding(horizontal = 18.dp)
                    .padding(top = 26.dp, bottom = NavBarReserve),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val shownPos = if (seeking) seekPos else position
                val durF = duration.toFloat().coerceAtLeast(1f)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(fmtFeedMs(shownPos), style = TimeStyle)
                    Text(" / ", style = TimeTotalStyle)
                    Text(fmtFeedMs(duration), style = TimeTotalStyle)
                }
                Spacer(Modifier.height(4.dp))
                Slider(
                    value = shownPos.toFloat().coerceIn(0f, durF),
                    onValueChange = { seeking = true; seekPos = it.toLong() },
                    onValueChangeFinished = {
                        player?.seekTo(seekPos)
                        position = seekPos
                        seeking = false
                    },
                    valueRange = 0f..durF,
                    enabled = duration > 0,
                    thumb = {
                        // 白圆点 + 极淡外发光，拖动时放大 1.2 倍
                        val thumbScale by animateFloatAsState(
                            targetValue = if (seeking) 1.2f else 1f,
                            animationSpec = tween(150),
                            label = "thumbScale",
                        )
                        Box(
                            Modifier.size(15.dp)
                                .graphicsLayer {
                                    scaleX = thumbScale
                                    scaleY = thumbScale
                                }
                                .shadow(8.dp, CircleShape, ambientColor = Color.White.copy(alpha = 0.35f))
                                .background(Color.White, CircleShape),
                        )
                    },
                    track = { sliderState ->
                        Box(
                            Modifier.fillMaxWidth().height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                        ) {
                            // 底轨
                            Box(
                                Modifier.fillMaxSize()
                                    .background(Color.White.copy(alpha = 0.22f)),
                            )
                            // 已缓冲
                            val bufFrac = (buffered.toFloat() / durF).coerceIn(0f, 1f)
                            Box(
                                Modifier.fillMaxHeight()
                                    .fillMaxWidth(bufFrac)
                                    .background(Color.White.copy(alpha = 0.4f)),
                            )
                            // 已播放：粉蓝渐变
                            val playFrac = (sliderState.value / durF).coerceIn(0f, 1f)
                            Box(
                                Modifier.fillMaxHeight()
                                    .fillMaxWidth(playFrac)
                                    .background(PinkBlueBrush),
                            )
                        }
                    },
                    colors = SliderDefaults.colors(
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        // 全屏极简控制层：右上退出按钮 + 底部细进度条（可拖）；轻点画面切换，3 秒自动淡出
        AnimatedVisibility(
            visible = isCurrent && isFullscreen && fsControlsVisible && errMsg == null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(Modifier.fillMaxSize()) {
                IconButton(
                    onClick = onExitFullscreen,
                    modifier = Modifier.align(Alignment.TopEnd)
                        .padding(top = 56.dp, end = 16.dp)
                        .size(44.dp)
                        .glassDark(999.dp, alpha = 0.42f),
                ) {
                    Icon(
                        painterResource(R.drawable.ic_fullscreen_exit),
                        "退出全屏",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp),
                    )
                }
                val fsPos = if (seeking) seekPos else position
                val fsDurF = duration.toFloat().coerceAtLeast(1f)
                Slider(
                    value = fsPos.toFloat().coerceIn(0f, fsDurF),
                    onValueChange = { seeking = true; seekPos = it.toLong() },
                    onValueChangeFinished = {
                        player?.seekTo(seekPos)
                        position = seekPos
                        seeking = false
                    },
                    valueRange = 0f..fsDurF,
                    enabled = duration > 0,
                    thumb = {
                        Box(Modifier.size(12.dp).background(Color.White, CircleShape))
                    },
                    track = { sliderState ->
                        Box(
                            Modifier.fillMaxWidth().height(3.dp)
                                .clip(RoundedCornerShape(2.dp)),
                        ) {
                            Box(Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.25f)))
                            val frac = (sliderState.value / fsDurF).coerceIn(0f, 1f)
                            Box(
                                Modifier.fillMaxHeight().fillMaxWidth(frac)
                                    .background(PinkBlueBrush),
                            )
                        }
                    },
                    colors = SliderDefaults.colors(
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent,
                    ),
                    modifier = Modifier.align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 48.dp),
                )
            }
        }

        // 放大时右下角 "1x" 一键复位（仅放大时显示）
        AnimatedVisibility(
            visible = isCurrent && zoom.zoomed && errMsg == null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomEnd),
        ) {
            Box(
                modifier = Modifier.padding(end = 16.dp, bottom = 110.dp)
                    .size(46.dp)
                    .glassDark(999.dp, alpha = 0.42f)
                    .pressScale(onClick = { zoom.reset() }),
                contentAlignment = Alignment.Center,
            ) {
                Text("1x", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/** 用系统 DownloadManager 下载到 Download 文件夹，通知栏显示进度 */
private fun downloadVideo(ctx: Context, video: VideoItem) {
    val dm = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val req = DownloadManager.Request(Uri.parse(video.url))
        .setTitle(video.name)
        .setDescription("LX视频 · 正在下载")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, video.name)
        .setAllowedOverMetered(true)
    dm.enqueue(req)
    Toast.makeText(ctx, "已加入下载", Toast.LENGTH_SHORT).show()
}

/** 系统分享：视频名 + 签名直链 */
private fun shareVideo(ctx: Context, video: VideoItem) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, "${video.name}\n${video.url}")
    }
    ctx.startActivity(Intent.createChooser(send, "分享视频"))
}

private fun fmtFeedMs(ms: Long): String {
    if (ms < 0) return "0:00"
    val s = ms / 1000
    return "%d:%02d".format(s / 60, s % 60)
}
