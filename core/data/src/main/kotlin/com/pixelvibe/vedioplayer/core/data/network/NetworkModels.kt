package com.pixelvibe.vedioplayer.core.data.network

import android.net.Uri

sealed interface NetworkSource {
    val name: String
    val host: String

    data class Smb(
        override val name: String,
        override val host: String,
        val share: String = "",
        val path: String = "",
        val username: String = "guest",
        val password: String = ""
    ) : NetworkSource

    data class Ftp(
        override val name: String,
        override val host: String,
        val port: Int = 21,
        val path: String = "",
        val username: String = "anonymous",
        val password: String = "anonymous@"
    ) : NetworkSource

    data class WebDav(
        override val name: String,
        override val host: String,
        val baseUrl: String,
        val path: String = "",
        val username: String = "",
        val password: String = ""
    ) : NetworkSource

    data class Dlna(
        override val name: String,
        override val host: String,
        val udn: String,
        val locationUrl: String
    ) : NetworkSource
}

data class NetworkFile(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long = 0,
    val lastModified: Long = 0,
    val source: NetworkSource
)

fun NetworkFile.toPlaybackUri(): String = when (val s = source) {
    is NetworkSource.Smb -> {
        val current = Uri.parse(path)
        val portPart = if (current.port > 0) ":${current.port}" else ""
        Uri.Builder()
            .scheme("smb")
            .encodedAuthority("${s.username}:${s.password}@${current.host}$portPart")
            .encodedPath(current.encodedPath)
            .build().toString()
    }
    is NetworkSource.Ftp -> {
        Uri.Builder()
            .scheme("ftp")
            .encodedAuthority("${s.username}:${s.password}@${s.host}:${s.port}")
            .path(path)
            .build().toString()
    }
    is NetworkSource.WebDav -> {
        val encodedUrl = Uri.encode(s.baseUrl)
        Uri.Builder()
            .scheme("webdav")
            .encodedAuthority("${s.username}:${s.password}@${s.host}")
            .path(path)
            .encodedQuery("baseUrl=$encodedUrl")
            .build().toString()
    }
    is NetworkSource.Dlna -> path
}

sealed interface NetworkResult {
    data class Success(val files: List<NetworkFile>, val breadcrumbs: List<Breadcrumb>) : NetworkResult
    data class Error(val message: String) : NetworkResult
    data object Loading : NetworkResult
}

data class Breadcrumb(val label: String, val path: String)
