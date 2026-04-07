package com.pixelvibe.vedioplayer.domain

import com.pixelvibe.vedioplayer.database.entities.PlaybackStateEntity
import com.pixelvibe.vedioplayer.database.repositories.PlaybackHistoryRepository
import kotlinx.coroutines.flow.Flow

/**
 * Manages per-file playback state (position, speed, tracks, delays).
 */
class PlaybackState(
    private val repository: PlaybackHistoryRepository
) {
    fun getAll(): Flow<List<PlaybackStateEntity>> = repository.getAll()

    fun getByTitle(mediaTitle: String): Flow<PlaybackStateEntity?> = repository.getByTitle(mediaTitle)

    suspend fun save(state: PlaybackStateEntity) = repository.save(state)

    suspend fun delete(state: PlaybackStateEntity) = repository.delete(state)

    suspend fun clearAll() = repository.clearAll()

    fun getUnwatched(): Flow<List<PlaybackStateEntity>> = repository.getUnwatched()
}
