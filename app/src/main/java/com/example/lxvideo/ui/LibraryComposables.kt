package com.example.lxvideo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lxvideo.ui.theme.Blue
import com.example.lxvideo.ui.theme.PinkBlueBrush
import com.example.lxvideo.ui.theme.glassDark

/** 列表分区小标题：字重层级分明 */
@Composable
fun SectionTitle(text: String) {
    Text(
        text,
        color = Color.White,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
    )
}

/** 空状态：图标 + 主副标题，不再是干巴巴一行字 */
@Composable
fun EmptyHint(
    text: String,
    subtitle: String? = null,
    icon: ImageVector = Icons.Filled.PlayArrow,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(56.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.size(10.dp))
        Text(text, color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
        if (subtitle != null) {
            Spacer(Modifier.size(4.dp))
            Text(
                subtitle,
                color = Color.White.copy(alpha = 0.35f),
                fontSize = 12.sp,
            )
        }
    }
}

/** 视频条目：圆角卡片化 */
@Composable
fun VideoEntryRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle.isNotEmpty()) {
                Spacer(Modifier.size(3.dp))
                Text(subtitle, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(8.dp))
            trailing()
        }
    }
}

/** 毛玻璃文字按钮（重试 / 知道了 这类） */
@Composable
fun GlassTextButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .glassDark(999.dp, alpha = 0.5f)
            .pressScale(onClick = onClick)
            .padding(horizontal = 28.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

/** 渐变主按钮（强调操作用） */
@Composable
fun GradientButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(PinkBlueBrush)
            .pressScale(onClick = onClick)
            .padding(horizontal = 32.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

/** 加载占位：精致转圈 + 文案（light=true 用于浅色背景） */
@Composable
fun LoadingPlaceholder(text: String, light: Boolean = false) {
    val fg = if (light) Color(0xFF1E3A5F) else Color.White
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (light) {
            CircularProgressIndicator(
                modifier = Modifier.size(36.dp),
                color = Blue,
                trackColor = Blue.copy(alpha = 0.15f),
                strokeWidth = 4.dp,
                strokeCap = StrokeCap.Round,
            )
        } else {
            RefinedSpinner()
        }
        Spacer(Modifier.size(14.dp))
        Text(text, color = fg.copy(alpha = 0.6f), fontSize = 13.sp)
    }
}

/** 错误占位：图标 + 信息 + 毛玻璃重试（light=true 用于浅色背景） */
@Composable
fun ErrorPlaceholder(
    message: String,
    icon: ImageVector = Icons.Filled.PlayArrow,
    light: Boolean = false,
    onRetry: () -> Unit,
) {
    val fg = if (light) Color(0xFF1E3A5F) else Color.White
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFFFE2C55).copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = Color(0xFFFE2C55), modifier = Modifier.size(30.dp))
        }
        Spacer(Modifier.size(12.dp))
        Text(
            message,
            color = fg.copy(alpha = 0.75f),
            fontSize = 13.sp,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.size(16.dp))
        if (light) {
            GradientButton("重试", onClick = onRetry)
        } else {
            GlassTextButton("重试", onClick = onRetry)
        }
    }
}
