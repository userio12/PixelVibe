package com.pixelvibe.vedioplayer.core.data.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CastDevice(
    val name: String,
    val id: String,
    val isConnected: Boolean = false
)

class ChromecastClient {

    private val _discoveredDevices = MutableStateFlow<List<CastDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<CastDevice>> = _discoveredDevices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    fun startScan() {
        _discoveredDevices.value = emptyList()
        _isScanning.value = false
    }

    fun stopScan() {
        _isScanning.value = false
    }

    fun connect(device: CastDevice): Boolean = false

    fun disconnect() {}

    fun cast(mediaUrl: String) {}

    fun release() {}

    val isAvailable: Boolean get() = false
}
