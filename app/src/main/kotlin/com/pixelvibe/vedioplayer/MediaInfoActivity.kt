package com.pixelvibe.vedioplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.pixelvibe.vedioplayer.ui.mediainfo.MediaInfo
import com.pixelvibe.vedioplayer.ui.mediainfo.MediaInfoScreen
import com.pixelvibe.vedioplayer.ui.theme.PixelVibeTheme
import com.pixelvibe.vedioplayer.utils.MediaInfoExtractor
import java.io.File

/**
 * Activity for displaying media file metadata.
 * Handles ACTION_VIEW and ACTION_SEND intents with media files.
 */
class MediaInfoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val mediaInfo = parseIntent(intent)

        setContent {
            PixelVibeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    MediaInfoScreen(
                        mediaInfo = mediaInfo,
                        onBack = { finish() }
                    )
                }
            }
        }
    }

    private fun parseIntent(intent: android.content.Intent): MediaInfo {
        val uri = intent.data ?: run {
            val clipData = intent.clipData
            if (clipData?.itemCount ?: 0 > 0) {
                clipData?.getItemAt(0)?.uri
            } else null
        }

        val filePath = uri?.toString() ?: ""
        val fileName = uri?.lastPathSegment?.substringAfterLast('/') ?: "Unknown"

        val file = if (filePath.startsWith("file://")) {
            File(filePath.removePrefix("file://"))
        } else if (filePath.startsWith("/")) {
            File(filePath)
        } else null

        return if (file != null && file.exists()) {
            val metadata = MediaInfoExtractor.extract(this, file)
            MediaInfo(
                fileName = metadata.fileName,
                filePath = metadata.filePath,
                fileSize = metadata.fileSize,
                duration = metadata.duration,
                videoCodec = metadata.videoCodec,
                videoWidth = metadata.videoWidth,
                videoHeight = metadata.videoHeight,
                videoFps = metadata.videoFps,
                videoBitrate = metadata.videoBitrate,
                videoPixelFmt = metadata.videoPixelFmt,
                audioCodec = metadata.audioCodec,
                audioChannels = metadata.audioChannels,
                audioSampleRate = metadata.audioSampleRate,
                audioBitrate = metadata.audioBitrate,
                audioLanguage = metadata.audioLanguage,
                containerFormat = metadata.containerFormat,
                totalBitrate = metadata.videoBitrate + metadata.audioBitrate
            )
        } else {
            MediaInfo(
                fileName = fileName,
                filePath = filePath
            )
        }
    }
}
