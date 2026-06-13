package com.pixelvibe.vedioplayer.core.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "playlist_videos",
    primaryKeys = ["playlistId", "videoId"],
    foreignKeys = [
        ForeignKey(entity = PlaylistEntity::class, parentColumns = ["id"], childColumns = ["playlistId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = VideoEntity::class, parentColumns = ["id"], childColumns = ["videoId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("videoId")]
)
data class PlaylistVideoEntity(
    val playlistId: String,
    val videoId: String,
    val position: Int
)
