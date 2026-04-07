package com.pixelvibe.vedioplayer.repository

import com.pixelvibe.vedioplayer.database.entities.NetworkConnectionEntity
import com.pixelvibe.vedioplayer.database.dao.NetworkConnectionDao
import kotlinx.coroutines.flow.Flow

/**
 * Repository for network connection operations.
 * Bridges NetworkConnectionDao with the rest of the app.
 */
class NetworkRepository(
    private val dao: NetworkConnectionDao
) {
    fun getAllConnections(): Flow<List<NetworkConnectionEntity>> = dao.getAll()

    fun getAutoConnectConnections(): Flow<List<NetworkConnectionEntity>> = dao.getAutoConnect()

    suspend fun getById(id: Long): NetworkConnectionEntity? = dao.getById(id)

    suspend fun saveConnection(entity: NetworkConnectionEntity): Long = dao.insert(entity)

    suspend fun updateConnection(entity: NetworkConnectionEntity) = dao.update(entity)

    suspend fun deleteConnection(entity: NetworkConnectionEntity) = dao.delete(entity)

    suspend fun updateLastConnected(id: Long) = dao.updateLastConnected(id, System.currentTimeMillis())
}
