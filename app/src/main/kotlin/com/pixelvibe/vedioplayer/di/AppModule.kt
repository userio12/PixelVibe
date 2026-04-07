package com.pixelvibe.vedioplayer.di

import com.pixelvibe.vedioplayer.database.PixelVibeDatabase
import com.pixelvibe.vedioplayer.database.repositories.PlaybackHistoryRepository
import com.pixelvibe.vedioplayer.database.repositories.PlaylistRepository
import com.pixelvibe.vedioplayer.database.repositories.RecentlyPlayedRepository
import com.pixelvibe.vedioplayer.domain.SubtitleManager
import com.pixelvibe.vedioplayer.preferences.PlayerPreferences
import com.pixelvibe.vedioplayer.preferences.PreferenceStore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    // Database
    single { PixelVibeDatabase.getInstance(androidContext()) }
    single { get<PixelVibeDatabase>().playbackStateDao() }
    single { get<PixelVibeDatabase>().recentlyPlayedDao() }
    single { get<PixelVibeDatabase>().videoMetadataDao() }
    single { get<PixelVibeDatabase>().networkConnectionDao() }
    single { get<PixelVibeDatabase>().playlistDao() }

    // Repositories
    single { PlaybackHistoryRepository(get()) }
    single { RecentlyPlayedRepository(get()) }
    single { PlaylistRepository(get()) }

    // Domain
    single { SubtitleManager(androidContext(), get()) }

    // Preferences
    single { PreferenceStore(androidContext()) }
    single { PlayerPreferences(get()) }
}
