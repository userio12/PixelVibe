package com.pixelvibe.vedioplayer.ui.browser

import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.io.File

/**
 * Direct filesystem browser with breadcrumb navigation.
 * Supports both folder browsing and file picking modes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileSystemBrowserScreen(
    rootDirectory: File = Environment.getExternalStorageDirectory(),
    mode: BrowserMode = BROWSE,
    onFileSelected: (File) -> Unit = {},
    onFolderSelected: (File) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentDirectory by remember { mutableStateOf(rootDirectory) }
    val files = remember(currentDirectory) {
        (currentDirectory.listFiles()?.toList() ?: emptyList())
            .sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
    }
    val folders = files.filter { it.isDirectory && !it.name.startsWith(".") }
    val regularFiles = files.filter { it.isFile }

    // Folder picker launcher
    val folderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let { onFolderSelected(File(it.path ?: "")) }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = currentDirectory.name.ifBlank { "/" },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    if (currentDirectory != rootDirectory) {
                        IconButton(onClick = {
                            currentDirectory.parentFile?.let { currentDirectory = it }
                        }) {
                            Icon(Icons.Default.ArrowBackIosNew, "Go up")
                        }
                    }
                },
                actions = {
                    if (mode == PICK_FOLDER) {
                        TextButton(onClick = { folderPicker.launch(null) }) {
                            Text("Pick")
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Breadcrumb path
            item {
                BreadcrumbPath(
                    root = rootDirectory,
                    current = currentDirectory,
                    onNavigate = { dir -> currentDirectory = dir },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Folders
            items(folders, key = { it.absolutePath }) { folder ->
                FileSystemEntryRow(
                    file = folder,
                    isFolder = true,
                    onClick = { currentDirectory = folder }
                )
            }

            // Files
            items(regularFiles, key = { it.absolutePath }) { file ->
                FileSystemEntryRow(
                    file = file,
                    isFolder = false,
                    onClick = { onFileSelected(file) }
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
                            text = "Empty folder",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

enum class BrowserMode { BROWSE, PICK_FOLDER }
val BROWSE = BrowserMode.BROWSE
val PICK_FOLDER = BrowserMode.PICK_FOLDER

@Composable
private fun BreadcrumbPath(
    root: File,
    current: File,
    onNavigate: (File) -> Unit,
    modifier: Modifier = Modifier
) {
    val pathParts = mutableListOf<File>()
    var file: File? = current
    while (file != null) {
        pathParts.add(0, file)
        file = if (file == root) null else file.parentFile
    }

    Row(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TextButton(onClick = { onNavigate(root) }) {
            Text("/", style = MaterialTheme.typography.labelMedium)
        }
        pathParts.drop(1).forEach { part ->
            TextButton(onClick = { onNavigate(part) }) {
                Text(part.name, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun FileSystemEntryRow(
    file: File,
    isFolder: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isFolder) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                contentDescription = null,
                tint = if (isFolder) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 8.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!isFolder) {
                    Text(
                        text = formatFileSize(file.length()),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
        bytes < 1024L * 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
        else -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
    }
}
