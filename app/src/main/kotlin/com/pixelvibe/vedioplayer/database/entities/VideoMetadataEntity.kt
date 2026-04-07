package com.pixelvibe.vedioplayer.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "video_metadata")
data class VideoMetadataEntity(
    @PrimaryKey(autoGenerate = false)
    val path: String,
    val size: Long = 0L,
    val dateModified: Long = 0L,
    val duration: Long = 0L,
    val width: Int = 0,
    val height: Int = 0,
    val fps: Float = 0f,
    val lastScanned: Long = 0L,
    val subtitleCodec: String = "",
    val hasEmbeddedSubtitles: Boolean = false
)
