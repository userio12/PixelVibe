package com.pixelvibe.vedioplayer.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pixelvibe.vedioplayer.database.dao.NetworkConnectionDao
import com.pixelvibe.vedioplayer.database.dao.PlaybackStateDao
import com.pixelvibe.vedioplayer.database.dao.PlaylistDao
import com.pixelvibe.vedioplayer.database.dao.RecentlyPlayedDao
import com.pixelvibe.vedioplayer.database.dao.VideoMetadataDao
import com.pixelvibe.vedioplayer.database.entities.NetworkConnectionEntity
import com.pixelvibe.vedioplayer.database.entities.PlaybackStateEntity
import com.pixelvibe.vedioplayer.database.entities.PlaylistEntity
import com.pixelvibe.vedioplayer.database.entities.PlaylistItemEntity
import com.pixelvibe.vedioplayer.database.entities.RecentlyPlayedEntity
import com.pixelvibe.vedioplayer.database.entities.VideoMetadataEntity

@Database(
    entities = [
        PlaybackStateEntity::class,
        RecentlyPlayedEntity::class,
        VideoMetadataEntity::class,
        NetworkConnectionEntity::class,
        PlaylistEntity::class,
        PlaylistItemEntity::class
    ],
    version = 9,
    exportSchema = false
)
abstract class PixelVibeDatabase : RoomDatabase() {
    abstract fun playbackStateDao(): PlaybackStateDao
    abstract fun recentlyPlayedDao(): RecentlyPlayedDao
    abstract fun videoMetadataDao(): VideoMetadataDao
    abstract fun networkConnectionDao(): NetworkConnectionDao
    abstract fun playlistDao(): PlaylistDao

    companion object {
        private const val DATABASE_NAME = "pixelvibe_database"

        @Volatile
        private var INSTANCE: PixelVibeDatabase? = null

        fun getInstance(context: Context): PixelVibeDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PixelVibeDatabase::class.java,
                    DATABASE_NAME
                )
                .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
                .fallbackToDestructiveMigration()
                .build()
                .also { INSTANCE = it }
            }
        }
    }
}
