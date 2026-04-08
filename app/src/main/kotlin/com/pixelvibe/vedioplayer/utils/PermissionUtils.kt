package com.pixelvibe.vedioplayer.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Utility functions for handling runtime permissions.
 */
object PermissionUtils {
    /**
     * Get the list of storage permissions needed based on API level.
     * Android 13+ uses READ_MEDIA_VIDEO, older versions use READ_EXTERNAL_STORAGE.
     */
    fun getStoragePermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    /**
     * Get the list of notification permissions needed.
     * Required on Android 13+ for posting notifications.
     */
    fun getNotificationPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            emptyArray()
        }
    }

    /**
     * Get all permissions needed for the app.
     */
    fun getAllPermissions(): Array<String> {
        val perms = mutableListOf<String>()
        perms.addAll(getStoragePermissions().toList())
        perms.addAll(getNotificationPermissions().toList())
        return perms.toTypedArray()
    }

    /**
     * Check if storage permission is granted.
     */
    fun hasStoragePermission(context: Context): Boolean {
        return getStoragePermissions().all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Check if notification permission is granted.
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Not required on older versions
        }
    }

    /**
     * Check if all required permissions are granted.
     */
    fun hasAllPermissions(context: Context): Boolean {
        return hasStoragePermission(context) && hasNotificationPermission(context)
    }

    /**
     * Get a user-friendly description for a permission.
     */
    fun getPermissionDescription(permission: String): String {
        return when (permission) {
            Manifest.permission.READ_MEDIA_VIDEO -> "Access your video files for browsing and playback"
            Manifest.permission.READ_EXTERNAL_STORAGE -> "Access your files for browsing and playback"
            Manifest.permission.POST_NOTIFICATIONS -> "Show playback notifications"
            Manifest.permission.FOREGROUND_SERVICE -> "Play videos in the background"
            Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK -> "Continue playback as a foreground service"
            else -> permission
        }
    }
}
