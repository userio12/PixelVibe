package com.pixelvibe.vedioplayer.ui.preferences

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pixelvibe.vedioplayer.ui.player.PlayerButtonType

/**
 * Default button layouts for different player configurations.
 */
object DefaultLayouts {
    val TopRight = listOf(PlayerButtonType.SPEED, PlayerButtonType.SUBTITLES, PlayerButtonType.AUDIO)
    val BottomLeft = listOf(PlayerButtonType.CHAPTERS, PlayerButtonType.ASPECT_RATIO, PlayerButtonType.PIP)
    val BottomRight = listOf(PlayerButtonType.MORE, PlayerButtonType.ZOOM)
    val PortraitBottom = listOf(PlayerButtonType.SPEED, PlayerButtonType.SUBTITLES, PlayerButtonType.AUDIO, PlayerButtonType.MORE)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ControlLayoutEditorScreen(
    onBack: () -> Unit,
    onSave: (
        topRight: List<PlayerButtonType>,
        bottomLeft: List<PlayerButtonType>,
        bottomRight: List<PlayerButtonType>,
        portraitBottom: List<PlayerButtonType>
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var topRight by remember { mutableStateOf(DefaultLayouts.TopRight.toMutableList()) }
    var bottomLeft by remember { mutableStateOf(DefaultLayouts.BottomLeft.toMutableList()) }
    var bottomRight by remember { mutableStateOf(DefaultLayouts.BottomRight.toMutableList()) }
    var portraitBottom by remember { mutableStateOf(DefaultLayouts.PortraitBottom.toMutableList()) }
    var selectedPreset by remember { mutableStateOf("Default") }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Control Layout Editor") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
                actions = {
                    IconButton(onClick = {
                        onSave(topRight, bottomLeft, bottomRight, portraitBottom)
                    }) {
                        Icon(Icons.Default.Save, "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp)
        ) {
            // Preset selector
            Text(
                "Presets",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Default", "Minimal", "Advanced", "Classic").forEach { preset ->
                    FilterChip(
                        selected = selectedPreset == preset,
                        onClick = { selectedPreset = preset },
                        label = { Text(preset) }
                    )
                }
            }

            androidx.compose.foundation.layout.Spacer(Modifier.padding(12.dp))

            // Layout preview
            Text("Preview", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            LayoutPreview(
                topRight = topRight,
                bottomLeft = bottomLeft,
                bottomRight = bottomRight,
                portraitBottom = portraitBottom
            )

            androidx.compose.foundation.layout.Spacer(Modifier.padding(12.dp))

            // Button slot editors
            ButtonSlotEditor("Top Right", topRight, onButtonsChange = { topRight = it.toMutableList() })
            ButtonSlotEditor("Bottom Left", bottomLeft, onButtonsChange = { bottomLeft = it.toMutableList() })
            ButtonSlotEditor("Bottom Right", bottomRight, onButtonsChange = { bottomRight = it.toMutableList() })
            ButtonSlotEditor("Portrait Bottom", portraitBottom, onButtonsChange = { portraitBottom = it.toMutableList() })
        }
    }
}

@Composable
private fun LayoutPreview(
    topRight: List<PlayerButtonType>,
    bottomLeft: List<PlayerButtonType>,
    bottomRight: List<PlayerButtonType>,
    portraitBottom: List<PlayerButtonType>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Simulated player preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        MaterialTheme.shapes.medium
                    )
                    .padding(8.dp)
            ) {
                // Top bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("◀", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        topRight.forEach { type ->
                            Text(type.name.take(3), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // Bottom bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    bottomLeft.forEach { type ->
                        Text(type.name.take(3), style = MaterialTheme.typography.labelSmall)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    bottomRight.forEach { type ->
                        Text(type.name.take(3), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ButtonSlotEditor(
    slotName: String,
    currentButtons: List<PlayerButtonType>,
    onButtonsChange: (List<PlayerButtonType>) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(slotName, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            PlayerButtonType.entries.forEach { type ->
                FilterChip(
                    selected = type in currentButtons,
                    onClick = {
                        val newButtons = if (type in currentButtons) {
                            currentButtons - type
                        } else {
                            currentButtons + type
                        }
                        onButtonsChange(newButtons)
                    },
                    label = { Text(type.contentDescription) }
                )
            }
        }
    }
}
