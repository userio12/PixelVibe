package com.pixelvibe.vedioplayer.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pixelvibe.vedioplayer.database.entities.PlaybackStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaybackStateDao {
    @Query("SELECT * FROM playback_state")
    fun getAll(): Flow<List<PlaybackStateEntity>>

    @Query("SELECT * FROM playback_state WHERE mediaTitle = :mediaTitle LIMIT 1")
    suspend fun getByTitle(mediaTitle: String): PlaybackStateEntity?

    @Query("SELECT * FROM playback_state WHERE mediaTitle = :mediaTitle LIMIT 1")
    fun getByTitleFlow(mediaTitle: String): Flow<PlaybackStateEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(state: PlaybackStateEntity)

    @Update
    suspend fun update(state: PlaybackStateEntity)

    @Delete
    suspend fun delete(state: PlaybackStateEntity)

    @Query("DELETE FROM playback_state")
    suspend fun deleteAll()

    @Query("SELECT * FROM playback_state WHERE hasBeenWatched = 0")
    fun getUnwatched(): Flow<List<PlaybackStateEntity>>
}
