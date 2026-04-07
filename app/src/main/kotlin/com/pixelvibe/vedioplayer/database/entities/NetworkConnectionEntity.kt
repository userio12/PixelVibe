package com.pixelvibe.vedioplayer.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "network_connections")
data class NetworkConnectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String = "",
    val protocol: String = "",       // SMB, FTP, WebDAV
    val host: String = "",
    val port: Int = 0,
    val username: String = "",
    val password: String = "",
    val path: String = "",
    val isAnonymous: Boolean = false,
    val lastConnected: Long = 0L,
    val autoConnect: Boolean = false,
    val useHttps: Boolean = false
)
