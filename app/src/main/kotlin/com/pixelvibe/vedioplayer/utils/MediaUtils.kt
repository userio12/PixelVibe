package com.pixelvibe.vedioplayer.utils

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import java.io.File

/**
 * Utility functions for media file operations.
 */
object MediaUtils {
    fun generateThumbnail(context: Context, filePath: String, targetSize: Int = 256): Bitmap? {
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
            retriever.getFrameAtTime(1_000_000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                ?.let { frame ->
                    Bitmap.createScaledBitmap(frame, targetSize, targetSize, true)
                }
        } catch (e: Exception) { null }
        finally { try { retriever.release() } catch (_: Exception) {} }
    }

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
        } catch (e: Exception) { 0L }
        finally { try { retriever.release() } catch (_: Exception) {} }
    }

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
            val w = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt() ?: 0
            val h = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt() ?: 0
            w to h
        } catch (e: Exception) { 0 to 0 }
        finally { try { retriever.release() } catch (_: Exception) {} }
    }

    fun isVideoFile(fileName: String): Boolean {
        val extensions = setOf("mp4", "mkv", "avi", "webm", "mov", "wmv", "flv", "m4v", "3gp", "3g2", "ogv", "vob", "ts", "mts", "m2ts")
        return extensions.any { fileName.endsWith(it, ignoreCase = true) }
    }

    fun isAudioFile(fileName: String): Boolean {
        val extensions = setOf("mp3", "flac", "wav", "aac", "ogg", "wma", "m4a", "opus")
        return extensions.any { fileName.endsWith(it, ignoreCase = true) }
    }

    fun isSubtitleFile(fileName: String): Boolean {
        val extensions = setOf("srt", "vtt", "ass", "ssa", "sub", "idx", "sup", "xml", "ttml", "dfxp", "itt", "ebu", "imsc", "usf", "sbv", "srv1", "srv2", "srv3", "json", "sami", "smi", "mpl", "pjs", "stl", "rt", "psb", "cap", "scc", "vttx", "lrc", "krc", "txt", "pgs")
        return extensions.any { fileName.endsWith(it, ignoreCase = true) }
    }

    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
            else -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }

    fun formatDuration(durationMs: Long): String {
        val totalSeconds = durationMs / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) String.format("%d:%02d:%02d", hours, minutes, seconds)
        else String.format("%02d:%02d", minutes, seconds)
    }
}
