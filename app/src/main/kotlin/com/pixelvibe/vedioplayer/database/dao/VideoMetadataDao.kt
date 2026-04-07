package com.pixelvibe.vedioplayer.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pixelvibe.vedioplayer.database.entities.VideoMetadataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoMetadataDao {
    @Query("SELECT * FROM video_metadata")
    fun getAll(): Flow<List<VideoMetadataEntity>>

    @Query("SELECT * FROM video_metadata WHERE path = :path LIMIT 1")
    suspend fun getByPath(path: String): VideoMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(metadata: VideoMetadataEntity)

    @Query("DELETE FROM video_metadata WHERE lastScanned < :beforeTimestamp")
    suspend fun deleteOlderThan(beforeTimestamp: Long)

    @Query("DELETE FROM video_metadata")
    suspend fun deleteAll()
}
