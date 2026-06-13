package com.pixelvibe.vedioplayer.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey val id: String,
    val title: String,
    val uri: String,
    val filePath: String,
    val mimeType: String?,
    val durationMs: Long,
    val fileSize: Long,
    val folderName: String?,
    val addedAt: Long,
    val modifiedAt: Long,
    val resumePositionMs: Long = 0,
    val isFavorite: Boolean = false,
    val lastPlayedAt: Long? = null,
    val playCount: Int = 0
)
