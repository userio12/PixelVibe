package com.pixelvibe.vedioplayer.ui.preferences

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AudioVideo
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

sealed class SettingsDestination(val route: String, val title: String) {
    data object Appearance : SettingsDestination("appearance", "Appearance")
    data object Player : SettingsDestination("player", "Player")
    data object Gestures : SettingsDestination("gestures", "Gestures")
    data object Controls : SettingsDestination("controls", "Player Controls")
    data object Decoder : SettingsDestination("decoder", "Decoder")
    data object Subtitles : SettingsDestination("subtitles", "Subtitles")
    data object Audio : SettingsDestination("audio", "Audio")
    data object Folders : SettingsDestination("folders", "Folders")
    data object Advanced : SettingsDestination("advanced", "Advanced")
    data object About : SettingsDestination("about", "About")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(
    onNavigate: (SettingsDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Settings") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            item {
                SettingsCard("Appearance", Icons.Default.Palette, "Theme, AMOLED mode, Material You") {
                    onNavigate(SettingsDestination.Appearance)
                }
                SettingsCard("Player", Icons.Default.Videocam, "Orientation, gestures, display") {
                    onNavigate(SettingsDestination.Player)
                }
                SettingsCard("Gestures", Icons.Default.Gesture, "Double-tap, swipe controls") {
                    onNavigate(SettingsDestination.Gestures)
                }
                SettingsCard("Player Controls", Icons.Default.Tune, "Seekbar, buttons, layout") {
                    onNavigate(SettingsDestination.Controls)
                }
                SettingsCard("Decoder", Icons.Default.Code, "HW decoding, gpu-next, debanding") {
                    onNavigate(SettingsDestination.Decoder)
                }
                SettingsCard("Subtitles", Icons.Default.Subtitles, "Languages, fonts, search") {
                    onNavigate(SettingsDestination.Subtitles)
                }
                SettingsCard("Audio", Icons.Default.VolumeUp, "Channels, normalization, boost") {
                    onNavigate(SettingsDestination.Audio)
                }
                SettingsCard("Folders", Icons.Default.Folder, "View mode, scan directories") {
                    onNavigate(SettingsDestination.Folders)
                }
                SettingsCard("Advanced", Icons.Default.Settings, "Config files, cache, logging") {
                    onNavigate(SettingsDestination.Advanced)
                }
                SettingsCard("About", Icons.Default.Info, "Version, libraries, donations") {
                    onNavigate(SettingsDestination.About)
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    icon: ImageVector,
    summary: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
