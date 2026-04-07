package com.pixelvibe.vedioplayer.ui.player

import android.content.Context
import android.util.AttributeSet
import androidx.core.net.toUri
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
        initializeLibrary()
    }

    private fun initializeLibrary() {
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
        if (useVulkan) {
            MPVLib.setOptionString("gpu-context", "androidvk")
        } else {
            MPVLib.setOptionString("gpu-context", "android")
        }

        // Hardware decoding
        val hwDec = if (preferences.hwDecoding.get()) "mediacodec" else "no"
        MPVLib.setOptionString("hwdec", hwDec)

        // YUV420P
        if (preferences.yuv420p.get()) {
            MPVLib.setOptionString("vd-lavc-o", "pix_fmt=yuv420p")
        }

        // Demuxer cache for smoother streaming
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
        MPVLib.setOptionString("sub-fonts-dir", context.cacheDir.absolutePath + "/fonts")

        // Subtitle options
        MPVLib.setOptionString("sub-ass", "yes")
        MPVLib.setOptionString("sub-scale", "0")
    }

    /**
     * Post-initialization options (after surface is ready).
     */
    fun postInitOptions() {
        // Debanding
        when (preferences.debanding.get()) {
            "cpu" -> MPVLib.setOptionString("vf", "gradfun")
            "gpu" -> MPVLib.setOptionString("deband", "yes")
            else -> {} // none
        }
    }

    /**
     * Observe MPV properties for UI state updates.
     */
    fun observeProperties() {
        val properties = listOf(
            "pause",
            "paused-for-cache",
            "time-pos",
            "duration",
            "eof-reached",
            "video-params/aspect",
            "video-params/w",
            "video-params/h",
            "video-params/fps",
            "speed",
            "chapter",
            "chapter-list",
            "track-list",
            "file-path",
            "filename",
            "media-title",
            "sid",
            "aid",
            "sub-delay",
            "audio-delay",
        )

        properties.forEach { prop ->
            MPVLib.observeProperty(prop)
        }
    }

    /**
     * Get the video output aspect ratio.
     */
    fun getVideoOutAspect(): Double {
        // Try video-params first
        val aspect = MPVLib.getPropertyDouble("video-params/aspect")
        if (aspect != null && aspect > 0) return aspect

        // Fallback to width/height
        val w = MPVLib.getPropertyInt("video-params/w") ?: 0
        val h = MPVLib.getPropertyInt("video-params/h") ?: 0
        if (w > 0 && h > 0) return w.toDouble() / h.toDouble()

        return 16.0 / 9.0 // Default fallback
    }

    /**
     * Load a file into MPV.
     */
    fun loadFile(
        path: String,
        mode: String = "replace",
        options: Map<String, String> = emptyMap()
    ) {
        val uri = path.toUri()
        val uriString = if (uri.scheme == null) path else uri.toString()

        val cmd = mutableListOf("loadfile", uriString, mode)
        options.forEach { (key, value) ->
            cmd.add("$key=$value")
        }

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
        operator fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): Int {
            return MPVLib.getPropertyInt(propertyName) ?: defaultValue
        }

        operator fun setValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>, value: Int) {
            MPVLib.setPropertyInt(propertyName, value)
        }
    }
}
