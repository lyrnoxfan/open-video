package com.example.lxvideo.data

import org.json.JSONObject

data class VideoItem(
    val id: String,
    val name: String,
    val path: String,
    val size: Long,
    val mime: String?,
    val createdAt: Long,
    val url: String,
)

fun JSONObject.toVideoItem() = VideoItem(
    id = optString("id"),
    name = optString("name"),
    path = optString("path", "/"),
    size = optLong("size"),
    mime = if (isNull("mime")) null else optString("mime"),
    createdAt = optLong("created_at"),
    url = optString("url"),
)
