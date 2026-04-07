package com.pixelvibe.vedioplayer.utils

import android.os.Environment
import android.os.StatFs
import java.io.File

/**
 * Utility functions for storage operations.
 */
object StorageUtils {
    /**
     * Get common video directories to scan.
     */
    fun getDefaultVideoDirectories(): List<File> {
        val directories = mutableListOf<File>()
        val externalStorage = Environment.getExternalStorageDirectory()

        // Standard directories
        listOf(
            File(externalStorage, "DCIM"),
            File(externalStorage, "Movies"),
            File(externalStorage, "Download"),
            File(externalStorage, "Videos"),
        ).forEach { dir ->
            if (dir.exists() && dir.isDirectory) {
                directories.add(dir)
            }
        }

        // SD card
        val sdCards = System.getenv("SECONDARY_STORAGE")?.split(":") ?: emptyList()
        sdCards.forEach { path ->
            val sdCard = File(path)
            if (sdCard.exists() && sdCard.isDirectory) {
                directories.add(sdCard)
            }
        }

        return directories
    }

    /**
     * Get available storage space in bytes.
     */
    fun getAvailableSpace(path: String): Long {
        return try {
            val stat = StatFs(path)
            stat.availableBlocksLong * stat.blockSizeLong
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Get total storage space in bytes.
     */
    fun getTotalSpace(path: String): Long {
        return try {
            val stat = StatFs(path)
            stat.blockCountLong * stat.blockSizeLong
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Check if path is on external storage.
     */
    fun isOnExternalStorage(path: String): Boolean {
        val externalStorage = Environment.getExternalStorageDirectory().absolutePath
        return path.startsWith(externalStorage)
    }

    /**
     * Get relative path from base directory.
     */
    fun getRelativePath(baseDir: File, file: File): String {
        return try {
            file.absolutePath.removePrefix(baseDir.absolutePath).trimStart('/')
        } catch (e: Exception) {
            file.name
        }
    }
}
