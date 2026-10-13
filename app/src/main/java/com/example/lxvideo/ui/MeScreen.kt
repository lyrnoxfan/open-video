package com.example.lxvideo.ui

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lxvideo.R
import com.example.lxvideo.data.VideoItem
import com.example.lxvideo.data.VideoStore
import com.example.lxvideo.ui.theme.Blue
import com.example.lxvideo.ui.theme.Pink
import com.example.lxvideo.ui.theme.PinkBlueBrush
import com.example.lxvideo.ui.theme.glassDark
import com.example.lxvideo.util.fmtSize

/**
 * 「我」页面：粉蓝渐变头图 + 毛玻璃统计卡 + 圆角卡片列表。
 * 注意：这是单用户本地播放器，没有消息后端，所以不做"消息"页。
 */
@Composable
fun MeScreen(
    videos: List<VideoItem>,
    onPickVideo: (String) -> Unit,
) {
    val ctx = LocalContext.current
    val store = remember(ctx) { VideoStore.get(ctx) }
    val favIds by remember { store.favorites() }.collectAsState(initial = emptyList())
    val hist by remember { store.history() }.collectAsState(initial = emptyList())

    LazyColumn(
        modifier = Modifier.fillMaxSize()
            .background(Color(0xFF06090F)),
    ) {
        // 头图：深色极光渐变 + 柔光
        item {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .height(220.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF1A2340),
                                Color(0xFF0E1528),
                                Color(0xFF06090F),
                            ),
                        ),
                    ),
            ) {
                // 极光色带：粉蓝柔光（Android 12+ 硬件模糊）
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Box(
                        Modifier.fillMaxWidth()
                            .height(120.dp)
                            .blur(50.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Blue.copy(alpha = 0.55f),
                                        Pink.copy(alpha = 0.45f),
                                        Blue.copy(alpha = 0.35f),
                                    ),
                                ),
                            ),
                    )
                    // 右上柔光点缀
                    Box(
                        Modifier.size(90.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 20.dp, y = (-20).dp)
                            .blur(40.dp)
                            .background(Pink.copy(alpha = 0.35f), CircleShape),
                    )
                } else {
                    // 低版本降级：静态渐变条
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .height(90.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Blue.copy(alpha = 0.35f),
                                        Pink.copy(alpha = 0.35f),
                                    ),
                                ),
                            ),
                    )
                }
                // 头像 + 昵称
                Row(
                    modifier = Modifier.align(Alignment.BottomStart)
                        .padding(horizontal = 20.dp)
                        .offset(y = 28.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(R.drawable.avatar),
                        contentDescription = "头像",
                        modifier = Modifier.size(88.dp)
                            .shadow(16.dp, CircleShape, ambientColor = Pink.copy(alpha = 0.4f))
                            .clip(CircleShape)
                            .border(2.5.dp, Color.White.copy(alpha = 0.9f), CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(
                        modifier = Modifier.padding(bottom = 30.dp),
                    ) {
                        Text(
                            "LX视频",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                        )
                    }
                }
            }
            Spacer(Modifier.height(44.dp))
        }

        // 统计卡：毛玻璃悬浮
        item {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .glassDark(20.dp, alpha = 0.5f)
                    .padding(vertical = 16.dp),
            ) {
                Stat("收藏", favIds.size, Modifier.weight(1f))
                Stat("观看", hist.size, Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
        }

        // 我的收藏
        item { SectionTitle("我的收藏") }
        val favs = favIds.mapNotNull { id -> videos.find { it.id == id } }
        if (favs.isEmpty()) {
            item {
                EmptyHint(
                    text = "还没有收藏",
                    subtitle = "刷视频时点右边的星星",
                    icon = Icons.Filled.Star,
                )
            }
        } else {
            items(favs.size) { i ->
                val v = favs[i]
                VideoEntryRow(
                    title = v.name,
                    subtitle = fmtSize(v.size),
                    onClick = { onPickVideo(v.id) },
                )
            }
        }

        // 底部悬浮导航预留
        item { Spacer(Modifier.height(110.dp)) }
    }
}

/** 统计：大字重数字 + 灰字标签，数字用粉蓝渐变提亮 */
@Composable
private fun Stat(label: String, value: Int, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value.toString(),
            style = TextStyle(
                brush = PinkBlueBrush,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFeatureSettings = "tnum",
            ),
        )
        Spacer(Modifier.size(2.dp))
        Text(label, color = Color.White.copy(alpha = 0.55f), fontSize = 12.sp)
    }
}

private fun fmtMs(ms: Long): String {
    if (ms < 0) return "0:00"
    val s = ms / 1000
    return "%d:%02d".format(s / 60, s % 60)
}
