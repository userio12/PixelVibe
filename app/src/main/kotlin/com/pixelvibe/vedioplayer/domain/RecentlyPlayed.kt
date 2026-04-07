package com.pixelvibe.vedioplayer.domain

import com.pixelvibe.vedioplayer.database.entities.RecentlyPlayedEntity
import com.pixelvibe.vedioplayer.database.repositories.RecentlyPlayedRepository
import kotlinx.coroutines.flow.Flow

/**
 * Manages recently played video tracking.
 */
class RecentlyPlayed(
    private val repository: RecentlyPlayedRepository
) {
    fun getAll(): Flow<List<RecentlyPlayedEntity>> = repository.getAll()

    fun getRecent(limit: Int = 50): Flow<List<RecentlyPlayedEntity>> = repository.getRecent(limit)

    suspend fun recordPlayback(entry: RecentlyPlayedEntity) {
        repository.add(entry)
    }

    suspend fun clearHistory() {
        repository.clearAll()
    }

    fun getCount(): Flow<Int> = repository.getCount()
}
