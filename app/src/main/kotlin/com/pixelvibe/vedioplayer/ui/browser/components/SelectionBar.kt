package com.pixelvibe.vedioplayer.ui.browser.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SelectionBar(
    selectedCount: Int,
    onDelete: () -> Unit = {},
    onAddToPlaylist: () -> Unit = {},
    onPlayAll: () -> Unit = {},
    onClearSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$selectedCount selected",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(start = 8.dp)
        )

        Row(horizontalArrangement = Arrangement.End) {
            IconButton(onClick = onPlayAll) {
                Icon(Icons.Default.PlayArrow, "Play all", tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            IconButton(onClick = onAddToPlaylist) {
                Icon(Icons.Default.Add, "Add to playlist", tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            IconButton(onClick = onClearSelection) {
                Icon(Icons.Default.Close, "Clear selection", tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}
