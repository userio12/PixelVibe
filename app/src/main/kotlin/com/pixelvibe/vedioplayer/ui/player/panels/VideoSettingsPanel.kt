package com.pixelvibe.vedioplayer.ui.player.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class VideoFilterState(
    val brightness: Float = 0f,
    val saturation: Float = 0f,
    val contrast: Float = 0f,
    val gamma: Float = 0f,
    val hue: Float = 0f,
    val sharpness: Float = 0f
)

val FilterPresets = listOf(
    "None" to VideoFilterState(),
    "Vivid" to VideoFilterState(saturation = 3f, contrast = 1f),
    "Warm Tone" to VideoFilterState(brightness = 1f, saturation = 2f, gamma = 1f),
    "Cool Tone" to VideoFilterState(brightness = 1f, hue = -2f, gamma = 1f),
    "Soft Pastel" to VideoFilterState(brightness = 2f, saturation = -1f, contrast = -1f),
    "Cinematic" to VideoFilterState(contrast = 2f, saturation = -1f, sharpness = 1f),
    "Dramatic" to VideoFilterState(contrast = 3f, saturation = -2f, brightness = -1f),
    "Night Mode" to VideoFilterState(brightness = -2f, gamma = -1f),
    "Nostalgic" to VideoFilterState(saturation = -2f, gamma = 2f, brightness = 1f),
    "Ghibli Style" to VideoFilterState(brightness = 2f, saturation = 2f, contrast = -1f),
    "Neon Pop" to VideoFilterState(saturation = 4f, contrast = 2f, brightness = 1f),
    "Deep Black" to VideoFilterState(brightness = -3f, contrast = 3f, gamma = -1f)
)

@Composable
fun VideoSettingsPanel(
    filters: VideoFilterState,
    currentPreset: String,
    debanding: String,
    onFilterChange: (VideoFilterState) -> Unit,
    onPresetChange: (String) -> Unit,
    onDebandingChange: (String) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Video Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(top = 8.dp)
        ) {
            // Filter Presets
            Text("Presets", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FilterPresets.forEach { (name, _) ->
                    FilterChip(
                        selected = name == currentPreset,
                        onClick = { onPresetChange(name) },
                        label = { Text(name) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Debanding
            Text("Debanding", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("None", "CPU", "GPU").forEach { mode ->
                    FilterChip(
                        selected = mode.lowercase() == debanding,
                        onClick = { onDebandingChange(mode.lowercase()) },
                        label = { Text(mode) }
                    )
                }
            }
        }
    }
}

@Composable
fun MultiCardPanel(
    onClose: () -> Unit
) {
    // Placeholder for compound panel with multiple settings cards
    Column(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }
        Text("Multi-card panel placeholder", style = MaterialTheme.typography.bodyMedium)
    }
}
