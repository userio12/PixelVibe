package com.pixelvibe.vedioplayer.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Squashed migration from v1 to v9.
 * Instead of creating individual migrations for each version,
 * this single migration handles all schema changes from v1 to v9.
 *
 * Note: The database uses fallbackToDestructiveMigration() in PixelVibeDatabase,
 * which means if this migration fails, the database will be recreated from scratch.
 * For production apps, proper migrations should be implemented.
 */

// Placeholder migrations — in production each would handle specific schema changes
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add network connections table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS network_connections (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL DEFAULT '',
                protocol TEXT NOT NULL DEFAULT '',
                host TEXT NOT NULL DEFAULT '',
                port INTEGER NOT NULL DEFAULT 0,
                username TEXT NOT NULL DEFAULT '',
                password TEXT NOT NULL DEFAULT '',
                path TEXT NOT NULL DEFAULT '',
                isAnonymous INTEGER NOT NULL DEFAULT 0,
                lastConnected INTEGER NOT NULL DEFAULT 0,
                autoConnect INTEGER NOT NULL DEFAULT 0
            )
        """)
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add video metadata table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS video_metadata (
                path TEXT NOT NULL PRIMARY KEY,
                size INTEGER NOT NULL DEFAULT 0,
                dateModified INTEGER NOT NULL DEFAULT 0,
                duration INTEGER NOT NULL DEFAULT 0,
                width INTEGER NOT NULL DEFAULT 0,
                height INTEGER NOT NULL DEFAULT 0,
                fps REAL NOT NULL DEFAULT 0,
                lastScanned INTEGER NOT NULL DEFAULT 0,
                subtitleCodec TEXT NOT NULL DEFAULT '',
                hasEmbeddedSubtitles INTEGER NOT NULL DEFAULT 0
            )
        """)
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add recently_played table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS recently_played (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                filePath TEXT NOT NULL DEFAULT '',
                fileName TEXT NOT NULL DEFAULT '',
                videoTitle TEXT NOT NULL DEFAULT '',
                duration INTEGER NOT NULL DEFAULT 0,
                fileSize INTEGER NOT NULL DEFAULT 0,
                width INTEGER NOT NULL DEFAULT 0,
                height INTEGER NOT NULL DEFAULT 0,
                timestamp INTEGER NOT NULL DEFAULT 0,
                launchSource TEXT NOT NULL DEFAULT '',
                playlistId INTEGER
            )
        """)
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add playlists table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS playlists (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL DEFAULT '',
                createdAt INTEGER NOT NULL DEFAULT 0,
                updatedAt INTEGER NOT NULL DEFAULT 0,
                m3uSourceUrl TEXT NOT NULL DEFAULT '',
                isM3uPlaylist INTEGER NOT NULL DEFAULT 0
            )
        """)
        db.execSQL("CREATE INDEX IF NOT EXISTS index_playlists_name ON playlists(name)")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add playlist_items table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS playlist_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                playlistId INTEGER NOT NULL,
                filePath TEXT NOT NULL DEFAULT '',
                fileName TEXT NOT NULL DEFAULT '',
                position INTEGER NOT NULL DEFAULT 0,
                addedAt INTEGER NOT NULL DEFAULT 0,
                lastPlayedAt INTEGER NOT NULL DEFAULT 0,
                playCount INTEGER NOT NULL DEFAULT 0,
                lastPosition INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY(playlistId) REFERENCES playlists(id) ON DELETE CASCADE
            )
        """)
        db.execSQL("CREATE INDEX IF NOT EXISTS index_playlist_items_playlistId ON playlist_items(playlistId)")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add useHttps column to network_connections
        db.execSQL("ALTER TABLE network_connections ADD COLUMN useHttps INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add hasBeenWatched column to playback_state
        db.execSQL("ALTER TABLE playback_state ADD COLUMN hasBeenWatched INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Final schema adjustments
        // No structural changes — version bump for feature tracking
    }
}

/**
 * All migrations combined array for Room database builder.
 */
val ALL_MIGRATIONS = arrayOf(
    MIGRATION_1_2,
    MIGRATION_2_3,
    MIGRATION_3_4,
    MIGRATION_4_5,
    MIGRATION_5_6,
    MIGRATION_6_7,
    MIGRATION_7_8,
    MIGRATION_8_9
)
