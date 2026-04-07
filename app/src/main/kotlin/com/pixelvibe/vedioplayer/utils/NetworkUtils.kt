package com.pixelvibe.vedioplayer.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

/**
 * Utility functions for network-related operations.
 */
object NetworkUtils {
    /**
     * Check if the device is currently connected to a network.
     */
    fun isConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected == true
        }
    }

    /**
     * Check if connected via Wi-Fi.
     */
    fun isWifiConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo?.type == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected
        }
    }

    /**
     * Check if connected via cellular/mobile data.
     */
    fun isMobileDataConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo?.type == ConnectivityManager.TYPE_MOBILE && networkInfo.isConnected
        }
    }

    /**
     * Check if the connection is metered (may incur data charges).
     */
    fun isMeteredConnection(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            connectivityManager.isActiveNetworkMetered
        } else {
            @Suppress("DEPRECATION")
            ConnectivityManager.isNetworkTypeMobile(
                connectivityManager.activeNetworkInfo?.type ?: ConnectivityManager.TYPE_WIFI
            )
        }
    }

    /**
     * Validate a URL string.
     */
    fun isValidUrl(url: String): Boolean {
        return try {
            java.net.URL(url)
            (url.startsWith("http://") || url.startsWith("https://") ||
             url.startsWith("smb://") || url.startsWith("ftp://") ||
             url.startsWith("ftps://") || url.startsWith("webdav://") ||
             url.startsWith("webdavs://"))
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Extract the file name from a URL.
     */
    fun getFileNameFromUrl(url: String): String {
        return try {
            url.substringAfterLast('/').substringBefore('?').ifBlank { "unknown" }
        } catch (e: Exception) {
            "unknown"
        }
    }

    /**
     * Get the host from a URL.
     */
    fun getHostFromUrl(url: String): String {
        return try {
            val withoutProtocol = url.substringAfter("://").substringBefore('/')
            withoutProtocol.substringBefore(':')
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Get the port from a URL, or return the default.
     */
    fun getPortFromUrl(url: String, defaultPort: Int): Int {
        return try {
            val withoutProtocol = url.substringAfter("://")
            val hostPart = withoutProtocol.substringBefore('/')
            if (hostPart.contains(':')) {
                hostPart.substringAfter(':').toIntOrNull() ?: defaultPort
            } else {
                defaultPort
            }
        } catch (e: Exception) {
            defaultPort
        }
    }
}
