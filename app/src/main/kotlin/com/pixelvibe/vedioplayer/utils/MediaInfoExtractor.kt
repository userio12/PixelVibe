package com.pixelvibe.vedioplayer.utils

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import java.io.File

/**
 * Extracts media metadata from video/audio files.
 */
object MediaInfoExtractor {
    data class MediaMetadata(
        val fileName: String = "",
        val filePath: String = "",
        val fileSize: Long = 0L,
        val duration: Long = 0L,
        val containerFormat: String = "",
        val videoWidth: Int = 0,
        val videoHeight: Int = 0,
        val videoFps: Float = 0f,
        val videoBitrate: Long = 0L,
        val title: String = "",
        val artist: String = "",
        val album: String = ""
    )

    fun extract(context: Context, filePath: String): MediaMetadata {
        val retriever = MediaMetadataRetriever()
        return try {
            when {
                filePath.startsWith("content://") -> {
                    val uri = Uri.parse(filePath)
                    context.contentResolver.openFileDescriptor(uri, "r")?.let {
                        retriever.setDataSource(it.fileDescriptor)
                    }
                }
                File(filePath).exists() -> retriever.setDataSource(filePath)
                else -> MediaMetadata(fileName = filePath.substringAfterLast('/'))
            }?.let {
                buildMetadata(retriever, filePath)
            } ?: MediaMetadata(fileName = filePath.substringAfterLast('/'))
        } catch (e: Exception) {
            MediaMetadata(fileName = filePath.substringAfterLast('/'))
        } finally {
            try { retriever.release() } catch (_: Exception) {}
        }
    }

    fun extract(context: Context, file: File): MediaMetadata = extract(context, file.absolutePath)

    private fun buildMetadata(retriever: MediaMetadataRetriever, filePath: String): MediaMetadata {
        return MediaMetadata(
            fileName = filePath.substringAfterLast('/'),
            filePath = filePath,
            fileSize = File(filePath).takeIf { it.exists() }?.length() ?: 0L,
            duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L,
            containerFormat = FileUtils.getExtension(filePath),
            videoWidth = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0,
            videoHeight = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0,
            videoFps = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)?.toFloatOrNull() ?: 0f,
            videoBitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toLongOrNull() ?: 0L,
            title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: "",
            artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "",
            album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: ""
        )
    }
}
