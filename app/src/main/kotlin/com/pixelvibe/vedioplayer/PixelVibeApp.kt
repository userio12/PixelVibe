package com.pixelvibe.vedioplayer

import android.app.Application
import com.pixelvibe.vedioplayer.di.appModule
import com.pixelvibe.vedioplayer.di.databaseModule
import com.pixelvibe.vedioplayer.di.domainModule
import com.pixelvibe.vedioplayer.di.fileManagerModule
import com.pixelvibe.vedioplayer.di.networkModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class PixelVibeApp : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@PixelVibeApp)
            modules(
                appModule,
                databaseModule,
                fileManagerModule,
                domainModule,
                networkModule
            )
        }
    }
}
