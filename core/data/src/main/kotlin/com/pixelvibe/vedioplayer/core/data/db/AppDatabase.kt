package com.pixelvibe.vedioplayer.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pixelvibe.vedioplayer.core.data.db.dao.HistoryDao
import com.pixelvibe.vedioplayer.core.data.db.dao.PlaylistDao
import com.pixelvibe.vedioplayer.core.data.db.dao.VideoDao
import com.pixelvibe.vedioplayer.core.data.db.entity.HistoryEntity
import com.pixelvibe.vedioplayer.core.data.db.entity.PlaylistEntity
import com.pixelvibe.vedioplayer.core.data.db.entity.PlaylistVideoEntity
import com.pixelvibe.vedioplayer.core.data.db.entity.VideoEntity

@Database(
    entities = [VideoEntity::class, PlaylistEntity::class, PlaylistVideoEntity::class, HistoryEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun videoDao(): VideoDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun historyDao(): HistoryDao
}
