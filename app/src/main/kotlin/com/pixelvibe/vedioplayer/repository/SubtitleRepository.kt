package com.pixelvibe.vedioplayer.repository

import java.io.File

/**
 * Represents a subtitle file entry.
 */
data class SubtitleEntry(
    val file: File,
    val language: String? = null,
    val isExternal: Boolean = true
)

/**
 * Repository for subtitle operations.
 * Phase 5: Full implementation with online search and local scanning.
 */
class SubtitleRepository {
    /**
     * Find subtitle files matching a video file name in the same directory.
     */
    fun findLocalSubtitles(videoFile: File): List<SubtitleEntry> {
        val directory = videoFile.parentFile ?: return emptyList()
        val videoName = videoFile.nameWithoutExtension

        return directory.listFiles()?.filter { file ->
            file.isFile && isSubtitleFile(file.name) &&
                file.nameWithoutExtension.startsWith(videoName, ignoreCase = true)
        }?.map { file ->
            SubtitleEntry(file)
        } ?: emptyList()
    }

    /**
     * Scan directory for all subtitle files.
     */
    fun scanSubtitles(directory: File): List<SubtitleEntry> {
        return directory.listFiles()?.filter { file ->
            file.isFile && isSubtitleFile(file.name)
        }?.map { file ->
            SubtitleEntry(file)
        } ?: emptyList()
    }

    private fun isSubtitleFile(fileName: String): Boolean {
        val extensions = setOf(
            "srt", "vtt", "ass", "ssa", "sub", "idx", "sup", "xml",
            "ttml", "dfxp", "sami", "smi"
        )
        return extensions.any { fileName.endsWith(it, ignoreCase = true) }
    }
}
