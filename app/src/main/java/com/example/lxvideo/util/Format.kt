package com.example.lxvideo.util

import java.util.Locale

fun fmtSize(n: Long): String {    if (n < 1024) return "$n B"
    val u = arrayOf("KB", "MB", "GB", "TB")
    var v = n.toDouble()
    var i = -1
    do {
        v /= 1024; i++
    } while (v >= 1024 && i < u.size - 1)
    val s = if (v >= 100) v.toInt().toString() else String.format(Locale.US, "%.1f", v)
    return "$s ${u[i]}"
}

/** 历史记录时间：MM-dd HH:mm */
fun fmtTime(ts: Long): String {
    val c = java.util.Calendar.getInstance().apply { timeInMillis = ts }
    return "%02d-%02d %02d:%02d".format(
        c.get(java.util.Calendar.MONTH) + 1,
        c.get(java.util.Calendar.DAY_OF_MONTH),
        c.get(java.util.Calendar.HOUR_OF_DAY),
        c.get(java.util.Calendar.MINUTE),
    )
}
