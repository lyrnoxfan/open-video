package com.example.lxvideo

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lxvideo.data.Api
import com.example.lxvideo.ui.ErrorPlaceholder
import com.example.lxvideo.ui.LoadingPlaceholder
import com.example.lxvideo.ui.MeScreen
import com.example.lxvideo.ui.VideoFeedScreen
import com.example.lxvideo.ui.VideoFeedVMFactory
import com.example.lxvideo.ui.VideoFeedViewModel
import com.example.lxvideo.ui.pressScale
import com.example.lxvideo.ui.theme.LxTheme
import com.example.lxvideo.ui.theme.Sub
import com.example.lxvideo.ui.theme.glassDark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // 冷启动时 Manifest 里是 Splash 主题，进来后切回主主题
        setTheme(R.style.Theme_LxVideo_Main)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LxTheme {
                var api by remember { mutableStateOf<Api?>(null) }
                var err by remember { mutableStateOf<String?>(null) }
                var attempt by remember { mutableIntStateOf(0) }

                // 内置服务器地址和密码，开机直连（用户要求免登录）
                LaunchedEffect(attempt) {
                    err = null
                    try {
                        val a = withContext(Dispatchers.IO) {
                            Api(BuildConfig.PD_SERVER).also { if (BuildConfig.PD_PASSWORD.isNotEmpty()) it.login(BuildConfig.PD_PASSWORD) }
                        }
                        api = a
                    } catch (e: Exception) {
                        err = "连接失败: ${e.message}"
                    }
                }

                // 401（服务端改密码）→ 回到错误页，可重试
                fun onUnauthorized() {
                    api = null
                    err = "会话失效，请重试"
                }

                val current = api
                when {
                    current != null -> {
                        val vm: VideoFeedViewModel =
                            viewModel(factory = remember(current) { VideoFeedVMFactory(current) })
                        LaunchedEffect(current) { vm.onUnauthorized = ::onUnauthorized }

                        // 底部导航：只有「首页 / 我」。注意：这是单用户本地播放器，
                        // 没有消息后端，所以不做"消息"tab（UI 不许谎报）。
                        var tab by remember { mutableIntStateOf(0) }
                        var jumpId by remember { mutableStateOf<String?>(null) }
                        // 全屏 / 缩放状态（VideoFeedScreen 上报）
                        var isFullscreen by remember { mutableStateOf(false) }
                        var zoomed by remember { mutableStateOf(false) }
                        // 返回键：全屏时优先退出全屏，其次"我"页回首页，最后退 App
                        BackHandler {
                            when {
                                isFullscreen -> isFullscreen = false
                                tab == 1 -> tab = 0
                                else -> finish()
                            }
                        }
                        // 全屏时隐藏系统状态栏+导航栏（滑动短暂唤出），退出时恢复
                        val activity = LocalContext.current as Activity
                        LaunchedEffect(isFullscreen) {
                            val controller = WindowInsetsControllerCompat(
                                activity.window, activity.window.decorView,
                            )
                            if (isFullscreen) {
                                controller.hide(WindowInsetsCompat.Type.systemBars())
                                controller.systemBarsBehavior =
                                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                            } else {
                                controller.show(WindowInsetsCompat.Type.systemBars())
                            }
                        }

                        Box(
                            Modifier.fillMaxSize()
                                .background(Color.Black),
                        ) {
                            // 首页视频流：切到"我"时暂停但保留在组合中（进度/页码不丢）
                            VideoFeedScreen(
                                vm = vm,
                                paused = tab == 1,
                                jumpId = jumpId,
                                onJumpDone = { jumpId = null },
                                isFullscreen = isFullscreen,
                                onFullscreenChange = { isFullscreen = it },
                                onZoomedChange = { zoomed = it },
                            )
                            // "我"页盖在视频流之上
                            if (tab == 1) {
                                MeScreen(
                                    videos = vm.videos,
                                    onPickVideo = { id ->
                                        jumpId = id
                                        tab = 0
                                    },
                                )
                            }
                            // 悬浮毛玻璃底栏：全屏或放大时隐藏（纯画面）
                            AnimatedVisibility(
                                visible = !isFullscreen && !zoomed,
                                enter = fadeIn(),
                                exit = fadeOut(),
                                modifier = Modifier.align(Alignment.BottomCenter),
                            ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .navigationBarsPadding()
                                    .padding(horizontal = 88.dp, vertical = 14.dp)
                                    .glassDark(999.dp, alpha = 0.5f)
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                NavTab(
                                    icon = Icons.Filled.Home,
                                    label = "首页",
                                    selected = tab == 0,
                                    onClick = { tab = 0 },
                                )
                                NavTab(
                                    icon = Icons.Filled.Person,
                                    label = "我",
                                    selected = tab == 1,
                                    onClick = { tab = 1 },
                                )
                            }
                            }
                        }
                    }
                    err != null -> Box(
                        Modifier.fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFFE8F1FD), Color(0xFFF3EAFB), Color(0xFFFDEBF3)),
                                ),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(48.dp),
                        ) {
                            ErrorPlaceholder(
                                message = err ?: "连接失败",
                                light = true,
                                onRetry = { attempt++ },
                            )
                            Spacer(Modifier.height(10.dp))
                            Text("请检查网络或服务端状态", color = Sub, fontSize = 12.sp)
                        }
                    }
                    // 应用内启动屏：粉蓝渐变 + icon-1，去掉黑屏冷启动感
                    else -> Box(
                        Modifier.fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFFE8F1FD), Color(0xFFF3EAFB), Color(0xFFFDEBF3)),
                                ),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(
                                painter = painterResource(R.drawable.avatar),
                                contentDescription = "LX视频",
                                modifier = Modifier.size(108.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop,
                            )
                            Spacer(Modifier.height(18.dp))
                            Text(
                                "LX视频",
                                color = Color(0xFF1E3A5F),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(Modifier.height(20.dp))
                            LoadingPlaceholder("正在连接…", light = true)
                        }
                    }
                }
            }
        }
    }
}

/** 悬浮底栏 tab：选中白色高亮，未选中半透明 */
@Composable
private fun NavTab(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.pressScale(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 6.dp),
    ) {
        Icon(
            icon,
            label,
            tint = if (selected) Color.White else Color.White.copy(alpha = 0.4f),
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.height(2.dp))
        Text(
            label,
            color = if (selected) Color.White else Color.White.copy(alpha = 0.4f),
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}
