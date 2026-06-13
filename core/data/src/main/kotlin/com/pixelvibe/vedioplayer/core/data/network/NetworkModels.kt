package com.pixelvibe.vedioplayer.core.data.network

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

sealed interface NetworkResult {
    data class Success(val files: List<NetworkFile>, val breadcrumbs: List<Breadcrumb>) : NetworkResult
    data class Error(val message: String) : NetworkResult
    data object Loading : NetworkResult
}

data class Breadcrumb(val label: String, val path: String)
