package com.pixelvibe.vedioplayer.utils

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Utility functions for media file operations.
 */
object MediaUtils {
    /**
     * Generate a thumbnail for a video file.
     */
    fun generateThumbnail(context: Context, filePath: String, targetSize: Int = 256): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            when {
                filePath.startsWith("content://") -> {
                    val uri = Uri.parse(filePath)
                    val fd = context.contentResolver.openFileDescriptor(uri, "r")
                    fd?.let {
                        retriever.setDataSource(it.fileDescriptor)
                    }
                }
                filePath.startsWith("file://") -> {
                    retriever.setDataSource(filePath)
                }
                else -> {
                    retriever.setDataSource(filePath)
                }
            }
            retriever.frameAtTime(1_000_000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                ?.let { frame ->
                    Bitmap.createScaledBitmap(frame, targetSize, targetSize, true)
                }
        } catch (e: Exception) {
            null
        } finally {
            try { retriever.release() } catch (_: Exception) {}
        }
    }

    /**
     * Get video duration in milliseconds.
     */
    fun getVideoDuration(context: Context, filePath: String): Long {
        val retriever = MediaMetadataRetriever()
        return try {
            when {
                filePath.startsWith("content://") -> {
                    val uri = Uri.parse(filePath)
                    context.contentResolver.openFileDescriptor(uri, "r")?.let {
                        retriever.setDataSource(it.fileDescriptor)
                    }
                }
                else -> retriever.setDataSource(filePath)
            }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
        } catch (e: Exception) {
            0L
        } finally {
            try { retriever.release() } catch (_: Exception) {}
        }
    }

    /**
     * Get video dimensions (width x height).
     */
    fun getVideoDimensions(context: Context, filePath: String): Pair<Int, Int> {
        val retriever = MediaMetadataRetriever()
        return try {
            when {
                filePath.startsWith("content://") -> {
                    val uri = Uri.parse(filePath)
                    context.contentResolver.openFileDescriptor(uri, "r")?.let {
                        retriever.setDataSource(it.fileDescriptor)
                    }
                }
                else -> retriever.setDataSource(filePath)
            }
            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt() ?: 0
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt() ?: 0
            width to height
        } catch (e: Exception) {
            0 to 0
        } finally {
            try { retriever.release() } catch (_: Exception) {}
        }
    }

    /**
     * Check if a file is a video based on extension.
     */
    fun isVideoFile(fileName: String): Boolean {
        val extensions = setOf(
            "mp4", "mkv", "avi", "webm", "mov", "wmv", "flv", "m4v",
            "3gp", "3g2", "ogv", "vob", "ts", "mts", "m2ts", "divx",
            "xvid", "rm", "rmvb", "asf", "f4v", "mxf"
        )
        return extensions.any { fileName.endsWith(it, ignoreCase = true) }
    }

    /**
     * Check if a file is an audio file.
     */
    fun isAudioFile(fileName: String): Boolean {
        val extensions = setOf(
            "mp3", "flac", "wav", "aac", "ogg", "wma", "m4a", "opus",
            "alac", "aiff", "ape", "ac3", "dts", "amr", "ra", "mid", "midi"
        )
        return extensions.any { fileName.endsWith(it, ignoreCase = true) }
    }

    /**
     * Check if a file is a subtitle file.
     */
    fun isSubtitleFile(fileName: String): Boolean {
        val extensions = setOf(
            "srt", "vtt", "ass", "ssa", "sub", "idx", "sup", "xml",
            "ttml", "dfxp", "itt", "ebu", "imsc", "usf", "sbv", "srv1",
            "srv2", "srv3", "json", "sami", "smi", "mpl", "pjs", "stl",
            "rt", "psb", "cap", "scc", "vttx", "lrc", "krc", "txt", "pgs"
        )
        return extensions.any { fileName.endsWith(it, ignoreCase = true) }
    }

    /**
     * Format file size for display.
     */
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
            else -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }

    /**
     * Format duration for display (mm:ss or hh:mm:ss).
     */
    fun formatDuration(durationMs: Long): String {
        val totalSeconds = durationMs / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
}
