package com.pixelvibe.vedioplayer.feature.network

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelvibe.vedioplayer.core.data.network.ChromecastClient
import com.pixelvibe.vedioplayer.core.data.network.FtpClient
import com.pixelvibe.vedioplayer.core.data.network.NetworkFile
import com.pixelvibe.vedioplayer.core.data.network.NetworkResult
import com.pixelvibe.vedioplayer.core.data.network.NetworkSource
import com.pixelvibe.vedioplayer.core.data.network.SmbClient
import com.pixelvibe.vedioplayer.core.data.network.SsdpDevice
import com.pixelvibe.vedioplayer.core.data.network.SsdpDiscovery
import com.pixelvibe.vedioplayer.core.data.network.Breadcrumb
import com.pixelvibe.vedioplayer.core.data.network.WebDavClient
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class NetworkTab { SMB, FTP, WEBDAV, DLNA, CHROMECAST }

data class NetworkState(
    val selectedTab: NetworkTab = NetworkTab.SMB,
    val currentSource: NetworkSource? = null,
    val result: NetworkResult = NetworkResult.Loading,
    val connectionStatus: String = "",
    val discoveredDlnaDevices: List<SsdpDevice> = emptyList(),
    val discoveredCastDevices: List<String> = emptyList(),
    val smbUsername: String = "guest",
    val smbPassword: String = "",
    val smbHost: String = "",
    val smbShare: String = "",
    val ftpHost: String = "",
    val ftpPort: String = "21",
    val ftpUsername: String = "anonymous",
    val ftpPassword: String = "",
    val webdavUrl: String = "",
    val webdavUsername: String = "",
    val webdavPassword: String = "",
    val isConnecting: Boolean = false
)

sealed interface NetworkAction {
    data class OnTabSelected(val tab: NetworkTab) : NetworkAction
    data class OnNavigateToFolder(val folder: NetworkFile) : NetworkAction
    data class OnNavigateToBreadcrumb(val path: String) : NetworkAction
    data object OnRefresh : NetworkAction
    data class OnSmbHostChange(val host: String) : NetworkAction
    data class OnSmbShareChange(val share: String) : NetworkAction
    data class OnSmbUsernameChange(val username: String) : NetworkAction
    data class OnSmbPasswordChange(val password: String) : NetworkAction
    data object OnSmbConnect : NetworkAction
    data class OnFtpHostChange(val host: String) : NetworkAction
    data class OnFtpPortChange(val port: String) : NetworkAction
    data class OnFtpUsernameChange(val username: String) : NetworkAction
    data class OnFtpPasswordChange(val password: String) : NetworkAction
    data object OnFtpConnect : NetworkAction
    data class OnWebDavUrlChange(val url: String) : NetworkAction
    data class OnWebDavUsernameChange(val username: String) : NetworkAction
    data class OnWebDavPasswordChange(val password: String) : NetworkAction
    data object OnWebDavConnect : NetworkAction
    data object OnScanDlna : NetworkAction
    data object OnScanChromecast : NetworkAction
    data class OnVideoClick(val file: NetworkFile) : NetworkAction
    data object OnBack : NetworkAction
}

sealed interface NetworkEvent {
    data class PlayVideo(val uri: String) : NetworkEvent
}

class NetworkViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val smbClient: SmbClient,
    private val ftpClient: FtpClient,
    private val webDavClient: WebDavClient,
    private val ssdpDiscovery: SsdpDiscovery,
    private val chromecastClient: ChromecastClient
) : ViewModel() {

    private val _state = MutableStateFlow(
        NetworkState(
            selectedTab = savedStateHandle.get<String>("selectedTab")?.let { name ->
                try { NetworkTab.valueOf(name) } catch (_: Exception) { NetworkTab.SMB }
            } ?: NetworkTab.SMB
        )
    )
    val state: StateFlow<NetworkState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<NetworkEvent>()
    val events = _events.asSharedFlow()

    init {
        persistState()
        refresh()
    }

    private fun persistState() {
        viewModelScope.launch {
            _state.collect { s ->
                savedStateHandle["selectedTab"] = s.selectedTab.name
            }
        }
    }

    fun onAction(action: NetworkAction) {
        when (action) {
            is NetworkAction.OnTabSelected -> onTabSelected(action.tab)
            is NetworkAction.OnNavigateToFolder -> navigateTo(action.folder)
            is NetworkAction.OnNavigateToBreadcrumb -> navigateToPath(action.path)
            NetworkAction.OnRefresh -> refresh()
            is NetworkAction.OnSmbHostChange -> _state.update { it.copy(smbHost = action.host) }
            is NetworkAction.OnSmbShareChange -> _state.update { it.copy(smbShare = action.share) }
            is NetworkAction.OnSmbUsernameChange -> _state.update { it.copy(smbUsername = action.username) }
            is NetworkAction.OnSmbPasswordChange -> _state.update { it.copy(smbPassword = action.password) }
            NetworkAction.OnSmbConnect -> {
                val s = _state.value; connectSmb(s.smbHost, s.smbShare, s.smbUsername, s.smbPassword)
            }
            is NetworkAction.OnFtpHostChange -> _state.update { it.copy(ftpHost = action.host) }
            is NetworkAction.OnFtpPortChange -> _state.update { it.copy(ftpPort = action.port) }
            is NetworkAction.OnFtpUsernameChange -> _state.update { it.copy(ftpUsername = action.username) }
            is NetworkAction.OnFtpPasswordChange -> _state.update { it.copy(ftpPassword = action.password) }
            NetworkAction.OnFtpConnect -> {
                val s = _state.value; connectFtp(s.ftpHost, s.ftpPort.toIntOrNull() ?: 21, s.ftpUsername, s.ftpPassword)
            }
            is NetworkAction.OnWebDavUrlChange -> _state.update { it.copy(webdavUrl = action.url) }
            is NetworkAction.OnWebDavUsernameChange -> _state.update { it.copy(webdavUsername = action.username) }
            is NetworkAction.OnWebDavPasswordChange -> _state.update { it.copy(webdavPassword = action.password) }
            NetworkAction.OnWebDavConnect -> {
                val s = _state.value; connectWebDav(s.webdavUrl, s.webdavUsername, s.webdavPassword)
            }
            NetworkAction.OnScanDlna -> scanDlna()
            NetworkAction.OnScanChromecast -> scanChromecast()
            is NetworkAction.OnVideoClick -> playVideo(action.file)
            NetworkAction.OnBack -> onBack()
        }
    }

    private fun onTabSelected(tab: NetworkTab) {
        _state.update { it.copy(selectedTab = tab, result = NetworkResult.Loading) }
        refresh()
    }

    private fun refresh() {
        _state.update { it.copy(result = NetworkResult.Loading) }
        when (_state.value.selectedTab) {
            NetworkTab.SMB -> {
                _state.update { it.copy(
                    result = NetworkResult.Success(emptyList(), emptyList()),
                    connectionStatus = "Enter server details below"
                )}
            }
            NetworkTab.FTP -> {
                _state.update { it.copy(
                    result = NetworkResult.Success(emptyList(), emptyList()),
                    connectionStatus = "Enter server details below"
                )}
            }
            NetworkTab.WEBDAV -> {
                _state.update { it.copy(
                    result = NetworkResult.Success(emptyList(), emptyList()),
                    connectionStatus = "Enter server URL below"
                )}
            }
            NetworkTab.DLNA -> { scanDlna() }
            NetworkTab.CHROMECAST -> { scanChromecast() }
        }
    }

    private fun connectSmb(host: String, share: String, username: String, password: String) {
        _state.update { it.copy(isConnecting = true) }
        viewModelScope.launch {
            val source = NetworkSource.Smb(host, host, share, "", username, password)
            val authenticated = smbClient.authenticate(source)
            if (authenticated) {
                _state.update { it.copy(
                    currentSource = source,
                    isConnecting = false,
                    connectionStatus = "Connected to $host/$share"
                )}
                browse(source)
            } else {
                _state.update { it.copy(
                    result = NetworkResult.Error("Failed to connect to $host/$share"),
                    isConnecting = false,
                    connectionStatus = "Connection failed"
                )}
            }
        }
    }

    private fun connectFtp(host: String, port: Int, username: String, password: String) {
        _state.update { it.copy(isConnecting = true) }
        viewModelScope.launch {
            val source = NetworkSource.Ftp(host, host, port, "", username, password)
            val authenticated = ftpClient.authenticate(source)
            if (authenticated) {
                _state.update { it.copy(
                    currentSource = source,
                    isConnecting = false,
                    connectionStatus = "Connected to $host:$port"
                )}
                browse(source)
            } else {
                _state.update { it.copy(
                    result = NetworkResult.Error("Failed to connect to $host:$port"),
                    isConnecting = false,
                    connectionStatus = "Connection failed"
                )}
            }
        }
    }

    private fun connectWebDav(url: String, username: String, password: String) {
        _state.update { it.copy(isConnecting = true) }
        viewModelScope.launch {
            val source = NetworkSource.WebDav(url, url, url, "", username, password)
            val authenticated = webDavClient.authenticate(source)
            if (authenticated) {
                _state.update { it.copy(
                    currentSource = source,
                    isConnecting = false,
                    connectionStatus = "Connected to $url"
                )}
                browse(source)
            } else {
                _state.update { it.copy(
                    result = NetworkResult.Error("Failed to connect to $url"),
                    isConnecting = false,
                    connectionStatus = "Connection failed"
                )}
            }
        }
    }

    private fun browse(source: NetworkSource) {
        viewModelScope.launch {
            _state.update { it.copy(result = NetworkResult.Loading) }
            try {
                val files = when (source) {
                    is NetworkSource.Smb -> smbClient.listFiles(source)
                    is NetworkSource.Ftp -> ftpClient.listFiles(source)
                    is NetworkSource.WebDav -> webDavClient.listFiles(source)
                    is NetworkSource.Dlna -> emptyList()
                }
                val crumbs = buildBreadcrumbs(source)
                _state.update { it.copy(result = NetworkResult.Success(files, crumbs)) }
            } catch (e: Exception) {
                _state.update { it.copy(result = NetworkResult.Error(e.message ?: "Unknown error")) }
            }
        }
    }

    private fun navigateTo(folder: NetworkFile) {
        val source = _state.value.currentSource ?: return
        val updatedSource = when (source) {
            is NetworkSource.Smb -> source.copy(path = folder.path)
            is NetworkSource.Ftp -> source.copy(path = folder.path)
            is NetworkSource.WebDav -> source.copy(path = folder.path)
            is NetworkSource.Dlna -> source
        }
        _state.update { it.copy(currentSource = updatedSource) }
        browse(updatedSource)
    }

    private fun navigateToPath(path: String) {
        val source = _state.value.currentSource ?: return
        val updatedSource = when (source) {
            is NetworkSource.Smb -> source.copy(path = path.removePrefix("smb://${source.host}/${source.share}/"))
            is NetworkSource.Ftp -> source.copy(path = path)
            is NetworkSource.WebDav -> source.copy(path = path)
            is NetworkSource.Dlna -> source
        }
        _state.update { it.copy(currentSource = updatedSource) }
        browse(updatedSource)
    }

    private fun scanDlna() {
        viewModelScope.launch {
            _state.update { it.copy(result = NetworkResult.Loading, connectionStatus = "Scanning for DLNA devices...") }
            val devices = mutableListOf<SsdpDevice>()
            ssdpDiscovery.discover().collect { device ->
                devices.add(device)
                _state.update { it.copy(
                    discoveredDlnaDevices = devices.toList(),
                    connectionStatus = "Found ${devices.size} device(s)"
                )}
            }
            _state.update { it.copy(
                result = if (devices.isEmpty()) NetworkResult.Error("No DLNA devices found")
                else NetworkResult.Success(emptyList(), emptyList())
            )}
        }
    }

    private fun scanChromecast() {
        _state.update { it.copy(result = NetworkResult.Loading, connectionStatus = "Checking Chromecast availability...") }
        chromecastClient.startScan()
        val msg = if (chromecastClient.isAvailable) "No Chromecast devices found"
        else "Chromecast requires Google Play Services Cast SDK"
        _state.update { it.copy(
            result = NetworkResult.Success(emptyList(), emptyList()),
            connectionStatus = msg,
            discoveredCastDevices = emptyList()
        )}
    }

    private fun playVideo(file: NetworkFile) {
        viewModelScope.launch {
            _events.emit(NetworkEvent.PlayVideo(file.path))
        }
    }

    private fun onBack() {
        val source = _state.value.currentSource ?: return
        val currentPath = when (source) {
            is NetworkSource.Smb -> source.path
            is NetworkSource.Ftp -> source.path
            is NetworkSource.WebDav -> source.path
            is NetworkSource.Dlna -> ""
        }
        if (currentPath.isEmpty() || currentPath == "/") {
            _state.update { it.copy(currentSource = null, result = NetworkResult.Loading) }
            refresh()
        } else {
            val parent = currentPath.substringBeforeLast("/", "").trim('/')
            navigateToPath(parent)
        }
    }

    private fun buildBreadcrumbs(source: NetworkSource): List<Breadcrumb> {
        val path = when (source) {
            is NetworkSource.Smb -> source.path
            is NetworkSource.Ftp -> source.path
            is NetworkSource.WebDav -> source.path
            is NetworkSource.Dlna -> ""
        }
        val parts = path.split("/").filter { it.isNotBlank() }
        return parts.mapIndexed { i, part ->
            val crumbPath = parts.take(i + 1).joinToString("/")
            Breadcrumb(part, crumbPath)
        }
    }

    override fun onCleared() {
        super.onCleared()
        smbClient.release()
        ftpClient.release()
        webDavClient.release()
        chromecastClient.release()
    }
}
