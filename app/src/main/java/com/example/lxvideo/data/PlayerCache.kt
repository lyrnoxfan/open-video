package com.example.lxvideo.data

import android.content.Context
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

/**
 * ExoPlayer 磁盘缓存单例（2GB，LRU）。
 * 看过的内容按 Range 分片存本地：重复看、拖进度直接读本地，不再走网络。
 * 进程生命周期内单例，不要 release。
 */
object PlayerCache {
    private const val MAX_BYTES = 2L * 1024 * 1024 * 1024 // 2GB

    @Volatile
    private var instance: SimpleCache? = null

    fun get(context: Context): SimpleCache = instance ?: synchronized(this) {
        instance ?: SimpleCache(
            File(context.cacheDir, "exo_cache"),
            LeastRecentlyUsedCacheEvictor(MAX_BYTES)
        ).also { instance = it }
    }
}
