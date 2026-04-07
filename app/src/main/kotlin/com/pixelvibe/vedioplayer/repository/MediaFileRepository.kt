package com.pixelvibe.vedioplayer.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

/**
 * Represents a media file for the browser.
 */
data class MediaFile(
    val path: String,
    val name: String,
    val isFolder: Boolean,
    val duration: Long = 0L,
    val fileSize: Long = 0L,
    val width: Int = 0,
    val height: Int = 0,
    val dateModified: Long = 0L,
    val thumbnailPath: String? = null
)

/**
 * Repository for browsing and managing media files.
 */
class MediaFileRepository {
    /**
     * List files in a directory.
     */
    fun listFiles(directory: File, includeHidden: Boolean = false): Flow<List<MediaFile>> = flow {
        val files = directory.listFiles()?.toList() ?: emptyList()
        val mediaFiles = files
            .filter { file -> includeHidden || !file.name.startsWith(".") }
            .map { file ->
                MediaFile(
                    path = file.absolutePath,
                    name = file.name,
                    isFolder = file.isDirectory,
                    fileSize = if (file.isFile) file.length() else 0L,
                    dateModified = file.lastModified()
                )
            }
        emit(mediaFiles)
    }

    /**
     * Search for media files matching a query.
     */
    fun search(directory: File, query: String): Flow<List<MediaFile>> = flow {
        val results = mutableListOf<MediaFile>()

        fun scanRecursive(dir: File) {
            val files = dir.listFiles() ?: return
            files.forEach { file ->
                if (file.name.startsWith(".") && !file.name.startsWith(".")) return@forEach
                if (file.isDirectory) {
                    scanRecursive(file)
                } else if (file.name.contains(query, ignoreCase = true)) {
                    results.add(
                        MediaFile(
                            path = file.absolutePath,
                            name = file.name,
                            isFolder = false,
                            fileSize = file.length()
                        )
                    )
                }
            }
        }

        scanRecursive(directory)
        emit(results)
    }
}
