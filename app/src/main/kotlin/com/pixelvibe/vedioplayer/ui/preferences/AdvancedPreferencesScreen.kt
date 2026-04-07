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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
fun AdvancedPreferencesScreen(
    preferences: PlayerPreferences,
    onBack: () -> Unit,
    onEditMpvConf: () -> Unit = {},
    onEditInputConf: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val verboseLogging by remember { mutableStateOf(false) }
    val recentlyPlayed by remember { mutableStateOf(true) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Advanced") },
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
            // Config editors
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                    Text("Edit config.conf", style = MaterialTheme.typography.bodyLarge)
                    Text("Configuration file", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = onEditMpvConf) { Text("Edit") }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                    Text("Edit keys.conf", style = MaterialTheme.typography.bodyLarge)
                    Text("Key bindings configuration", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = onEditInputConf) { Text("Edit") }
            }

            androidx.compose.foundation.layout.Spacer(Modifier.padding(8.dp))

            // Clear caches
            ClearCacheRow("Clear config cache", "Clear cached config.conf settings") { /* Clear */ }
            ClearCacheRow("Clear thumbnail cache", "Delete all cached video thumbnails") { /* Clear */ }
            ClearCacheRow("Clear font cache", "Clear cached fonts") { /* Clear */ }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                    Text("Clear playback history", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
                    Text("Delete all playback positions and states", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = { showClearHistoryDialog = true }) { Text("Clear", color = MaterialTheme.colorScheme.error) }
            }

            PrefSwitchRow("Recently played tracking", "Track and display recently played videos", recentlyPlayed) { }

            PrefSwitchRow("Verbose logging", "Helps in debugging but may hinder performance", verboseLogging) { }
        }
    }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear playback history?") },
            text = { Text("This will delete all playback positions, selected tracks, and delays. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Clear history
                        showClearHistoryDialog = false
                    }
                ) { Text("Clear", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun FoldersPreferencesScreen(
    preferences: PlayerPreferences,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewMode by preferences.folderViewMode.collectAsState()
    val showHidden by preferences.showHiddenFiles.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Folders") },
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
            Text("View mode", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 8.dp))
            Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                listOf("tree" to "Tree", "flat" to "Flat").forEach { (value, label) ->
                    androidx.compose.material3.FilterChip(
                        selected = viewMode == value,
                        onClick = { /* preferences.folderViewMode.set(value) */ },
                        label = { Text(label) }
                    )
                }
            }

            androidx.compose.foundation.layout.Spacer(Modifier.padding(8.dp))
            PrefSwitchRow("Show hidden files", "Show files and folders starting with '.'", showHidden) { /* preferences.showHiddenFiles.set(it) */ }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                    Text("Scan directories", style = MaterialTheme.typography.bodyLarge)
                    Text("Choose folders to scan for media files", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = { /* Open directory picker */ }) { Text("Choose") }
            }
        }
    }
}

@Composable
private fun ClearCacheRow(title: String, summary: String, onClear: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
        Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        TextButton(onClick = onClear) { Text("Clear") }
    }
}
