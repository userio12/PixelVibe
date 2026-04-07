package com.pixelvibe.vedioplayer.domain

import com.pixelvibe.vedioplayer.database.entities.PlaylistEntity
import com.pixelvibe.vedioplayer.database.entities.PlaylistItemEntity
import com.pixelvibe.vedioplayer.database.repositories.PlaylistRepository
import kotlinx.coroutines.flow.Flow

/**
 * Manages playlist creation, editing, and M3U import.
 */
class PlaylistManager(
    private val repository: PlaylistRepository
) {
    fun getAll(): Flow<List<PlaylistEntity>> = repository.getAll()

    fun getM3uPlaylists(): Flow<List<PlaylistEntity>> = repository.getM3uPlaylists()

    suspend fun getById(id: Long): PlaylistEntity? = repository.getById(id)

    suspend fun create(name: String): Long {
        val playlist = PlaylistEntity(
            name = name,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return repository.create(playlist)
    }

    suspend fun update(playlist: PlaylistEntity) = repository.update(playlist)

    suspend fun delete(playlist: PlaylistEntity) = repository.delete(playlist)

    fun getItems(playlistId: Long): Flow<List<PlaylistItemEntity>> = repository.getItems(playlistId)

    suspend fun addItem(playlistId: Long, filePath: String, fileName: String, position: Int): Long {
        val item = PlaylistItemEntity(
            playlistId = playlistId,
            filePath = filePath,
            fileName = fileName,
            position = position,
            addedAt = System.currentTimeMillis()
        )
        return repository.addItem(item)
    }

    suspend fun updateItemPosition(itemId: Long, newPosition: Int) = repository.updateItemPosition(itemId, newPosition)

    suspend fun importM3u(url: String, items: List<String>): Long {
        val playlist = PlaylistEntity(
            name = url.substringAfterLast("/").substringBeforeLast("."),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            m3uSourceUrl = url,
            isM3uPlaylist = true
        )
        val playlistId = repository.create(playlist)
        val playlistItems = items.mapIndexed { index, path ->
            PlaylistItemEntity(
                playlistId = playlistId,
                filePath = path,
                fileName = path.substringAfterLast("/"),
                position = index,
                addedAt = System.currentTimeMillis()
            )
        }
        repository.addItems(playlistItems)
        return playlistId
    }
}
