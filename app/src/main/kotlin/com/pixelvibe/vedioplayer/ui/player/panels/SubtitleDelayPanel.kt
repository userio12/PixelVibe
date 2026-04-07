package com.pixelvibe.vedioplayer.ui.player.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SubtitleDelayPanel(
    primaryDelay: Float,
    secondaryDelay: Float,
    primarySpeed: Float,
    secondarySpeed: Float,
    onPrimaryDelayChange: (Float) -> Unit,
    onSecondaryDelayChange: (Float) -> Unit,
    onPrimarySpeedChange: (Float) -> Unit,
    onSecondarySpeedChange: (Float) -> Unit,
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
                text = "Subtitle Delay",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(Modifier.height(16.dp))

        // Primary subtitle
        Text("Primary", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        Text("Voice heard ← → Text seen", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Slider(
            value = primaryDelay,
            onValueChange = onPrimaryDelayChange,
            valueRange = -60000f..60000f
        )
        Text("${String.format("%.0f", primaryDelay)} ms", style = MaterialTheme.typography.bodyMedium)

        Spacer(Modifier.height(16.dp))

        // Secondary subtitle
        Text("Secondary", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        Slider(
            value = secondaryDelay,
            onValueChange = onSecondaryDelayChange,
            valueRange = -60000f..60000f
        )
        Text("${String.format("%.0f", secondaryDelay)} ms", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun AudioDelayPanel(
    delay: Float,
    onDelayChange: (Float) -> Unit,
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
                text = "Audio Delay",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(Modifier.height(16.dp))

        Text("Sound heard ← → Sound spotted", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Slider(
            value = delay,
            onValueChange = onDelayChange,
            valueRange = -60000f..60000f
        )
        Text("${String.format("%.0f", delay)} ms", style = MaterialTheme.typography.bodyMedium)
    }
}
