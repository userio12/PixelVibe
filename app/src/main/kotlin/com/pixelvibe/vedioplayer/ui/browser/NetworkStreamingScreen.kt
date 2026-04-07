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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pixelvibe.vedioplayer.database.entities.NetworkConnectionEntity

data class NetworkConnectionUi(
    val id: Long,
    val name: String,
    val protocol: String,
    val host: String,
    val isAnonymous: Boolean,
    val autoConnect: Boolean,
    val useHttps: Boolean
)

@Composable
fun NetworkStreamingScreen(
    connections: List<NetworkConnectionUi>,
    onAddConnection: (NetworkConnectionEntity) -> Unit,
    onDeleteConnection: (Long) -> Unit,
    onConnectionClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        if (connections.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Cloud,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "No network connections",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Add SMB, FTP, or WebDAV connections",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
            ) {
                items(connections, key = { it.id }) { conn ->
                    NetworkConnectionCard(
                        connection = conn,
                        onClick = { onConnectionClick(conn.id) },
                        onDelete = { onDeleteConnection(conn.id) }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, "Add connection")
        }
    }

    if (showAddDialog) {
        AddConnectionDialog(
            onConfirm = { entity ->
                onAddConnection(entity)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun NetworkConnectionCard(
    connection: NetworkConnectionUi,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (connection.protocol.uppercase()) {
                    "SMB" -> Icons.Default.Computer
                    "FTP" -> Icons.Default.Cloud
                    "WEBDAV" -> Icons.Default.Folder
                    else -> Icons.Default.Cloud
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(32.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = connection.name,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "${connection.protocol}://${connection.host}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete connection")
            }
        }
    }
}

@Composable
private fun AddConnectionDialog(
    onConfirm: (NetworkConnectionEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var protocol by remember { mutableStateOf("SMB") }
    var host by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isAnonymous by remember { mutableStateOf(false) }
    var autoConnect by remember { mutableStateOf(false) }
    var useHttps by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Network Connection") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                OutlinedTextField(value = host, onValueChange = { host = it }, label = { Text("Host") })
                OutlinedTextField(value = port, onValueChange = { port = it }, label = { Text("Port (optional)") })

                // Protocol selector
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Protocol:")
                    listOf("SMB", "FTP", "WebDAV").forEach { p ->
                        TextButton(onClick = { protocol = p }) {
                            Text(p, color = if (p == protocol) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                if (!isAnonymous) {
                    OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") })
                    OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") })
                }

                // Toggles
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Anonymous", Modifier.weight(1f))
                    Switch(checked = isAnonymous, onCheckedChange = { isAnonymous = it })
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Auto-connect", Modifier.weight(1f))
                    Switch(checked = autoConnect, onCheckedChange = { autoConnect = it })
                }
                if (protocol == "WebDAV") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Use HTTPS", Modifier.weight(1f))
                        Switch(checked = useHttps, onCheckedChange = { useHttps = it })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && host.isNotBlank()) {
                        onConfirm(
                            NetworkConnectionEntity(
                                name = name,
                                protocol = protocol,
                                host = host,
                                port = port.toIntOrNull() ?: 0,
                                username = username,
                                password = password,
                                isAnonymous = isAnonymous,
                                autoConnect = autoConnect,
                                useHttps = useHttps
                            )
                        )
                    }
                },
                enabled = name.isNotBlank() && host.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
