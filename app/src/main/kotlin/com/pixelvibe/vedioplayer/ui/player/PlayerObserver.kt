package com.pixelvibe.vedioplayer.ui.player

import android.os.Handler
import android.os.Looper
import is.xyz.mpv.MPVLib

/**
 * Bridges MPV events from the native thread to the Android UI thread.
 * Observes MPV properties and events, forwarding them to the ViewModel.
 */
class PlayerObserver(
    private val onEvent: (MPVLib.Event) -> Unit,
    private val onPropertyChange: (String, Any?) -> Unit
) : MPVLib.EventObserver {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var isExiting = false

    fun markExiting() {
        isExiting = true
    }

    override fun eventProperty(property: String) {
        if (isExiting) return
        mainHandler.post {
            onPropertyChange(property, null)
        }
    }

    override fun eventProperty(property: String, value: Boolean) {
        if (isExiting) return
        mainHandler.post {
            onPropertyChange(property, value)
        }
    }

    override fun eventProperty(property: String, value: Long) {
        if (isExiting) return
        mainHandler.post {
            onPropertyChange(property, value)
        }
    }

    override fun eventProperty(property: String, value: Double) {
        if (isExiting) return
        mainHandler.post {
            onPropertyChange(property, value)
        }
    }

    override fun eventProperty(property: String, value: String) {
        if (isExiting) return
        mainHandler.post {
            onPropertyChange(property, value)
        }
    }

    override fun event(event: MPVLib.Event) {
        if (isExiting) return
        mainHandler.post {
            onEvent(event)
        }
    }

    fun cleanup() {
        isExiting = true
    }
}
