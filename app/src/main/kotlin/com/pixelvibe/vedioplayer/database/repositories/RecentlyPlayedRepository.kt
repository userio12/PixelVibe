package com.pixelvibe.vedioplayer.database.repositories

import com.pixelvibe.vedioplayer.database.dao.RecentlyPlayedDao
import com.pixelvibe.vedioplayer.database.entities.RecentlyPlayedEntity
import kotlinx.coroutines.flow.Flow

class RecentlyPlayedRepository(
    private val dao: RecentlyPlayedDao
) {
    fun getAll(): Flow<List<RecentlyPlayedEntity>> = dao.getAll()

    fun getRecent(limit: Int = 50): Flow<List<RecentlyPlayedEntity>> = dao.getRecent(limit)

    suspend fun getByPath(path: String): RecentlyPlayedEntity? = dao.getByPath(path)

    suspend fun add(item: RecentlyPlayedEntity) = dao.insert(item)

    suspend fun clearAll() = dao.deleteAll()

    fun getCount(): Flow<Int> = dao.getCount()
}
