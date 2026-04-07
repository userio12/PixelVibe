package com.pixelvibe.vedioplayer.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pixelvibe.vedioplayer.database.entities.RecentlyPlayedEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentlyPlayedDao {
    @Query("SELECT * FROM recently_played ORDER BY timestamp DESC")
    fun getAll(): Flow<List<RecentlyPlayedEntity>>

    @Query("SELECT * FROM recently_played ORDER BY timestamp DESC LIMIT :limit")
    fun getRecent(limit: Int = 50): Flow<List<RecentlyPlayedEntity>>

    @Query("SELECT * FROM recently_played WHERE filePath = :path LIMIT 1")
    suspend fun getByPath(path: String): RecentlyPlayedEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: RecentlyPlayedEntity)

    @Query("DELETE FROM recently_played")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM recently_played")
    fun getCount(): Flow<Int>
}
