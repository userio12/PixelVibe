package com.pixelvibe.vedioplayer.core.common.util

import android.net.Uri

fun String.toUri(): Uri = Uri.parse(this)

fun Long.formatMillis(): String {
    val totalSeconds = this / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
