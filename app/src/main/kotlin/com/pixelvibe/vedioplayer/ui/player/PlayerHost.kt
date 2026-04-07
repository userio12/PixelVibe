package com.pixelvibe.vedioplayer.ui.player

import android.content.Context
import android.view.WindowManager
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import is.xyz.mpv.MPVLib

/**
 * Interface abstracting Android system primitives for the player.
 * Allows player logic to be decoupled from Activity/Service context.
 */
interface PlayerHost {
    val context: Context
    val windowManager: WindowManager

    fun setKeepScreenOn(keepOn: Boolean)
    fun setBrightness(brightness: Float)
    fun getBrightness(): Float
    fun setVolume(volume: Float)
    fun getVolume(): Float
    fun getMaxVolume(): Int
    fun requestAudioFocus()
    fun abandonAudioFocus()
    fun setSecureFlags(secure: Boolean)
    fun hideSystemUi()
    fun showSystemUi()
}
