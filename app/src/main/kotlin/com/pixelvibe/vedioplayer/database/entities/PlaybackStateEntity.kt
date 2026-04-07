package com.pixelvibe.vedioplayer.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playback_state")
data class PlaybackStateEntity(
    @PrimaryKey(autoGenerate = false)
    val mediaTitle: String,
    val lastPosition: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val videoZoom: Int = 0,
    val sid: Int = -1,
    val secondarySid: Int = -1,
    val subDelay: Float = 0f,
    val subSpeed: Float = 1.0f,
    val aid: Int = -1,
    val audioDelay: Float = 0f,
    val timeRemaining: Long = 0L,
    val externalSubtitles: String = "",
    val hasBeenWatched: Boolean = false
)
