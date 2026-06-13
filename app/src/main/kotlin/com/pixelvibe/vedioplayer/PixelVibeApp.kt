package com.pixelvibe.vedioplayer

import android.app.Application
import com.pixelvibe.vedioplayer.core.data.di.dataModule
import com.pixelvibe.vedioplayer.core.player.di.playerModule
import com.pixelvibe.vedioplayer.core.ui.di.uiModule
import com.pixelvibe.vedioplayer.feature.home.di.homeFeatureModule
import com.pixelvibe.vedioplayer.feature.network.di.networkFeatureModule
import com.pixelvibe.vedioplayer.feature.player.di.playerFeatureModule
import com.pixelvibe.vedioplayer.feature.recent.di.recentFeatureModule
import com.pixelvibe.vedioplayer.feature.settings.di.settingsFeatureModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class PixelVibeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@PixelVibeApp)
            modules(
                dataModule,
                playerModule,
                uiModule,
                homeFeatureModule,
                recentFeatureModule,
                networkFeatureModule,
                playerFeatureModule,
                settingsFeatureModule
            )
        }
    }
}
