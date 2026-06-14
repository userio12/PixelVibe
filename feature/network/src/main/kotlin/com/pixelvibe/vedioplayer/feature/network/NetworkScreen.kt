package com.pixelvibe.vedioplayer.feature.network

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.MaterialTheme
import com.pixelvibe.vedioplayer.core.common.util.UiText
import com.pixelvibe.vedioplayer.core.data.network.NetworkFile
import com.pixelvibe.vedioplayer.core.data.network.NetworkResult
import com.pixelvibe.vedioplayer.core.data.network.SsdpDevice
import com.pixelvibe.vedioplayer.core.ui.component.EmptyView
import com.pixelvibe.vedioplayer.core.ui.component.ErrorView
import com.pixelvibe.vedioplayer.core.ui.component.LoadingIndicator
import com.pixelvibe.vedioplayer.core.ui.component.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel

@Composable
fun NetworkRoot(
    onVideoClick: (String) -> Unit = {},
    viewModel: NetworkViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is NetworkEvent.PlayVideo -> onVideoClick(event.uri)
        }
    }

    NetworkScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkScreen(
    state: NetworkState,
    onAction: (NetworkAction) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = state.selectedTab.ordinal,
            modifier = Modifier.fillMaxWidth()
        ) {
            NetworkTab.entries.forEach { tab ->
                Tab(
                    selected = state.selectedTab == tab,
                    onClick = { onAction(NetworkAction.OnTabSelected(tab)) },
                    text = { Text(tab.name, maxLines = 1) }
                )
            }
        }

        if (state.connectionStatus.isNotEmpty()) {
            Text(
                text = state.connectionStatus,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (state.currentSource != null) {
            BreadcrumbBar(state = state, onAction = onAction)
        }

        when (state.selectedTab) {
            NetworkTab.SMB -> SmbPanel(state = state, onAction = onAction)
            NetworkTab.FTP -> FtpPanel(state = state, onAction = onAction)
            NetworkTab.WEBDAV -> WebDavPanel(state = state, onAction = onAction)
            NetworkTab.DLNA -> DlnaPanel(state = state, onAction = onAction)
            NetworkTab.CHROMECAST -> ChromecastPanel(state = state, onAction = onAction)
        }
    }
}

@Composable
private fun BreadcrumbBar(state: NetworkState, onAction: (NetworkAction) -> Unit) {
    val result = state.result
    if (result is NetworkResult.Success && result.breadcrumbs.isNotEmpty()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            result.breadcrumbs.forEach { crumb ->
                Text(
                    text = "/ ${crumb.label}",
                    modifier = Modifier.clickable { onAction(NetworkAction.OnNavigateToBreadcrumb(crumb.path)) },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun SmbPanel(state: NetworkState, onAction: (NetworkAction) -> Unit) {
    if (state.currentSource == null) {
        SmbConnectForm(state = state, onAction = onAction)
    } else {
        FileBrowser(state = state, onAction = onAction)
    }
}

@Composable
private fun FtpPanel(state: NetworkState, onAction: (NetworkAction) -> Unit) {
    if (state.currentSource == null) {
        FtpConnectForm(state = state, onAction = onAction)
    } else {
        FileBrowser(state = state, onAction = onAction)
    }
}

@Composable
private fun WebDavPanel(state: NetworkState, onAction: (NetworkAction) -> Unit) {
    if (state.currentSource == null) {
        WebDavConnectForm(state = state, onAction = onAction)
    } else {
        FileBrowser(state = state, onAction = onAction)
    }
}

@Composable
private fun DlnaPanel(state: NetworkState, onAction: (NetworkAction) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(
            onClick = { onAction(NetworkAction.OnScanDlna) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Scan for DLNA Devices") }

        Spacer(Modifier.height(16.dp))

        if (state.discoveredDlnaDevices.isEmpty()) {
            EmptyView(message = "No DLNA devices found. Tap Scan to discover.")
        } else {
            LazyColumn {
                items(state.discoveredDlnaDevices) { device ->
                    DlnaDeviceItem(device = device)
                }
            }
        }
    }
}

@Composable
private fun ChromecastPanel(state: NetworkState, onAction: (NetworkAction) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(
            onClick = { onAction(NetworkAction.OnScanChromecast) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Scan for Chromecast") }

        Spacer(Modifier.height(16.dp))

        EmptyView(message = "Chromecast requires Google Play Services.")
    }
}

@Composable
private fun SmbConnectForm(state: NetworkState, onAction: (NetworkAction) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = state.smbHost,
            onValueChange = { onAction(NetworkAction.OnSmbHostChange(it)) },
            label = { Text("Host") },
            placeholder = { Text("192.168.1.100") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.smbShare,
            onValueChange = { onAction(NetworkAction.OnSmbShareChange(it)) },
            label = { Text("Share") },
            placeholder = { Text("ShareName") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.smbUsername,
            onValueChange = { onAction(NetworkAction.OnSmbUsernameChange(it)) },
            label = { Text("Username") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.smbPassword,
            onValueChange = { onAction(NetworkAction.OnSmbPasswordChange(it)) },
            label = { Text("Password") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { onAction(NetworkAction.OnSmbConnect) },
            modifier = Modifier.fillMaxWidth(),
            enabled = state.smbHost.isNotBlank() && !state.isConnecting
        ) {
            if (state.isConnecting) CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
            Text("Connect")
        }
    }
}

@Composable
private fun FtpConnectForm(state: NetworkState, onAction: (NetworkAction) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = state.ftpHost,
            onValueChange = { onAction(NetworkAction.OnFtpHostChange(it)) },
            label = { Text("Host") },
            placeholder = { Text("192.168.1.100") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.ftpPort,
            onValueChange = { onAction(NetworkAction.OnFtpPortChange(it)) },
            label = { Text("Port") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.ftpUsername,
            onValueChange = { onAction(NetworkAction.OnFtpUsernameChange(it)) },
            label = { Text("Username") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.ftpPassword,
            onValueChange = { onAction(NetworkAction.OnFtpPasswordChange(it)) },
            label = { Text("Password") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { onAction(NetworkAction.OnFtpConnect) },
            modifier = Modifier.fillMaxWidth(),
            enabled = state.ftpHost.isNotBlank() && !state.isConnecting
        ) {
            if (state.isConnecting) CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
            Text("Connect")
        }
    }
}

@Composable
private fun WebDavConnectForm(state: NetworkState, onAction: (NetworkAction) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = state.webdavUrl,
            onValueChange = { onAction(NetworkAction.OnWebDavUrlChange(it)) },
            label = { Text("WebDAV URL") },
            placeholder = { Text("https://example.com/dav") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.webdavUsername,
            onValueChange = { onAction(NetworkAction.OnWebDavUsernameChange(it)) },
            label = { Text("Username") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.webdavPassword,
            onValueChange = { onAction(NetworkAction.OnWebDavPasswordChange(it)) },
            label = { Text("Password") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { onAction(NetworkAction.OnWebDavConnect) },
            modifier = Modifier.fillMaxWidth(),
            enabled = state.webdavUrl.isNotBlank() && !state.isConnecting
        ) {
            if (state.isConnecting) CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
            Text("Connect")
        }
    }
}

@Composable
private fun FileBrowser(state: NetworkState, onAction: (NetworkAction) -> Unit) {
    when (val result = state.result) {
        is NetworkResult.Loading -> LoadingIndicator(message = "Loading files...")
        is NetworkResult.Error -> ErrorView(message = UiText.DynamicString(result.message), onRetry = { onAction(NetworkAction.OnRefresh) })
        is NetworkResult.Success -> {
            if (result.files.isEmpty()) {
                EmptyView(message = "No files found")
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(result.files) { file ->
                        NetworkFileItem(file = file, onClick = {
                            if (file.isDirectory) onAction(NetworkAction.OnNavigateToFolder(file))
                            else onAction(NetworkAction.OnVideoClick(file))
                        })
                    }
                }
            }
        }
    }
}

@Composable
private fun NetworkFileItem(file: NetworkFile, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (file.isDirectory) Icons.Default.Folder else Icons.Default.Videocam,
            contentDescription = if (file.isDirectory) "Folder" else "Video",
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!file.isDirectory && file.size > 0) {
                Text(
                    text = formatFileSize(file.size),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
    HorizontalDivider()
}

@Composable
private fun DlnaDeviceItem(device: SsdpDevice) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = device.usn.take(50), maxLines = 1)
            Text(text = device.location, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
        }
    }
    HorizontalDivider()
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes >= 1_073_741_824 -> "%.1f GB".format(bytes / 1_073_741_824.0)
        bytes >= 1_048_576 -> "%.1f MB".format(bytes / 1_048_576.0)
        bytes >= 1024 -> "%.1f KB".format(bytes / 1024.0)
        else -> "$bytes B"
    }
}
