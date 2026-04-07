package com.pixelvibe.vedioplayer.network

import com.thegrizzlylabs.sardineandroid.DavResource
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import com.thegrizzlylabs.sardineandroid.impl.SardineException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.URI

/**
 * Represents a file/folder entry from WebDAV server.
 */
data class WebDAVFileEntry(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long
)

/**
 * WebDAV client for accessing remote files over HTTP/HTTPS.
 * Uses Sardine Android library with OkHttp backend.
 */
class WebDAVClient(
    private val baseUrl: String,
    private val username: String = "",
    private val password: String = "",
    private val isAnonymous: Boolean = false,
    private val useHttps: Boolean = false
) {
    private val sardine = OkHttpSardine()

    init {
        if (!isAnonymous && username.isNotBlank()) {
            sardine.setCredentials(username, password)
        }
    }

    private fun getFullUrl(path: String): String {
        val scheme = if (useHttps) "https" else "http"
        val uri = URI(baseUrl)
        val host = uri.host ?: baseUrl
        val port = uri.port
        val basePath = uri.path?.trimEnd('/') ?: ""

        val cleanPath = path.trimStart('/')
        val fullPath = if (basePath.isNotBlank()) "$basePath/$cleanPath" else cleanPath

        return if (port > 0) {
            "$scheme://$host:$port/$fullPath"
        } else {
            "$scheme://$host/$fullPath"
        }
    }

    /**
     * List files in a remote directory.
     */
    suspend fun listFiles(remotePath: String = ""): Result<List<WebDAVFileEntry>> = withContext(Dispatchers.IO) {
        try {
            val url = getFullUrl(remotePath.ifBlank { "/" })
            val resources = sardine.list(url)

            val entries = resources.drop(1) // Skip root entry
                .mapNotNull { resource ->
                    val name = resource.name
                    if (name == null || name.isBlank()) return@mapNotNull null

                    WebDAVFileEntry(
                        name = name,
                        path = "${remotePath.trimEnd('/')}/$name",
                        isDirectory = resource.isDirectory,
                        size = resource.contentLength,
                        lastModified = resource.modified?.time ?: 0L
                    )
                }

            Result.success(entries)
        } catch (e: SardineException) {
            Result.failure(Exception("WebDAV error: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get an input stream for a remote file.
     */
    suspend fun getFileStream(remotePath: String): Result<InputStream> = withContext(Dispatchers.IO) {
        try {
            val url = getFullUrl(remotePath)
            val inputStream = sardine.get(url)
            Result.success(inputStream)
        } catch (e: SardineException) {
            Result.failure(Exception("Failed to retrieve file: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get file metadata.
     */
    suspend fun getFileMetadata(remotePath: String): Result<DavResource?> = withContext(Dispatchers.IO) {
        try {
            val url = getFullUrl(remotePath)
            val resources = sardine.list(url)
            if (resources.isNotEmpty()) {
                Result.success(resources.first())
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Check if a path exists.
     */
    suspend fun exists(remotePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = getFullUrl(remotePath)
            sardine.exists(url)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check if the server is reachable.
     */
    suspend fun ping(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val url = getFullUrl("/")
            sardine.list(url)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
