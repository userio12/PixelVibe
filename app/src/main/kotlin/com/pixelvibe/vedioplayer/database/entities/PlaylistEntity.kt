package com.pixelvibe.vedioplayer.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "playlists",
    indices = [Index(value = ["name"])]
)
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val m3uSourceUrl: String = "",
    val isM3uPlaylist: Boolean = false
)
