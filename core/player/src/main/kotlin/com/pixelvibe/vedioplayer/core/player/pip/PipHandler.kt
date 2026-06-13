package com.pixelvibe.vedioplayer.core.player.pip

import android.app.Activity
import android.app.PictureInPictureParams
import android.os.Build
import android.util.Rational

open class PipHandler {

    private var pipActive = false

    open fun isPipSupported(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }

    open fun isAutoPipEnabled(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    }

    open fun enterPipMode(activity: Activity, width: Int = 16, height: Int = 9) {
        if (!isPipSupported()) return

        val rational = try {
            Rational(width, height)
        } catch (_: Exception) {
            Rational(16, 9)
        }

        val params = PictureInPictureParams.Builder()
            .setAspectRatio(rational)
            .build()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                activity.enterPictureInPictureMode(params)
                pipActive = true
            }
        } catch (_: Exception) {
            pipActive = false
        }
    }

    open fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        pipActive = isInPictureInPictureMode
    }

    open fun isPipActive(): Boolean = pipActive

    open fun release() {}
}
