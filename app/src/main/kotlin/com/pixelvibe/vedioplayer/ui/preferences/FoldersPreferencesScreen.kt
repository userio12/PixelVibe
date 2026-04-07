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
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
            Text(
                "View mode",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("tree" to "Tree", "flat" to "Flat").forEach { (value, label) ->
                    FilterChip(
                        selected = viewMode == value,
                        onClick = { /* preferences.folderViewMode.set(value) */ },
                        label = { Text(label) }
                    )
                }
            }

            androidx.compose.foundation.layout.Spacer(Modifier.padding(8.dp))
            PrefSwitchRow(
                "Show hidden files",
                "Show files and folders starting with '.'",
                showHidden
            ) { /* preferences.showHiddenFiles.set(it) */ }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Folder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                    Text("Scan directories", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Choose folders to scan for media files",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(onClick = { /* Open directory picker */ }) { Text("Choose") }
            }
        }
    }
}
