package com.pixelvibe.vedioplayer.utils

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import java.io.File

/**
 * Extracts media metadata from video/audio files.
 * Uses Android's built-in MediaMetadataRetriever instead of external library.
 */
object MediaInfoExtractor {
    data class MediaMetadata(
        val fileName: String = "",
        val filePath: String = "",
        val fileSize: Long = 0L,
        val duration: Long = 0L,
        val containerFormat: String = "",
        // Video
        val videoCodec: String = "",
        val videoWidth: Int = 0,
        val videoHeight: Int = 0,
        val videoFps: Float = 0f,
        val videoBitrate: Long = 0L,
        val videoPixelFmt: String = "",
        val videoRotation: Int = 0,
        // Audio
        val audioCodec: String = "",
        val audioChannels: Int = 0,
        val audioSampleRate: Int = 0,
        val audioBitrate: Long = 0L,
        val audioLanguage: String = "",
        // Metadata
        val title: String = "",
        val artist: String = "",
        val album: String = "",
        val date: String = "",
        val author: String = ""
    )

    /**
     * Extract metadata from a file path.
     */
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
                filePath.startsWith("file://") -> {
                    retriever.setDataSource(filePath)
                }
                File(filePath).exists() -> {
                    retriever.setDataSource(filePath)
                }
                else -> {
                    MediaMetadata(fileName = filePath.substringAfterLast('/'))
                }
            }?.let {
                extractMetadata(retriever, filePath)
            } ?: MediaMetadata(fileName = filePath.substringAfterLast('/'))
        } catch (e: Exception) {
            MediaMetadata(fileName = filePath.substringAfterLast('/'))
        } finally {
            try { retriever.release() } catch (_: Exception) {}
        }
    }

    /**
     * Extract metadata from a File object.
     */
    fun extract(context: Context, file: File): MediaMetadata {
        return extract(context, file.absolutePath)
    }

    private fun extractMetadata(retriever: MediaMetadataRetriever, filePath: String): MediaMetadata {
        val fileName = filePath.substringAfterLast('/')

        return MediaMetadata(
            fileName = fileName,
            filePath = filePath,
            fileSize = File(filePath).takeIf { it.exists() }?.length() ?: 0L,
            duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L,
            containerFormat = FileUtils.getExtension(fileName),
            // Video
            videoCodec = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_CODEC) ?: "",
            videoWidth = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0,
            videoHeight = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0,
            videoFps = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)?.toFloatOrNull() ?: 0f,
            videoBitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toLongOrNull() ?: 0L,
            videoPixelFmt = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION) ?: "",
            videoRotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull() ?: 0,
            // Audio
            audioCodec = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUDIO_CODEC) ?: "",
            audioChannels = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUDIO_CHANNELS)?.toIntOrNull() ?: 0,
            audioSampleRate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUDIO_SAMPLERATE)?.toIntOrNull() ?: 0,
            audioBitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUDIO_BITRATE)?.toLongOrNull() ?: 0L,
            // Metadata
            title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: "",
            artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "",
            album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: "",
            date = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE) ?: "",
            author = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR) ?: ""
        )
    }
}
