package com.pixelvibe.vedioplayer.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pixelvibe.vedioplayer.database.entities.NetworkConnectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NetworkConnectionDao {
    @Query("SELECT * FROM network_connections ORDER BY lastConnected DESC")
    fun getAll(): Flow<List<NetworkConnectionEntity>>

    @Query("SELECT * FROM network_connections WHERE autoConnect = 1")
    fun getAutoConnect(): Flow<List<NetworkConnectionEntity>>

    @Query("SELECT * FROM network_connections WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): NetworkConnectionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(connection: NetworkConnectionEntity): Long

    @Update
    suspend fun update(connection: NetworkConnectionEntity)

    @Delete
    suspend fun delete(connection: NetworkConnectionEntity)

    @Query("UPDATE network_connections SET lastConnected = :timestamp WHERE id = :id")
    suspend fun updateLastConnected(id: Long, timestamp: Long)
}
