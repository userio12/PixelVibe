package com.pixelvibe.vedioplayer.domain

import android.content.Context
import com.pixelvibe.vedioplayer.database.entities.NetworkConnectionEntity
import com.pixelvibe.vedioplayer.database.dao.NetworkConnectionDao
import com.pixelvibe.vedioplayer.network.LocalProxyServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Manages network connections and streaming via local proxy.
 */
class NetworkConnectionManager(
    private val context: Context,
    private val dao: NetworkConnectionDao,
    private val proxyServer: LocalProxyServer
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Get all saved network connections.
     */
    fun getAllConnections(): Flow<List<NetworkConnectionEntity>> = dao.getAll()

    /**
     * Get connections marked for auto-connect.
     */
    fun getAutoConnectConnections(): Flow<List<NetworkConnectionEntity>> = dao.getAutoConnect()

    /**
     * Save a new network connection.
     */
    suspend fun saveConnection(entity: NetworkConnectionEntity): Long = withContext(Dispatchers.IO) {
        dao.insert(entity)
    }

    /**
     * Update an existing connection.
     */
    suspend fun updateConnection(entity: NetworkConnectionEntity) = withContext(Dispatchers.IO) {
        dao.update(entity)
    }

    /**
     * Delete a connection.
     */
    suspend fun deleteConnection(entity: NetworkConnectionEntity) = withContext(Dispatchers.IO) {
        dao.delete(entity)
    }

    /**
     * Add a network file stream and return the local URL for video engine to play.
     */
    fun addNetworkStream(
        connection: NetworkConnectionEntity,
        remotePath: String
    ): String {
        val proxyConnection = when (connection.protocol.uppercase()) {
            "SMB" -> LocalProxyServer.NetworkConnection.SMB(
                host = connection.host,
                port = connection.port,
                share = connection.path.substringBefore("/", ""),
                username = connection.username,
                password = connection.password,
                isAnonymous = connection.isAnonymous
            )
            "FTP" -> LocalProxyServer.NetworkConnection.FTP(
                host = connection.host,
                port = connection.port,
                username = connection.username,
                password = connection.password,
                isAnonymous = connection.isAnonymous
            )
            "WEBDAV" -> LocalProxyServer.NetworkConnection.WebDAV(
                baseUrl = "http${if (connection.useHttps) "s" else ""}://${connection.host}:${connection.port}",
                username = connection.username,
                password = connection.password,
                isAnonymous = connection.isAnonymous,
                useHttps = connection.useHttps
            )
            else -> throw IllegalArgumentException("Unknown protocol: ${connection.protocol}")
        }

        val streamId = proxyServer.addStream(proxyConnection, remotePath)
        val localUrl = proxyServer.getStreamUrl(streamId)

        // Update last connected timestamp
        scope.launch {
            dao.updateLastConnected(connection.id, System.currentTimeMillis())
        }

        return localUrl
    }

    /**
     * Auto-connect to saved connections on app launch.
     */
    fun autoConnectConnections() {
        scope.launch {
            dao.getAutoConnect().collect { connections ->
                connections.forEach { connection ->
                    try {
                        // Test connection
                        val proxyConnection = when (connection.protocol.uppercase()) {
                            "WEBDAV" -> LocalProxyServer.NetworkConnection.WebDAV(
                                baseUrl = "http${if (connection.useHttps) "s" else ""}://${connection.host}:${connection.port}",
                                username = connection.username,
                                password = connection.password,
                                isAnonymous = connection.isAnonymous,
                                useHttps = connection.useHttps
                            )
                            else -> null
                        }

                        if (proxyConnection != null) {
                            // Ping to verify connection
                            when (proxyConnection) {
                                is LocalProxyServer.NetworkConnection.WebDAV -> {
                                    val client = com.pixelvibe.vedioplayer.network.WebDAVClient(
                                        baseUrl = proxyConnection.baseUrl,
                                        username = proxyConnection.username,
                                        password = proxyConnection.password,
                                        isAnonymous = proxyConnection.isAnonymous,
                                        useHttps = proxyConnection.useHttps
                                    )
                                    client.ping().onSuccess {
                                        dao.updateLastConnected(connection.id, System.currentTimeMillis())
                                    }
                                }
                                else -> {}
                            }
                        }
                    } catch (e: Exception) {
                        // Silently fail auto-connect
                    }
                }
            }
        }
    }

    /**
     * Start the proxy server.
     */
    fun startProxy() {
        proxyServer.startServer()
    }

    /**
     * Stop the proxy server and clear all streams.
     */
    fun stopProxy() {
        proxyServer.stopServer()
    }
}
