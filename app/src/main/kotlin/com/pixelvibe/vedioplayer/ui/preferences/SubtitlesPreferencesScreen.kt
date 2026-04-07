package com.pixelvibe.vedioplayer.ui.preferences

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import com.pixelvibe.vedioplayer.preferences.PlayerPreferences
import com.pixelvibe.vedioplayer.preferences.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubtitlesPreferencesScreen(
    preferences: PlayerPreferences,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val onlineSearch by preferences.onlineSubtitleSearch.collectAsState()
    val autoLoad by preferences.autoLoadSubtitles.collectAsState()
    val saveLocation by preferences.subtitleSaveLocation.collectAsState()
    var languages by remember { mutableStateOf(preferences.preferredSubtitleLanguages.get()) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Subtitles") },
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
            PrefSwitchRow("Online subtitle search", "Search and download subtitles from online sources", onlineSearch) { /* preferences.onlineSubtitleSearch.set(it) */ }

            OutlinedTextField(
                value = languages,
                onValueChange = { languages = it },
                label = { Text("Preferred languages (comma-separated)") },
                modifier = Modifier.fillMaxWidth()
            )

            PrefSwitchRow("Automatically load subtitles", "Auto-load external subtitles with the same name", autoLoad) { /* preferences.autoLoadSubtitles.set(it) */ }

            OutlinedTextField(
                value = saveLocation,
                onValueChange = { /* preferences.subtitleSaveLocation.set(it) */ },
                label = { Text("Subtitle save location") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Clear downloaded subtitles", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                IconButton(onClick = { /* Clear subtitles */ }) {
                    Icon(Icons.Default.Delete, "Clear subtitles", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPreferencesScreen(
    preferences: PlayerPreferences,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pitchCorrection by preferences.pitchCorrection.collectAsState()
    val volumeNorm by preferences.volumeNormalization.collectAsState()
    val channels by preferences.audioChannels.collectAsState()
    val boostCap by preferences.volumeBoostCap.collectAsState()
    var lang by remember { mutableStateOf(preferences.preferredAudioLanguage.get()) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Audio") },
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
            PrefSwitchRow("Audio pitch correction", "Prevents audio from becoming high/low-pitched at different speeds", pitchCorrection) { /* preferences.pitchCorrection.set(it) */ }
            PrefSwitchRow("Volume normalization", "Automatically maintain consistent loudness levels", volumeNorm) { /* preferences.volumeNormalization.set(it) */ }

            Text("Audio channels", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
            Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                listOf("auto" to "Auto", "mono" to "Mono", "stereo" to "Stereo", "reversed" to "Reversed").forEach { (value, label) ->
                    androidx.compose.material3.FilterChip(
                        selected = channels == value,
                        onClick = { /* preferences.audioChannels.set(value) */ },
                        label = { Text(label) }
                    )
                }
            }

            OutlinedTextField(
                value = lang,
                onValueChange = { lang = it },
                label = { Text("Preferred audio language(s)") },
                modifier = Modifier.fillMaxWidth()
            )

            PrefSliderRow("Volume boost cap", "Maximum volume boost percentage", boostCap.toFloat(), 100f..200f) { /* preferences.volumeBoostCap.set(it.toInt()) */ }
        }
    }
}
