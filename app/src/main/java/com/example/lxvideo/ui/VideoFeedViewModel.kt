package com.example.lxvideo.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lxvideo.data.Api
import com.example.lxvideo.data.UnauthorizedException
import com.example.lxvideo.data.VideoItem
import kotlinx.coroutines.launch

enum class FeedOrder { SHUFFLE, LATEST }

/** 短视频 feed 状态：视频列表 + 播放模式（随机/最新优先） */
class VideoFeedViewModel(val api: Api) : ViewModel() {
    var onUnauthorized: () -> Unit = {}

    var videos by mutableStateOf(emptyList<VideoItem>())
        private set
    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var order by mutableStateOf(FeedOrder.SHUFFLE)
        private set
    var currentIndex by mutableStateOf(0)
        private set

    fun refresh() {
        viewModelScope.launch {
            loading = true
            error = null
            try {
                applyOrder(api.getVideos())
            } catch (e: UnauthorizedException) {
                onUnauthorized()
            } catch (e: Exception) {
                error = "加载失败: ${e.message}"
            } finally {
                loading = false
            }
        }
    }

    fun setIndex(i: Int) {
        currentIndex = i.coerceIn(videos.indices)
    }

    /** 切换播放模式：重排列表并回到第一页 */
    fun toggleOrder() {
        val all = videos
        order = if (order == FeedOrder.SHUFFLE) FeedOrder.LATEST else FeedOrder.SHUFFLE
        applyOrder(all)
    }

    private fun applyOrder(list: List<VideoItem>) {
        videos = when (order) {
            FeedOrder.SHUFFLE -> list.shuffled()
            FeedOrder.LATEST -> list.sortedByDescending { it.createdAt }
        }
        currentIndex = 0
    }
}

class VideoFeedVMFactory(private val api: Api) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = VideoFeedViewModel(api) as T
}
