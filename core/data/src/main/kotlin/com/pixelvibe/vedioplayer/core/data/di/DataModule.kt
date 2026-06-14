package com.pixelvibe.vedioplayer.core.data.di

import android.content.Context
import androidx.room.Room
import com.pixelvibe.vedioplayer.core.data.db.AppDatabase
import com.pixelvibe.vedioplayer.core.data.network.ChromecastClient
import com.pixelvibe.vedioplayer.core.data.network.FtpClient
import com.pixelvibe.vedioplayer.core.data.network.SmbClient
import com.pixelvibe.vedioplayer.core.data.network.SsdpDiscovery
import com.pixelvibe.vedioplayer.core.data.network.WebDavClient
import com.pixelvibe.vedioplayer.core.data.repository.VideoRepository
import com.pixelvibe.vedioplayer.core.data.scanner.MediaScanner
import com.pixelvibe.vedioplayer.core.data.security.AppLockManager
import com.pixelvibe.vedioplayer.core.data.security.IncognitoManager
import com.pixelvibe.vedioplayer.core.data.security.SettingsBackupManager
import com.pixelvibe.vedioplayer.core.data.security.ThemePreferences
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "pixelvibe.db"
        ).fallbackToDestructiveMigrationOnDowngrade().build()
    }

    single { get<AppDatabase>().videoDao() }
    single { get<AppDatabase>().playlistDao() }
    single { get<AppDatabase>().historyDao() }

    single { VideoRepository(get()) }
    single { MediaScanner(androidContext(), get()) }

    single { SmbClient() }
    single { FtpClient() }
    single { WebDavClient() }
    single { SsdpDiscovery() }
    single { ChromecastClient() }

    single { AppLockManager(androidContext()) }
    single { IncognitoManager(androidContext()) }
    single { SettingsBackupManager(androidContext()) }
    single { ThemePreferences(androidContext()) }
}
