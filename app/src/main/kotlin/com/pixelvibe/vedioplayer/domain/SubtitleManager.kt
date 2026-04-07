package com.pixelvibe.vedioplayer.domain

import android.content.Context
import android.net.Uri
import com.pixelvibe.vedioplayer.preferences.PlayerPreferences
import com.pixelvibe.vedioplayer.utils.FileUtils
import com.pixelvibe.vedioplayer.utils.MediaUtils
import is.xyz.mpv.MPVLib
import java.io.File

/**
 * Represents a subtitle track (embedded or external).
 */
data class SubtitleTrack(
    val id: Int,
    val title: String,
    val language: String? = null,
    val codec: String? = null,
    val isExternal: Boolean = false,
    val filePath: String? = null,
    val isSelected: Boolean = false
)

/**
 * Represents an audio track.
 */
data class AudioTrack(
    val id: Int,
    val title: String,
    val language: String? = null,
    val codec: String? = null,
    val channels: Int = 2,
    val samplerate: Int = 0,
    val isSelected: Boolean = false
)

/**
 * Manages subtitle and audio track operations.
 */
class SubtitleManager(
    private val context: Context,
    private val preferences: PlayerPreferences
) {
    private var currentVideoPath: String? = null

    /**
     * Set the current video file for subtitle scanning.
     */
    fun setCurrentVideo(path: String) {
        currentVideoPath = path
    }

    /**
     * Scan for local subtitles matching the current video file.
     * Returns both embedded (id >= 0) and external subtitle tracks.
     */
    fun scanLocalSubtitles(videoFile: File, embeddedTracks: List<SubtitleTrack>): List<SubtitleTrack> {
        val tracks = embeddedTracks.toMutableList()

        // Scan same directory for matching subtitle files
        val directory = videoFile.parentFile ?: return tracks.toList()
        val videoName = FileUtils.getNameWithoutExtension(videoFile.name)

        directory.listFiles()?.forEach { file ->
            if (file.isFile && MediaUtils.isSubtitleFile(file.name)) {
                val fileName = FileUtils.getNameWithoutExtension(file.name)

                // Check if subtitle name starts with video name (exact or partial match)
                if (fileName.startsWith(videoName, ignoreCase = true) ||
                    fileName.contains(videoName, ignoreCase = true)) {
                    val codec = FileUtils.getExtension(file.name).uppercase()
                    tracks.add(
                        SubtitleTrack(
                            id = tracks.size + 100, // External IDs start at 100
                            title = FileUtils.sanitizeDisplayName(file.name),
                            codec = codec,
                            isExternal = true,
                            filePath = file.absolutePath
                        )
                    )
                }
            }
        }

        // Also check configured save location
        val saveLocation = preferences.subtitleSaveLocation.get()
        if (saveLocation.isNotBlank()) {
            val saveDir = File(saveLocation)
            if (saveDir.exists() && saveDir.isDirectory) {
                saveDir.listFiles()?.forEach { file ->
                    if (file.isFile && MediaUtils.isSubtitleFile(file.name)) {
                        val codec = FileUtils.getExtension(file.name).uppercase()
                        tracks.add(
                            SubtitleTrack(
                                id = tracks.size + 200,
                                title = FileUtils.sanitizeDisplayName(file.name),
                                codec = codec,
                                isExternal = true,
                                filePath = file.absolutePath
                            )
                        )
                    }
                }
            }
        }

        // Apply preferred languages ordering
        val preferredLangs = preferences.preferredSubtitleLanguages.get()
            .split(",")
            .map { it.trim().lowercase() }
            .filter { it.isNotBlank() }

        return if (preferredLangs.isNotEmpty()) {
            tracks.sortedByDescending { track ->
                preferredLangs.indexOfFirst { lang ->
                    track.language?.contains(lang, ignoreCase = true) == true ||
                    track.title.contains(lang, ignoreCase = true)
                }
            }
        } else {
            tracks.toList()
        }
    }

    /**
     * Load an external subtitle file.
     */
    fun loadExternalSubtitle(filePath: String, asSecondary: Boolean = false) {
        if (asSecondary) {
            // Load as secondary subtitle
            MPVLib.command(
                arrayOf("sub-add", filePath, "cached", "secondary")
            )
        } else {
            // Load as primary subtitle
            MPVLib.command(
                arrayOf("sub-add", filePath, "cached", "primary")
            )
        }
    }

    /**
     * Load external subtitle from URI (SAF).
     */
    fun loadExternalSubtitleUri(uri: Uri) {
        try {
            // For content URIs, we need to copy to cache first
            val cacheFile = File(context.cacheDir, "external_sub_${System.currentTimeMillis()}.srt")
            context.contentResolver.openInputStream(uri)?.use { input ->
                cacheFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            loadExternalSubtitle(cacheFile.absolutePath)
        } catch (e: Exception) {
            // Fallback: try loading URI directly
            MPVLib.command(
                arrayOf("sub-add", uri.toString(), "cached", "primary")
            )
        }
    }

    /**
     * Apply subtitle typography settings to MPV.
     */
    fun applySubtitleTypography() {
        val fontSize = preferences.subtitleFontSize.get()
        val fontName = preferences.subtitleFontName.get()
        val bold = preferences.subtitleBold.get()
        val textColor = preferences.subtitleTextColor.get()
        val borderColor = preferences.subtitleBorderColor.get()
        val bgColor = preferences.subtitleBackgroundColor.get()
        val borderSize = preferences.subtitleBorderSize.get()
        val scale = preferences.subtitleScale.get()
        val position = preferences.subtitlePosition.get()
        val overrideAss = preferences.subtitleOverrideAss.get()
        val scaleByWindow = preferences.subtitleScaleByWindow.get()

        if (fontSize > 0) {
            MPVLib.setPropertyDouble("sub-scale", fontSize / 50.0)
        }
        if (fontName.isNotBlank()) {
            MPVLib.setPropertyString("sub-font", fontName)
        }
        if (bold) {
            MPVLib.setPropertyString("sub-fonts-dir", "")
            // Bold is handled by ASS styling
        }

        // Colors (MPV uses #AABBGGRR format)
        if (textColor != -1) {
            MPVLib.setPropertyString("sub-color", colorIntToString(textColor))
        }
        if (borderColor != -1) {
            MPVLib.setPropertyString("sub-border-color", colorIntToString(borderColor))
        }
        if (bgColor != 0) {
            MPVLib.setPropertyString("sub-back-color", colorIntToString(bgColor))
        }
        if (borderSize > 0) {
            MPVLib.setPropertyDouble("sub-border-size", borderSize.toDouble())
        }
        if (scale != 1.0f) {
            MPVLib.setPropertyDouble("sub-scale", scale.toDouble())
        }
        if (position != 0) {
            MPVLib.setPropertyDouble("sub-pos", position.toDouble())
        }

        MPVLib.setPropertyBoolean("sub-ass-override", if (overrideAss) "force" else "no")
        MPVLib.setPropertyBoolean("sub-scale-by-window", scaleByWindow)
    }

    /**
     * Set subtitle delay in milliseconds.
     */
    fun setSubtitleDelay(delayMs: Float) {
        MPVLib.setPropertyDouble("sub-delay", delayMs / 1000.0)
    }

    /**
     * Set secondary subtitle delay in milliseconds.
     */
    fun setSecondarySubtitleDelay(delayMs: Float) {
        MPVLib.setPropertyDouble("secondary-sub-delay", delayMs / 1000.0)
    }

    /**
     * Save external subtitle to save location.
     */
    fun saveSubtitleFile(sourceFile: File) {
        val saveLocation = preferences.subtitleSaveLocation.get()
        if (saveLocation.isBlank()) return

        val saveDir = File(saveLocation)
        if (!saveDir.exists()) saveDir.mkdirs()

        val destFile = File(saveDir, sourceFile.name)
        if (!destFile.exists()) {
            sourceFile.copyTo(destFile)
        }
    }

    /**
     * Convert Android color int to MPV color string (#AARRGGBB -> #BBGGRRAA).
     */
    private fun colorIntToString(color: Int): String {
        val a = (color shr 24) and 0xFF
        val r = (color shr 16) and 0xFF
        val g = (color shr 8) and 0xFF
        val b = color and 0xFF
        return String.format("#%02X%02X%02X%02X", b, g, r, a)
    }
}
