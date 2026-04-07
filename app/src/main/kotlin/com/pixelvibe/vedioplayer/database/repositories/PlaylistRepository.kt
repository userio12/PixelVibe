package com.pixelvibe.vedioplayer.database.repositories

import com.pixelvibe.vedioplayer.database.dao.PlaylistDao
import com.pixelvibe.vedioplayer.database.entities.PlaylistEntity
import com.pixelvibe.vedioplayer.database.entities.PlaylistItemEntity
import kotlinx.coroutines.flow.Flow

class PlaylistRepository(
    private val dao: PlaylistDao
) {
    fun getAll(): Flow<List<PlaylistEntity>> = dao.getAll()

    fun getM3uPlaylists(): Flow<List<PlaylistEntity>> = dao.getM3uPlaylists()

    suspend fun getById(id: Long): PlaylistEntity? = dao.getById(id)

    suspend fun create(playlist: PlaylistEntity): Long = dao.insert(playlist)

    suspend fun update(playlist: PlaylistEntity) = dao.update(playlist)

    suspend fun delete(playlist: PlaylistEntity) = dao.deletePlaylistWithItems(playlist)

    fun getItems(playlistId: Long): Flow<List<PlaylistItemEntity>> = dao.getItems(playlistId)

    suspend fun getItemsSync(playlistId: Long): List<PlaylistItemEntity> = dao.getItemsSync(playlistId)

    suspend fun addItem(item: PlaylistItemEntity): Long = dao.insertItem(item)

    suspend fun addItems(items: List<PlaylistItemEntity>) = dao.insertItems(items)

    suspend fun updateItem(item: PlaylistItemEntity) = dao.updateItem(item)

    suspend fun deleteItem(item: PlaylistItemEntity) = dao.deleteItem(item)

    suspend fun clearItems(playlistId: Long) = dao.deleteAllItems(playlistId)

    suspend fun updateItemPosition(itemId: Long, newPosition: Int) = dao.updateItemPosition(itemId, newPosition)
}
