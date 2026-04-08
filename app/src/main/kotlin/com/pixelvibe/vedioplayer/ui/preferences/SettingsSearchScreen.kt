package com.pixelvibe.vedioplayer.ui.preferences

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pixelvibe.vedioplayer.R

data class SearchablePreference(
    val title: String,
    val summary: String = "",
    val parentScreen: String = "",
    val route: String = ""
)

val AllSearchablePreferences = listOf(
    SearchablePreference("AMOLED Black Mode", "Pure black background", "Appearance"),
    SearchablePreference("Material You", "Dynamic colors", "Appearance"),
    SearchablePreference("App Theme", "Theme selection", "Appearance"),
    SearchablePreference("Show 'New' label", "Mark unplayed videos", "Appearance"),
    SearchablePreference("Watched threshold", "Mark as watched at %", "Appearance"),
    SearchablePreference("Show full names", "Don't truncate names", "Appearance"),
    SearchablePreference("Orientation", "Screen orientation", "Player"),
    SearchablePreference("Save position on quit", "Resume playback", "Player"),
    SearchablePreference("Close after EOF", "Close after playback ends", "Player"),
    SearchablePreference("Autoplay next video", "Auto-play next file", "Player"),
    SearchablePreference("Auto Picture-in-Picture", "Enter PiP on home", "Player"),
    SearchablePreference("Double-tap (left)", "Left tap action", "Gestures"),
    SearchablePreference("Double-tap (center)", "Center tap action", "Gestures"),
    SearchablePreference("Double-tap (right)", "Right tap action", "Gestures"),
    SearchablePreference("Use single tap for center", "Tap vs double-tap", "Gestures"),
    SearchablePreference("Media controls side swap", "Swap brightness/volume", "Gestures"),
    SearchablePreference("Seekbar style", "Standard/Wavy/Thick", "Controls"),
    SearchablePreference("Wavy seekbar animation", "Animated sine wave", "Controls"),
    SearchablePreference("Custom skip duration", "Skip button duration", "Controls"),
    SearchablePreference("Hardware decoding", "Mediacodec HW decode", "Decoder"),
    SearchablePreference("Use gpu-next", "New rendering backend", "Decoder"),
    SearchablePreference("Use Vulkan", "Vulkan rendering", "Decoder"),
    SearchablePreference("YUV420P pixel format", "Fix black screens", "Decoder"),
    SearchablePreference("Debanding", "None/CPU/GPU", "Decoder"),
    SearchablePreference("Online subtitle search", "Search subtitles online", "Subtitles"),
    SearchablePreference("Automatically load subtitles", "Auto-load external subs", "Subtitles"),
    SearchablePreference("Audio pitch correction", "Prevent pitch changes", "Audio"),
    SearchablePreference("Volume normalization", "Consistent loudness", "Audio"),
    SearchablePreference("Show hidden files", "Show dotfiles", "Folders"),
    SearchablePreference("Edit config.conf", "Configuration", "Advanced"),
    SearchablePreference("Edit keys.conf", "Key bindings", "Advanced"),
    SearchablePreference("Clear playback history", "Delete positions", "Advanced"),
    SearchablePreference("Verbose logging", "Debug logging", "Advanced"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSearchScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }

    val results = remember(query) {
        if (query.isBlank()) emptyList()
        else AllSearchablePreferences.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.summary.contains(query, ignoreCase = true) ||
            it.parentScreen.contains(query, ignoreCase = true)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_search_title)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text(stringResource(R.string.settings_search_hint)) },
                leadingIcon = { Icon(Icons.Default.Search, "Search") },
                singleLine = true
            )

            if (query.isNotBlank() && results.isEmpty()) {
                Text(
                    text = stringResource(R.string.settings_search_no_results),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            LazyColumn {
                items(results) { pref ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { /* Navigate to pref */ }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Column {
                            Text(pref.title, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = "${pref.summary} — ${pref.parentScreen}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
