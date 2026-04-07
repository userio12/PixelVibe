package com.pixelvibe.vedioplayer.database.repositories

import com.pixelvibe.vedioplayer.database.dao.PlaybackStateDao
import com.pixelvibe.vedioplayer.database.entities.PlaybackStateEntity
import kotlinx.coroutines.flow.Flow

class PlaybackHistoryRepository(
    private val dao: PlaybackStateDao
) {
    fun getAll(): Flow<List<PlaybackStateEntity>> = dao.getAll()

    fun getByTitle(mediaTitle: String): Flow<PlaybackStateEntity?> = dao.getByTitleFlow(mediaTitle)

    suspend fun getSync(mediaTitle: String): PlaybackStateEntity? = dao.getByTitle(mediaTitle)

    suspend fun save(state: PlaybackStateEntity) = dao.insert(state)

    suspend fun update(state: PlaybackStateEntity) = dao.update(state)

    suspend fun delete(state: PlaybackStateEntity) = dao.delete(state)

    suspend fun clearAll() = dao.deleteAll()

    fun getUnwatched(): Flow<List<PlaybackStateEntity>> = dao.getUnwatched()
}
