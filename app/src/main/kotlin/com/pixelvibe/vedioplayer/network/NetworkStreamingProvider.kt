package com.pixelvibe.vedioplayer.network

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileNotFoundException

/**
 * ContentProvider that enables URI-based streaming of network files.
 * Allows MPV and other apps to access network files via content:// URIs.
 *
 * Usage:
 * content://com.pixelvibe.vedioplayer.network/stream/<encoded_remote_path>
 *
 * The provider uses the LocalProxyServer to fetch network files and stream them.
 */
class NetworkStreamingProvider : ContentProvider() {

    private val proxyServer by lazy { LocalProxyServer() }

    override fun onCreate(): Boolean {
        proxyServer.startServer()
        Log.d(TAG, "NetworkStreamingProvider created, proxy on port ${proxyServer.port}")
        return true
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        val path = uri.pathSegments.joinToString("/")
        Log.d(TAG, "Opening network file: $path")

        return try {
            // Parse the network connection info from the URI
            val protocol = uri.getQueryParameter("protocol") ?: throw FileNotFoundException("No protocol")
            val host = uri.getQueryParameter("host") ?: throw FileNotFoundException("No host")
            val remotePath = uri.getQueryParameter("path") ?: throw FileNotFoundException("No path")
            val username = uri.getQueryParameter("username") ?: ""
            val password = uri.getQueryParameter("password") ?: ""
            val isAnonymous = uri.getBooleanQueryParameter("anonymous", false)

            // Add stream to proxy and get local URL
            val connection = when (protocol.uppercase()) {
                "SMB" -> LocalProxyServer.NetworkConnection.SMB(
                    host = host,
                    port = uri.getQueryParameter("port")?.toIntOrNull() ?: 445,
                    share = remotePath.substringBefore("/", ""),
                    username = username,
                    password = password,
                    isAnonymous = isAnonymous
                )
                "FTP" -> LocalProxyServer.NetworkConnection.FTP(
                    host = host,
                    port = uri.getQueryParameter("port")?.toIntOrNull() ?: 21,
                    username = username,
                    password = password,
                    isAnonymous = isAnonymous
                )
                "WEBDAV" -> LocalProxyServer.NetworkConnection.WebDAV(
                    baseUrl = "http${if (uri.getBooleanQueryParameter("https", false)) "s" else ""}://$host:${uri.getQueryParameter("port")?.toIntOrNull() ?: 80}",
                    username = username,
                    password = password,
                    isAnonymous = isAnonymous,
                    useHttps = uri.getBooleanQueryParameter("https", false)
                )
                else -> throw FileNotFoundException("Unsupported protocol: $protocol")
            }

            val streamId = proxyServer.addStream(connection, remotePath)
            val localUrl = proxyServer.getStreamUrl(streamId)

            // For now, return a pipe descriptor - actual streaming via HTTP
            // In production, this would open the file via the network client directly
            val pipe = ParcelFileDescriptor.createPipe()

            // Start streaming in background
            Thread {
                try {
                    val inputStream = when (connection) {
                        is LocalProxyServer.NetworkConnection.SMB -> {
                            runBlocking {
                                val client = SMBClient(
                                    host = connection.host,
                                    port = connection.port,
                                    username = connection.username,
                                    password = connection.password,
                                    isAnonymous = connection.isAnonymous
                                )
                                client.connect(connection.share).getOrThrow()
                                client.getFileStream(remotePath).getOrNull()
                            }
                        }
                        is LocalProxyServer.NetworkConnection.FTP -> {
                            runBlocking {
                                val client = FTPClientWrapper(
                                    host = connection.host,
                                    port = connection.port,
                                    username = connection.username,
                                    password = connection.password,
                                    isAnonymous = connection.isAnonymous
                                )
                                client.connect().getOrThrow()
                                client.getFileStream(remotePath).getOrNull()
                            }
                        }
                        is LocalProxyServer.NetworkConnection.WebDAV -> {
                            runBlocking {
                                val client = WebDAVClient(
                                    baseUrl = connection.baseUrl,
                                    username = connection.username,
                                    password = connection.password,
                                    isAnonymous = connection.isAnonymous,
                                    useHttps = connection.useHttps
                                )
                                client.getFileStream(remotePath).getOrNull()
                            }
                        }
                    }

                    inputStream?.use { input ->
                        ParcelFileDescriptor.AutoCloseOutputStream(pipe[1]).use { output ->
                            input.copyTo(output)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error streaming: ${e.message}")
                } finally {
                    try { pipe[1].close() } catch (_: Exception) {}
                }
            }.start()

            pipe[0]
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open network file: ${e.message}")
            throw FileNotFoundException(e.message)
        }
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun getType(uri: Uri): String? = "video/*"

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0

    companion object {
        private const val TAG = "NetworkStreamingProvider"
    }
}
