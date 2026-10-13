package com.example.lxvideo.ui.theme

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Blue = Color(0xFF2563EB)
val BlueLight = Color(0xFF60A5FA)
val Ink = Color(0xFF1E3A5F)
val Sub = Color(0xFF64748B)
val GlassWhite = Color(1f, 1f, 1f, 0.62f)
val GlassBorder = Color(1f, 1f, 1f, 0.75f)
val StrokeBlue = Color(0xFF3B82F6).copy(alpha = 0.18f)

/** 粉蓝二次元 accent */
val Pink = Color(0xFFFF7AB8)
val PinkDeep = Color(0xFFF857A6)
val LikeRed = Color(0xFFFE2C55)
val FavGold = Color(0xFFFFC107)

/** 粉→蓝横向渐变（进度条、激活态高亮） */
val PinkBlueBrush = Brush.horizontalGradient(listOf(Pink, BlueLight))

/** 「我」页头图粉蓝渐变 */
val MeHeaderBrush = Brush.linearGradient(
    listOf(Color(0xFF8FB8FF), Color(0xFFB79CFF), Color(0xFFFF9ECF)),
)

/** 时间文字：等宽数字 + 字重分层，避免跳动 */
val TimeStyle = TextStyle(
    fontSize = 13.sp,
    fontWeight = FontWeight.SemiBold,
    color = Color.White.copy(alpha = 0.95f),
    fontFeatureSettings = "tnum",
)
val TimeTotalStyle = TimeStyle.copy(
    fontWeight = FontWeight.Normal,
    color = Color.White.copy(alpha = 0.55f),
)

/** 深色毛玻璃面板。
 * 注：Compose 内没有真正的 backdrop blur（Modifier.blur 只模糊自身），
 * 这里用半透明底 + 高光描边 + 顶部渐隐来模拟 iOS 液态玻璃质感。 */
fun Modifier.glassDark(corner: Dp = 20.dp, alpha: Float = 0.45f, borderWidth: Dp = 1.dp): Modifier =
    this
        .clip(RoundedCornerShape(corner))
        .background(Color.Black.copy(alpha = alpha))
        .border(borderWidth, Color.White.copy(alpha = 0.14f), RoundedCornerShape(corner))

/** Android 12+ 才有 RenderEffect 硬件模糊；低版本直接返回原 Modifier 兜底 */
fun Modifier.blurIfSupported(radius: Dp): Modifier =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) this.blur(radius) else this

private val Scheme = lightColorScheme(
    primary = Blue,
    onPrimary = Color.White,
    secondary = BlueLight,
    background = Color(0xFFF0F7FF),
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Color(0xFFE8F1FD),
    onSurfaceVariant = Sub,
    error = Color(0xFFDC2626),
)

@Composable
fun LxTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, content = content)
}

/** 蓝白渐变底（对齐网页版液态玻璃风） */
@Composable
fun GradientBackground(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFF0F7FF), Color(0xFFE8F1FD), Color(0xFFEEF4FF)),
                ),
            ),
        content = content,
    )
}

/** 液态玻璃卡片 */
@Composable
fun GlassCard(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(GlassWhite)
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp)),
        content = content,
    )
}
