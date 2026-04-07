package com.pixelvibe.vedioplayer.utils

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility functions for file system operations.
 */
object FileUtils {
    /**
     * Get human-readable file size.
     */
    fun getDisplaySize(file: File): String {
        return if (file.isDirectory) {
            "${file.listFiles()?.size ?: 0} items"
        } else {
            MediaUtils.formatFileSize(file.length())
        }
    }

    /**
     * Get file extension (lowercase).
     */
    fun getExtension(fileName: String): String {
        val dotIndex = fileName.lastIndexOf('.')
        return if (dotIndex >= 0 && dotIndex < fileName.length - 1) {
            fileName.substring(dotIndex + 1).lowercase()
        } else {
            ""
        }
    }

    /**
     * Get file name without extension.
     */
    fun getNameWithoutExtension(fileName: String): String {
        val dotIndex = fileName.lastIndexOf('.')
        return if (dotIndex > 0) fileName.substring(0, dotIndex) else fileName
    }

    /**
     * Check if file is hidden (starts with dot).
     */
    fun isHidden(fileName: String): Boolean = fileName.startsWith(".")

    /**
     * Get last modified date formatted.
     */
    fun getLastModifiedFormatted(file: File): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(file.lastModified()))
    }

    /**
     * Delete a file or directory recursively.
     */
    fun deleteRecursively(file: File): Boolean {
        return if (file.isDirectory) {
            file.listFiles()?.all { deleteRecursively(it) } != false && file.delete()
        } else {
            file.delete()
        }
    }

    /**
     * Copy file from source to destination.
     */
    fun copyFile(source: File, destination: File): Boolean {
        return try {
            destination.parentFile?.mkdirs()
            source.inputStream().use { input ->
                destination.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Sanitize file name for display.
     */
    fun sanitizeDisplayName(fileName: String): String {
        return getNameWithoutExtension(fileName)
            .replace('_', ' ')
            .replace('.', ' ')
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
