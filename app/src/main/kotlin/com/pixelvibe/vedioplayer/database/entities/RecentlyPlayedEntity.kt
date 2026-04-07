package com.pixelvibe.vedioplayer.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recently_played")
data class RecentlyPlayedEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val filePath: String = "",
    val fileName: String = "",
    val videoTitle: String = "",
    val duration: Long = 0L,
    val fileSize: Long = 0L,
    val width: Int = 0,
    val height: Int = 0,
    val timestamp: Long = 0L,
    val launchSource: String = "",
    val playlistId: Long? = null
)
