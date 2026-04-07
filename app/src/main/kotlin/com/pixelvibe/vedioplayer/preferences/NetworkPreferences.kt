package com.pixelvibe.vedioplayer.preferences

/**
 * Typed preference class for network-related settings.
 */
class NetworkPreferences(private val store: PreferenceStore) {
    val proxyPort = store.intPreference("network_proxy_port", 8088)
    val showNetworkThumbnails = store.booleanPreference("appearance_network_thumbnails", false)
    val smbDefaultPort = store.intPreference("network_smb_port", 445)
    val ftpDefaultPort = store.intPreference("network_ftp_port", 21)
    val connectionTimeout = store.intPreference("network_timeout", 30)
    val lastUsedProtocol = store.stringPreference("network_last_protocol", "SMB")
}
