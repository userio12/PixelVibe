package com.pixelvibe.vedioplayer.ui.player.sheets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreSheet(
    isBackgroundPlaybackEnabled: Boolean,
    onBackgroundPlaybackToggle: () -> Unit,
    onSnapshot: () -> Unit,
    onSnapshotWithSubs: () -> Unit,
    onSleepTimer: () -> Unit,
    onMediaInfo: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    ) {
        Text(
            text = "More Options",
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            style = MaterialTheme.typography.titleLarge
        )

        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            SheetActionRow(
                icon = Icons.Default.Timer,
                label = "Sleep timer",
                onClick = onSleepTimer
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            SheetActionRow(
                icon = Icons.Default.CameraAlt,
                label = "Snapshot",
                onClick = onSnapshot
            )
            SheetActionRow(
                icon = Icons.Default.CameraAlt,
                label = "Snapshot with subtitles",
                onClick = onSnapshotWithSubs
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onBackgroundPlaybackToggle() }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Headphones, contentDescription = null)
                Text(
                    text = "Background playback",
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(checked = isBackgroundPlaybackEnabled, onCheckedChange = { onBackgroundPlaybackToggle() })
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            SheetActionRow(
                icon = Icons.Default.Info,
                label = "Media info",
                onClick = onMediaInfo
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SheetActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            modifier = Modifier.padding(start = 16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
