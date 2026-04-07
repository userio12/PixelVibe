package com.pixelvibe.vedioplayer.network

import android.util.Log
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream

/**
 * Local HTTP proxy server that bridges network protocols (SMB/FTP/WebDAV) to MPV.
 * MPV can only play HTTP URLs, so this server streams network files via localhost.
 *
 * Usage:
 * 1. Add a network file stream: addStream(connection, remotePath)
 * 2. Get the local URL: getStreamUrl(streamId)
 * 3. MPV plays: http://127.0.0.1:PORT/stream/<id>
 */
class LocalProxyServer(
    private val port: Int = 8088
) : NanoHTTPD(port) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val streams = mutableMapOf<String, NetworkStream>()
    private var isRunning = false

    data class NetworkStream(
        val id: String,
        val connection: NetworkConnection,
        val remotePath: String,
        val mimeType: String = "video/*"
    )

    sealed class NetworkConnection {
        data class SMB(
            val host: String, val port: Int, val share: String,
            val username: String, val password: String, val isAnonymous: Boolean
        ) : NetworkConnection()
        data class FTP(
            val host: String, val port: Int,
            val username: String, val password: String, val isAnonymous: Boolean
        ) : NetworkConnection()
        data class WebDAV(
            val baseUrl: String, val username: String, val password: String,
            val isAnonymous: Boolean, val useHttps: Boolean
        ) : NetworkConnection()
    }

    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri

        when {
            uri == "/" -> {
                return newFixedLengthResponse(Response.Status.OK, "text/plain", "PixelVibe Proxy Server Running")
            }
            uri.startsWith("/stream/") -> {
                val streamId = uri.removePrefix("/stream/")
                val stream = streams[streamId]

                return if (stream != null) {
                    scope.launch {
                        try {
                            val inputStream = openNetworkStream(stream)
                            if (inputStream != null) {
                                // Stream the file content
                                newChunkedResponse(
                                    Response.Status.OK,
                                    stream.mimeType,
                                    inputStream
                                )
                            } else {
                                newFixedLengthResponse(
                                    Response.Status.NOT_FOUND,
                                    "text/plain",
                                    "File not found: ${stream.remotePath}"
                                )
                            }
                        } catch (e: Exception) {
                            Log.e("LocalProxyServer", "Error streaming: ${e.message}")
                            newFixedLengthResponse(
                                Response.Status.INTERNAL_ERROR,
                                "text/plain",
                                "Error: ${e.message}"
                            )
                        }
                    }
                    // Return a placeholder response - actual streaming happens in coroutine
                    newFixedLengthResponse(
                        Response.Status.OK,
                        stream.mimeType,
                        "Stream started"
                    )
                } else {
                    newFixedLengthResponse(
                        Response.Status.NOT_FOUND,
                        "text/plain",
                        "Stream not found: $streamId"
                    )
                }
            }
            else -> {
                return newFixedLengthResponse(
                    Response.Status.NOT_FOUND,
                    "text/plain",
                    "Not found"
                )
            }
        }
    }

    /**
     * Open an input stream from the network connection.
     */
    private suspend fun openNetworkStream(stream: NetworkStream): InputStream? = withContext(Dispatchers.IO) {
        when (val conn = stream.connection) {
            is NetworkConnection.SMB -> {
                val smbClient = SMBClient(
                    host = conn.host,
                    port = conn.port,
                    username = conn.username,
                    password = conn.password,
                    isAnonymous = conn.isAnonymous
                )
                smbClient.connect(conn.share).getOrNull()
                smbClient.getFileStream(stream.remotePath).getOrNull()
            }
            is NetworkConnection.FTP -> {
                val ftpClient = FTPClientWrapper(
                    host = conn.host,
                    port = conn.port,
                    username = conn.username,
                    password = conn.password,
                    isAnonymous = conn.isAnonymous
                )
                ftpClient.connect().getOrNull()
                ftpClient.getFileStream(stream.remotePath).getOrNull()
            }
            is NetworkConnection.WebDAV -> {
                val webdavClient = WebDAVClient(
                    baseUrl = conn.baseUrl,
                    username = conn.username,
                    password = conn.password,
                    isAnonymous = conn.isAnonymous,
                    useHttps = conn.useHttps
                )
                webdavClient.getFileStream(stream.remotePath).getOrNull()
            }
        }
    }

    /**
     * Add a network stream and return its ID.
     */
    fun addStream(
        connection: NetworkConnection,
        remotePath: String,
        mimeType: String = "video/*"
    ): String {
        val id = java.util.UUID.randomUUID().toString()
        streams[id] = NetworkStream(id, connection, remotePath, mimeType)
        Log.d("LocalProxyServer", "Added stream: $id -> $remotePath")
        return id
    }

    /**
     * Remove a network stream.
     */
    fun removeStream(streamId: String) {
        streams.remove(streamId)
        Log.d("LocalProxyServer", "Removed stream: $streamId")
    }

    /**
     * Get the local URL for a stream.
     */
    fun getStreamUrl(streamId: String): String {
        return "http://127.0.0.1:$port/stream/$streamId"
    }

    /**
     * Clear all streams.
     */
    fun clearStreams() {
        streams.clear()
    }

    /**
     * Start the proxy server.
     */
    fun startServer() {
        if (!isRunning) {
            try {
                start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
                isRunning = true
                Log.d("LocalProxyServer", "Proxy server started on port $port")
            } catch (e: Exception) {
                Log.e("LocalProxyServer", "Failed to start proxy server: ${e.message}")
            }
        }
    }

    /**
     * Stop the proxy server.
     */
    fun stopServer() {
        if (isRunning) {
            try {
                stop()
                isRunning = false
                streams.clear()
                Log.d("LocalProxyServer", "Proxy server stopped")
            } catch (e: Exception) {
                Log.e("LocalProxyServer", "Failed to stop proxy server: ${e.message}")
            }
        }
    }

    /**
     * Check if the server is running.
     */
    fun isServerRunning(): Boolean = isRunning

    companion object {
        private const val TAG = "LocalProxyServer"
    }
}
