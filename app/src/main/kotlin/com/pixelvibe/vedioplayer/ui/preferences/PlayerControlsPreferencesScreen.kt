package com.pixelvibe.vedioplayer.ui.preferences

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pixelvibe.vedioplayer.preferences.PlayerPreferences
import com.pixelvibe.vedioplayer.preferences.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerControlsPreferencesScreen(
    preferences: PlayerPreferences,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val seekbarStyle by preferences.seekbarStyle.collectAsState()
    val wavySeekbar by preferences.wavySeekbar.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Player Controls") },
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
            Text("Seekbar Style", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("standard" to "Standard", "wavy" to "Wavy", "thick" to "Thick").forEach { (value, label) ->
                    FilterChip(
                        selected = seekbarStyle == value,
                        onClick = { /* preferences.seekbarStyle.set(value) */ },
                        label = { Text(label) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            PrefSwitchRow("Wavy seekbar animation", "Animated sine wave that flattens when paused", wavySeekbar) {}
            PrefSliderRow("Custom skip duration", "Duration in seconds", preferences.customSkipDuration.get().toFloat(), 10f..300f) {}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecoderPreferencesScreen(
    preferences: PlayerPreferences,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hwDec by preferences.hwDecoding.collectAsState()
    val gpuNext by preferences.gpuNext.collectAsState()
    val vulkan by preferences.vulkan.collectAsState()
    val yuv420p by preferences.yuv420p.collectAsState()
    val deband by preferences.debanding.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Decoder") },
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
            PrefSwitchRow("Hardware decoding", "Enable hardware-accelerated decoding", hwDec) {}
            PrefSwitchRow("Use gpu-next", "New rendering backend", gpuNext) {}
            PrefSwitchRow("Use Vulkan", "Enable Vulkan rendering (Android 13+)", vulkan) {}
            PrefSwitchRow("YUV420P pixel format", "May fix black screens on some codecs", yuv420p) {}

            Spacer(Modifier.height(12.dp))
            Text("Debanding", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("none" to "None", "cpu" to "CPU", "gpu" to "GPU").forEach { (value, label) ->
                    FilterChip(selected = deband == value, onClick = { /* TODO */ }, label = { Text(label) })
                }
            }
        }
    }
}

@Composable
internal fun PrefSwitchRow(title: String, summary: String? = null, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            summary?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
internal fun PrefSliderRow(title: String, summary: String? = null, value: Float, valueRange: ClosedFloatingPointRange<Float>, onValueChange: (Float) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                summary?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            Text(value.toInt().toString(), style = MaterialTheme.typography.labelLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
        }
        androidx.compose.material3.Slider(value = value, onValueChange = onValueChange, valueRange = valueRange)
    }
}
