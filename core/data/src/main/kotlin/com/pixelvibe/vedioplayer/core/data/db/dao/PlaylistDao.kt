package com.pixelvibe.vedioplayer.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pixelvibe.vedioplayer.core.data.db.entity.PlaylistEntity
import com.pixelvibe.vedioplayer.core.data.db.entity.PlaylistVideoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylist(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addVideoToPlaylist(entry: PlaylistVideoEntity)

    @Query("DELETE FROM playlist_videos WHERE playlistId = :playlistId AND videoId = :videoId")
    suspend fun removeVideoFromPlaylist(playlistId: String, videoId: String)

    @Query("SELECT videoId FROM playlist_videos WHERE playlistId = :playlistId ORDER BY position ASC")
    fun getPlaylistVideoIds(playlistId: String): Flow<List<String>>
}
