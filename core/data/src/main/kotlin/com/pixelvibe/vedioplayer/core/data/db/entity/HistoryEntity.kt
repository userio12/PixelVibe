package com.pixelvibe.vedioplayer.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_history")
data class HistoryEntity(
    @PrimaryKey val id: String,
    val videoId: String,
    val videoTitle: String,
    val videoUri: String,
    val watchedAt: Long,
    val positionMs: Long,
    val durationMs: Long
)
