package com.pixelvibe.vedioplayer.ui.mediainfo

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

data class MediaInfo(
    val fileName: String = "",
    val filePath: String = "",
    val fileSize: Long = 0L,
    val duration: Long = 0L,
    // Video
    val videoCodec: String = "",
    val videoWidth: Int = 0,
    val videoHeight: Int = 0,
    val videoFps: Float = 0f,
    val videoBitrate: Long = 0L,
    val videoPixelFmt: String = "",
    // Audio
    val audioCodec: String = "",
    val audioChannels: Int = 0,
    val audioSampleRate: Int = 0,
    val audioBitrate: Long = 0L,
    val audioLanguage: String = "",
    // Subtitles
    val subtitleCodecs: List<String> = emptyList(),
    val subtitleLanguages: List<String> = emptyList(),
    // Container
    val containerFormat: String = "",
    val totalBitrate: Long = 0L
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaInfoScreen(
    mediaInfo: MediaInfo,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Media Info") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
                actions = {
                    IconButton(onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, formatMediaInfo(mediaInfo))
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share media info"))
                    }) {
                        Icon(Icons.Default.Share, "Share")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp)
        ) {
            InfoCard("File") {
                InfoRow("Name", mediaInfo.fileName)
                InfoRow("Path", mediaInfo.filePath)
                InfoRow("Size", formatFileSize(mediaInfo.fileSize))
                InfoRow("Container", mediaInfo.containerFormat)
                InfoRow("Duration", formatDuration(mediaInfo.duration))
                InfoRow("Total Bitrate", formatBitrate(mediaInfo.totalBitrate))
            }

            InfoCard("Video") {
                InfoRow("Codec", mediaInfo.videoCodec)
                InfoRow("Resolution", if (mediaInfo.videoWidth > 0) "${mediaInfo.videoWidth}x${mediaInfo.videoHeight}" else "N/A")
                InfoRow("Frame Rate", if (mediaInfo.videoFps > 0) "${mediaInfo.videoFps} fps" else "N/A")
                InfoRow("Bitrate", formatBitrate(mediaInfo.videoBitrate))
                InfoRow("Pixel Format", mediaInfo.videoPixelFmt)
            }

            InfoCard("Audio") {
                InfoRow("Codec", mediaInfo.audioCodec)
                InfoRow("Channels", if (mediaInfo.audioChannels > 0) mediaInfo.audioChannels.toString() else "N/A")
                InfoRow("Sample Rate", if (mediaInfo.audioSampleRate > 0) "${mediaInfo.audioSampleRate} Hz" else "N/A")
                InfoRow("Bitrate", formatBitrate(mediaInfo.audioBitrate))
                InfoRow("Language", mediaInfo.audioLanguage)
            }

            if (mediaInfo.subtitleCodecs.isNotEmpty()) {
                InfoCard("Subtitles") {
                    mediaInfo.subtitleCodecs.forEachIndexed { index, codec ->
                        val lang = mediaInfo.subtitleLanguages.getOrElse(index) { "Unknown" }
                        InfoRow("Track ${index + 1}", "$codec ($lang)")
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value.ifBlank { "N/A" },
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
        else -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) String.format("%d:%02d:%02d", hours, minutes, seconds)
    else String.format("%02d:%02d", minutes, seconds)
}

private fun formatBitrate(bps: Long): String {
    return when {
        bps < 1000 -> "$bps bps"
        bps < 1000 * 1000 -> String.format("%.1f kbps", bps / 1000.0)
        else -> String.format("%.2f Mbps", bps / 1000000.0)
    }
}

private fun formatMediaInfo(info: MediaInfo): String {
    return buildString {
        appendLine("=== Media Info ===")
        appendLine("File: ${info.fileName}")
        appendLine("Size: ${formatFileSize(info.fileSize)}")
        appendLine("Duration: ${formatDuration(info.duration)}")
        appendLine()
        appendLine("Video: ${info.videoCodec} ${info.videoWidth}x${info.videoHeight} @ ${info.videoFps}fps")
        appendLine("Audio: ${info.audioCodec} ${info.audioChannels}ch @ ${info.audioSampleRate}Hz")
        if (info.subtitleCodecs.isNotEmpty()) {
            appendLine("Subtitles: ${info.subtitleCodecs.joinToString(", ")}")
        }
    }
}
