package com.pixelvibe.vedioplayer.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "playlist_items",
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["playlistId"])]
)
data class PlaylistItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val playlistId: Long,
    val filePath: String = "",
    val fileName: String = "",
    val position: Int = 0,
    val addedAt: Long = 0L,
    val lastPlayedAt: Long = 0L,
    val playCount: Int = 0,
    val lastPosition: Long = 0L
)
