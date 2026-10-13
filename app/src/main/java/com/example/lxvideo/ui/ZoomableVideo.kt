package com.example.lxvideo.ui

import android.view.LayoutInflater
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.lxvideo.R

/**
 * 双指缩放状态：1x~4x。放大后单指拖动平移（四边钳制，画面不飞出屏幕）。
 * 切页 / 退出全屏时调用 reset() 回到 1x。
 */
class ZoomState {
    var scale by mutableStateOf(1f)
        private set
    var offset by mutableStateOf(Offset.Zero)
        private set

    /** 放大中：>1x 时隐藏全部 UI、禁用翻页 */
    val zoomed: Boolean get() = scale > 1.01f

    fun reset() {
        scale = 1f
        offset = Offset.Zero
    }

    /** 双指：以手势中心为锚点缩放 + 跟手平移 */
    internal fun applyPinch(centroid: Offset, pan: Offset, zoomDelta: Float, box: IntSize) {
        val newScale = (scale * zoomDelta).coerceIn(1f, 4f)
        if (newScale <= 1f && scale <= 1f) {
            reset()
            return
        }
        val k = newScale / scale
        val newOffset = (offset - centroid) * k + centroid + pan
        scale = newScale
        offset = clamp(newOffset, newScale, box)
    }

    /** 单指拖动（仅放大时）：跟随手指，钳制在可视范围内 */
    internal fun applyPan(drag: Offset, box: IntSize) {
        if (!zoomed) return
        offset = clamp(offset + drag, scale, box)
    }

    private fun clamp(o: Offset, s: Float, box: IntSize): Offset {
        if (s <= 1f || box == IntSize.Zero) return Offset.Zero
        val maxX = box.width * (s - 1f) / 2f
        val maxY = box.height * (s - 1f) / 2f
        return Offset(o.x.coerceIn(-maxX, maxX), o.y.coerceIn(-maxY, maxY))
    }
}

/**
 * 可缩放的播放器。
 * - 必须用 TextureView：SurfaceView 的画面是独立图层，不跟随 View 变换。
 * - 手势：单指不拦截（交给翻页/点按/双击点赞），双指才消费事件；
 *   放大后单指拖动平移（带 12px 触摸位移阈值，避免误吞点按）。
 */
@Composable
fun ZoomableVideo(
    player: ExoPlayer?,
    zoom: ZoomState,
    modifier: Modifier = Modifier,
) {
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    Box(
        modifier = modifier
            .onSizeChanged { boxSize = it }
            .pointerInput(zoom) {
                awaitEachGesture {
                    awaitFirstDown()
                    var lastDist = 0f
                    var lastCentroid = Offset.Zero
                    var panSlop = Offset.Zero
                    do {
                        val event = awaitPointerEvent()
                        val pressed = event.changes.filter { it.pressed }
                        when {
                            pressed.size >= 2 -> {
                                // 双指：缩放 + 跟手，消费事件阻止翻页
                                val c = (pressed[0].position + pressed[1].position) / 2f
                                val d = (pressed[0].position - pressed[1].position).getDistance()
                                if (lastDist > 0f && d > 0f) {
                                    zoom.applyPinch(c, c - lastCentroid, d / lastDist, boxSize)
                                }
                                lastDist = d
                                lastCentroid = c
                                pressed.forEach { it.consume() }
                            }
                            pressed.size == 1 && zoom.zoomed -> {
                                // 放大后单指拖动：过位移阈值才消费，点按不受影响
                                val change = pressed[0]
                                val delta = change.position - change.previousPosition
                                panSlop += delta
                                if (panSlop.getDistance() > 12f) {
                                    zoom.applyPan(delta, boxSize)
                                    change.consume()
                                }
                            }
                            else -> {
                                lastDist = 0f
                                panSlop = Offset.Zero
                            }
                        }
                    } while (event.changes.any { it.pressed })
                }
            }
            .graphicsLayer {
                scaleX = zoom.scale
                scaleY = zoom.scale
                translationX = zoom.offset.x
                translationY = zoom.offset.y
            },
    ) {
        AndroidView(
            factory = { c ->
                (LayoutInflater.from(c).inflate(R.layout.player_view, null) as PlayerView).apply {
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
            },
            update = { it.player = player },
            modifier = Modifier.fillMaxSize(),
        )
    }
}
