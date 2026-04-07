package com.pixelvibe.vedioplayer.domain

import android.content.Context
import com.pixelvibe.vedioplayer.preferences.PlayerPreferences
import com.pixelvibe.vedioplayer.utils.FileUtils
import com.pixelvibe.vedioplayer.utils.MediaUtils
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
 * Pure domain logic — no video engine dependencies.
 */
class SubtitleManager(
    private val context: Context,
    private val preferences: PlayerPreferences
) {
    private var currentVideoPath: String? = null

    fun setCurrentVideo(path: String) { currentVideoPath = path }

    /**
     * Scan for local subtitles matching the current video file.
     */
    fun scanLocalSubtitles(videoFile: File, embeddedTracks: List<SubtitleTrack>): List<SubtitleTrack> {
        val tracks = embeddedTracks.toMutableList()
        val directory = videoFile.parentFile ?: return tracks.toList()
        val videoName = FileUtils.getNameWithoutExtension(videoFile.name)

        directory.listFiles()?.forEach { file ->
            if (file.isFile && MediaUtils.isSubtitleFile(file.name)) {
                val fileName = FileUtils.getNameWithoutExtension(file.name)
                if (fileName.startsWith(videoName, ignoreCase = true) || fileName.contains(videoName, ignoreCase = true)) {
                    tracks.add(SubtitleTrack(
                        id = tracks.size + 100,
                        title = FileUtils.sanitizeDisplayName(file.name),
                        codec = FileUtils.getExtension(file.name).uppercase(),
                        isExternal = true,
                        filePath = file.absolutePath
                    ))
                }
            }
        }

        // Check configured save location
        val saveLocation = preferences.subtitleSaveLocation.get()
        if (saveLocation.isNotBlank()) {
            val saveDir = File(saveLocation)
            if (saveDir.exists() && saveDir.isDirectory) {
                saveDir.listFiles()?.forEach { file ->
                    if (file.isFile && MediaUtils.isSubtitleFile(file.name)) {
                        tracks.add(SubtitleTrack(
                            id = tracks.size + 200,
                            title = FileUtils.sanitizeDisplayName(file.name),
                            codec = FileUtils.getExtension(file.name).uppercase(),
                            isExternal = true,
                            filePath = file.absolutePath
                        ))
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

    fun loadExternalSubtitle(filePath: String, asSecondary: Boolean = false) {
        // TODO: Implement when video engine is added
    }

    fun loadExternalSubtitleUri(uri: android.net.Uri) {
        // TODO: Implement when video engine is added
    }

    fun applySubtitleTypography() {
        // TODO: Implement when video engine is added
    }

    fun setSubtitleDelay(delayMs: Float) {
        // TODO: Implement when video engine is added
    }

    fun setSecondarySubtitleDelay(delayMs: Float) {
        // TODO: Implement when video engine is added
    }

    fun saveSubtitleFile(sourceFile: File) {
        val saveLocation = preferences.subtitleSaveLocation.get()
        if (saveLocation.isBlank()) return
        val saveDir = File(saveLocation)
        if (!saveDir.exists()) saveDir.mkdirs()
        val destFile = File(saveDir, sourceFile.name)
        if (!destFile.exists()) sourceFile.copyTo(destFile)
    }
}
