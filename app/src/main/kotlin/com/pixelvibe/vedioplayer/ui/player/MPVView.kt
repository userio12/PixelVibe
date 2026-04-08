package com.pixelvibe.vedioplayer.ui.player

import android.content.Context
import android.util.AttributeSet
import com.pixelvibe.vedioplayer.preferences.PlayerPreferences
import is.xyz.mpv.BaseMPVView
import is.xyz.mpv.MPVLib
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Wrapper around BaseMPVView from the mpv-android library.
 * Handles MPV initialization, options, and property observation.
 */
class MPVView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseMPVView(context, attrs), KoinComponent {

    private val preferences: PlayerPreferences by inject()

    // Track delegates
    var sid by TrackDelegate("sid", -1)
    var secondarySid by TrackDelegate("secondary-sid", -1)
    var aid by TrackDelegate("aid", -1)

    init {
        MPVLib.create(context)
    }

    /**
     * Initialize MPV options before initializing the surface.
     * Must be called before [initialize].
     */
    fun initOptions() {
        // GPU backend
        val useGpuNext = preferences.gpuNext.get()
        MPVLib.setOptionString("profile", if (useGpuNext) "pixelvibe-gpu-next" else "pixelvibe-gpu")

        // GPU context
        val useVulkan = preferences.vulkan.get()
        MPVLib.setOptionString("gpu-context", if (useVulkan) "androidvk" else "android")

        // Hardware decoding
        val hwDec = if (preferences.hwDecoding.get()) "mediacodec" else "no"
        MPVLib.setOptionString("hwdec", hwDec)

        // Demuxer cache
        MPVLib.setOptionString("demuxer-max-bytes", "150MiB")
        MPVLib.setOptionString("demuxer-max-back-bytes", "20MiB")

        // TLS
        MPVLib.setOptionString("tls-verify", "yes")
        MPVLib.setOptionString("tls-ca-file", "cacert.pem")

        // Screenshot directory
        MPVLib.setOptionString("screenshot-dir", context.cacheDir.absolutePath)

        // Speed
        MPVLib.setOptionString("speed", "1.0")

        // Audio pitch correction
        if (preferences.pitchCorrection.get()) {
            MPVLib.setOptionString("audio-pitch-correction", "yes")
        }

        // Volume
        MPVLib.setOptionString("volume-max", "100")

        // Subtitle fonts directory
        MPVLib.setOptionString("sub-fonts-dir", "${context.cacheDir.absolutePath}/fonts")

        // Subtitle options
        MPVLib.setOptionString("sub-ass", "yes")
        MPVLib.setOptionString("sub-scale", "0")
    }

    /**
     * Post-initialization options (after surface is ready).
     */
    fun postInitOptions() {
        when (preferences.debanding.get()) {
            "cpu" -> MPVLib.setOptionString("vf", "gradfun")
            "gpu" -> MPVLib.setOptionString("deband", "yes")
        }
    }

    /**
     * Observe MPV properties for UI state updates.
     */
    fun observeProperties() {
        listOf(
            "pause", "paused-for-cache", "time-pos", "duration", "eof-reached",
            "video-params/aspect", "video-params/w", "video-params/h", "video-params/fps",
            "speed", "chapter", "chapter-list", "track-list",
            "file-path", "filename", "media-title", "sid", "aid", "sub-delay", "audio-delay"
        ).forEach { prop -> MPVLib.observeProperty(prop) }
    }

    /**
     * Get the video output aspect ratio.
     */
    fun getVideoOutAspect(): Double {
        MPVLib.getPropertyDouble("video-params/aspect")?.let { if (it > 0) return it }
        val w = MPVLib.getPropertyInt("video-params/w") ?: 0
        val h = MPVLib.getPropertyInt("video-params/h") ?: 0
        if (w > 0 && h > 0) return w.toDouble() / h.toDouble()
        return 16.0 / 9.0
    }

    /**
     * Load a file into MPV.
     */
    fun loadFile(
        path: String,
        mode: String = "replace",
        options: Map<String, String> = emptyMap()
    ) {
        val uri = android.net.Uri.parse(path)
        val uriString = if (uri.scheme == null) path else uri.toString()

        val cmd = mutableListOf("loadfile", uriString, mode)
        options.forEach { (key, value) -> cmd.add("$key=$value") }
        MPVLib.command(cmd.toTypedArray())
    }

    /**
     * Cleanup and destroy the MPV instance.
     */
    fun destroy() {
        MPVLib.destroy()
    }

    /**
     * Delegate for MPV track properties (sid, aid, etc.)
     */
    inner class TrackDelegate(private val propertyName: String, private val defaultValue: Int) {
        operator fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): Int =
            MPVLib.getPropertyInt(propertyName) ?: defaultValue

        operator fun setValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>, value: Int) {
            MPVLib.setPropertyInt(propertyName, value)
        }
    }
}
