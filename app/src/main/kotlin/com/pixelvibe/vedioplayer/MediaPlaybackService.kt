package com.pixelvibe.vedioplayer

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Background playback service stub.
 * No actual playback implementation yet — ready for video engine integration.
 */
class MediaPlaybackService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }
}
