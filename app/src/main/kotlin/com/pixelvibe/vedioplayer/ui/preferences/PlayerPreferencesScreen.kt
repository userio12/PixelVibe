package com.pixelvibe.vedioplayer.ui.preferences

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pixelvibe.vedioplayer.preferences.PlayerPreferences
import com.pixelvibe.vedioplayer.preferences.collectAsState

val Orientations = listOf(
    "free" to "Free",
    "video" to "Video",
    "portrait" to "Portrait",
    "landscape" to "Landscape",
    "sensor_landscape" to "Sensor landscape"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerPreferencesScreen(
    preferences: PlayerPreferences,
    onBack: () -> Unit,
    onGestureSettings: () -> Unit = {},
    onControlLayout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val orientation by preferences.orientation.collectAsState()
    val savePosition by preferences.savePositionOnQuit.collectAsState()
    val closeEof by preferences.closeAfterEof.collectAsState()
    val rememberBrightness by preferences.rememberBrightness.collectAsState()
    val autoplay by preferences.autoplayNext.collectAsState()
    val autoPip by preferences.autoPip.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Player") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Orientation", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Orientations.forEach { (value, label) ->
                    FilterChip(
                        selected = orientation == value,
                        onClick = { /* preferences.orientation.set(value) */ },
                        label = { Text(label) }
                    )
                }
            }

            androidx.compose.foundation.layout.Spacer(Modifier.padding(8.dp))

            PrefSwitchRow("Save position on quit", null, savePosition) { /* preferences.savePositionOnQuit.set(it) */ }
            PrefSwitchRow("Close after end of playback", null, closeEof) { /* preferences.closeAfterEof.set(it) */ }
            PrefSwitchRow("Remember brightness", null, rememberBrightness) { /* preferences.rememberBrightness.set(it) */ }
            PrefSwitchRow("Autoplay next video", null, autoplay) { /* preferences.autoplayNext.set(it) */ }
            PrefSwitchRow("Auto Picture-in-Picture", null, autoPip) { /* preferences.autoPip.set(it) */ }

            androidx.compose.foundation.layout.Spacer(Modifier.padding(8.dp))

            // Navigation links
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onGestureSettings)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Gesture settings →", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onControlLayout)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Control layout editor →", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
