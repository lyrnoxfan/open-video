package com.example.lxvideo.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.videoDataStore by preferencesDataStore(name = "lx_video")

/** 一条观看历史：视频 id + 上次观看时间 + 上次播放位置(ms) */
data class HistEntry(val id: String, val ts: Long, val pos: Long)

/**
 * 本地存储单例：点赞 / 收藏 / 播放位置 / 观看历史。
 * 全部只存手机（DataStore Preferences），不动服务端。
 * key 形如 liked_<id> / fav_<id> / pos_<id> / ts_<id>。
 */
class VideoStore private constructor(private val appCtx: Context) {

    companion object {
        @Volatile
        private var inst: VideoStore? = null

        fun get(c: Context): VideoStore =
            inst ?: synchronized(this) {
                inst ?: VideoStore(c.applicationContext).also { inst = it }
            }
    }

    private val ds get() = appCtx.videoDataStore

    private fun likedKey(id: String) = booleanPreferencesKey("liked_$id")
    private fun favKey(id: String) = booleanPreferencesKey("fav_$id")
    private fun posKey(id: String) = longPreferencesKey("pos_$id")
    private fun tsKey(id: String) = longPreferencesKey("ts_$id")

    fun isLiked(id: String): Flow<Boolean> =
        ds.data.map { it[likedKey(id)] ?: false }

    fun isFav(id: String): Flow<Boolean> =
        ds.data.map { it[favKey(id)] ?: false }

    /** 上次播放位置(ms)，没有则 0 */
    fun position(id: String): Flow<Long> =
        ds.data.map { it[posKey(id)] ?: 0L }

    suspend fun setLiked(id: String, v: Boolean) {
        ds.edit { it[likedKey(id)] = v }
    }

    suspend fun setFav(id: String, v: Boolean) {
        ds.edit { it[favKey(id)] = v }
    }

    /** 记录一次观看：位置 + 时间。播完（pos 归零）也会记时间，方便历史排序。 */
    suspend fun saveWatch(id: String, posMs: Long) {
        ds.edit {
            it[posKey(id)] = posMs
            it[tsKey(id)] = System.currentTimeMillis()
        }
    }

    /** 点赞的视频 id 列表（用于"我"页统计获赞数） */
    fun likedIds(): Flow<List<String>> = ds.data.map { p ->
        p.asMap().entries
            .filter { (k, v) -> k.name.startsWith("liked_") && v == true }
            .map { (k, _) -> k.name.removePrefix("liked_") }
    }

    /** 收藏的视频 id 列表 */
    fun favorites(): Flow<List<String>> = ds.data.map { p ->
        p.asMap().entries
            .filter { (k, v) -> k.name.startsWith("fav_") && v == true }
            .map { (k, _) -> k.name.removePrefix("fav_") }
    }

    /** 观看历史（按最后观看时间倒序） */
    fun history(): Flow<List<HistEntry>> = ds.data.map { p ->
        p.asMap().entries
            .filter { (k, _) -> k.name.startsWith("ts_") }
            .mapNotNull { (k, _) ->
                val id = k.name.removePrefix("ts_")
                val ts = p[longPreferencesKey(k.name)] ?: 0L
                if (ts > 0) HistEntry(id, ts, p[posKey(id)] ?: 0L) else null
            }
            .sortedByDescending { it.ts }
    }
}
