package com.pixelvibe.vedioplayer.domain

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File

/**
 * Helper for file picking operations.
 * Uses the document picker for SAF compatibility.
 */
class FilePickerHelper(private val context: Context) {

    /**
     * Contract for picking a single file.
     */
    val singleFilePicker = ActivityResultContracts.OpenDocument()

    /**
     * Contract for picking a directory.
     */
    val directoryPicker = ActivityResultContracts.OpenDocumentTree()

    /**
     * Contract for picking multiple files.
     */
    val multipleFilePicker = ActivityResultContracts.OpenMultipleDocuments()

    /**
     * Create an intent for picking video files.
     */
    fun createVideoPickerIntent(): Intent {
        return Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "video/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
    }

    /**
     * Create an intent for picking subtitle files.
     */
    fun createSubtitlePickerIntent(): Intent {
        return Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                "text/*",
                "application/x-subrip",
                "text/vtt",
                "application/x-ass",
                "application/x-ssa"
            ))
            addCategory(Intent.CATEGORY_OPENABLE)
        }
    }

    /**
     * Create an intent for picking audio files.
     */
    fun createAudioPickerIntent(): Intent {
        return Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "audio/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
    }

    /**
     * Convert a Uri to a File path (best effort).
     * Returns null for content URIs that can't be resolved.
     */
    fun uriToFile(uri: Uri): File? {
        return try {
            when {
                uri.scheme == "file" -> File(uri.path!!)
                uri.scheme == "content" -> {
                    // For content URIs, we can't get a direct file path
                    // Return null and let the caller handle via ContentResolver
                    null
                }
                else -> File(uri.path!!)
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get display name from Uri.
     */
    fun getDisplayName(uri: Uri): String {
        return try {
            uri.lastPathSegment?.substringAfterLast('/') ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }
}
