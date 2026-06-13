package com.pixelvibe.vedioplayer.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pixelvibe.vedioplayer.core.data.db.entity.VideoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Query("SELECT * FROM videos ORDER BY modifiedAt DESC")
    fun getAllVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE folderName = :folder ORDER BY title ASC")
    fun getVideosByFolder(folder: String): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavoriteVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE id = :id")
    suspend fun getVideoById(id: String): VideoEntity?

    @Query("SELECT * FROM videos WHERE title LIKE '%' || :query || '%'")
    fun searchVideos(query: String): Flow<List<VideoEntity>>

    @Query("SELECT DISTINCT folderName FROM videos WHERE folderName IS NOT NULL ORDER BY folderName ASC")
    fun getAllFolders(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(videos: List<VideoEntity>)

    @Update
    suspend fun update(video: VideoEntity)

    @Query("UPDATE videos SET resumePositionMs = :position WHERE id = :id")
    suspend fun updateResumePosition(id: String, position: Long)

    @Query("UPDATE videos SET isFavorite = :favorite WHERE id = :id")
    suspend fun updateFavorite(id: String, favorite: Boolean)

    @Query("DELETE FROM videos WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM videos")
    suspend fun deleteAll()
}
