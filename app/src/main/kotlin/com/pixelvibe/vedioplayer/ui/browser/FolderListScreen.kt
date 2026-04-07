package com.pixelvibe.vedioplayer.ui.browser

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pixelvibe.vedioplayer.ui.browser.components.FolderCard
import com.pixelvibe.vedioplayer.ui.browser.components.SelectionBar
import com.pixelvibe.vedioplayer.ui.browser.components.VideoCard
import com.pixelvibe.vedioplayer.utils.MediaUtils
import com.pixelvibe.vedioplayer.utils.SortOrder
import com.pixelvibe.vedioplayer.utils.SortType
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderListScreen(
    rootDirectory: File,
    onVideoClick: (File) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentDirectory by remember { mutableStateOf(rootDirectory) }
    var viewMode by remember { mutableStateOf<ViewMode>(ViewMode.TREE) }
    var sortBy by remember { mutableStateOf(SortType.NAME) }
    var sortOrder by remember { mutableStateOf(SortOrder.ASCENDING) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var selectedFiles by remember { mutableStateOf<Set<File>>(emptySet()) }
    val listState = rememberLazyListState()

    // Get directory contents
    val files = remember(currentDirectory, sortBy, sortOrder) {
        currentDirectory.listFiles()?.toList()?.sortedWith(
            compareBy({ !it.isDirectory }, { it.name.lowercase() })
        ) ?: emptyList()
    }

    val folders = files.filter { it.isDirectory }
    val videos = files.filter { f -> f.isFile && MediaUtils.isVideoFile(f.name) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Breadcrumb navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Parent directory button
                if (currentDirectory != rootDirectory) {
                    IconButton(onClick = {
                        currentDirectory.parentFile?.let { currentDirectory = it }
                        selectedFiles = emptySet()
                    }) {
                        Icon(Icons.Default.ArrowUpward, "Go to parent")
                    }
                }

                // Current path
                Text(
                    text = currentDirectory.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                )

                // Action buttons
                IconButton(onClick = { showSortDialog = true }) {
                    Icon(Icons.Default.Sort, "Sort")
                }
                IconButton(onClick = {
                    viewMode = if (viewMode == ViewMode.TREE) ViewMode.FLAT else ViewMode.TREE
                }) {
                    Icon(
                        if (viewMode == ViewMode.TREE) Icons.Default.ViewList else Icons.Default.ViewModule,
                        "Toggle view"
                    )
                }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, "More options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Select multiple") },
                            onClick = { /* Enable selection mode */ }
                        )
                    }
                }
            }

            // Selection bar
            if (selectedFiles.isNotEmpty()) {
                SelectionBar(
                    selectedCount = selectedFiles.size,
                    onDelete = { /* Delete selected */ },
                    onAddToPlaylist = { /* Add to playlist */ },
                    onPlayAll = { /* Play all */ },
                    onClearSelection = { selectedFiles = emptySet() }
                )
            }

            // File list
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                // Folders
                items(folders, key = { it.absolutePath }) { folder ->
                    FolderCard(
                        folder = folder,
                        onClick = {
                            currentDirectory = folder
                            selectedFiles = emptySet()
                        }
                    )
                }

                // Videos
                items(videos, key = { it.absolutePath }) { video ->
                    VideoCard(
                        file = video,
                        title = video.nameWithoutExtension,
                        duration = 0, // Would need metadata scan
                        fileSize = video.length(),
                        isNew = false, // Would need watched tracking
                        onClick = { onVideoClick(video) }
                    )
                }

                if (files.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No media files found",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // Sort dialog
    if (showSortDialog) {
        SortDialog(
            currentSortBy = sortBy,
            currentOrder = sortOrder,
            onSortChange = { newSortBy, newOrder ->
                sortBy = newSortBy
                sortOrder = newOrder
                showSortDialog = false
            },
            onDismiss = { showSortDialog = false }
        )
    }
}

enum class ViewMode { TREE, FLAT }

@Composable
private fun SortDialog(
    currentSortBy: SortType,
    currentOrder: SortOrder,
    onSortChange: (SortType, SortOrder) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedBy by remember { mutableStateOf(currentSortBy) }
    var selectedOrder by remember { mutableStateOf(currentOrder) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sort by") },
        text = {
            Column {
                Text("Sort Type", style = MaterialTheme.typography.labelLarge)
                SortType.entries.forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedBy = type }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedBy == type, onClick = { selectedBy = type })
                        Text(type.name.replaceFirstChar { it.uppercase() }, Modifier.padding(start = 8.dp))
                    }
                }

                Text("Order", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
                SortOrder.entries.forEach { order ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedOrder = order }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedOrder == order, onClick = { selectedOrder = order })
                        Text(order.name.replaceFirstChar { it.uppercase() }, Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSortChange(selectedBy, selectedOrder) }) { Text("Apply") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
