package com.pixelvibe.vedioplayer.core.player.di

import com.pixelvibe.vedioplayer.core.player.audio.AudioEffectManager
import com.pixelvibe.vedioplayer.core.player.controller.PlayerController
import com.pixelvibe.vedioplayer.core.player.datasource.NetworkDataSourceFactory
import com.pixelvibe.vedioplayer.core.player.engine.PlaybackEngine
import com.pixelvibe.vedioplayer.core.player.pip.PipHandler
import com.pixelvibe.vedioplayer.core.player.subtitle.SubtitleManager
import com.pixelvibe.vedioplayer.core.player.subtitle.SubtitleSearchClient
import com.pixelvibe.vedioplayer.core.player.subtitle.SubtitleStylePreferences
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val playerModule = module {
    single { NetworkDataSourceFactory() }
    factory { PlaybackEngine(androidContext(), get()) }
    factory { PlayerController(get()) }
    single { SubtitleManager() }
    single { SubtitleSearchClient() }
    single { SubtitleStylePreferences(androidContext()) }
    single { AudioEffectManager(androidContext()) }
    single { PipHandler() }
}
