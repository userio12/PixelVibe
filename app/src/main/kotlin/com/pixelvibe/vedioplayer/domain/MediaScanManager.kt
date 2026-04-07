package com.pixelvibe.vedioplayer.domain

import com.pixelvibe.vedioplayer.utils.FileUtils
import com.pixelvibe.vedioplayer.utils.MediaUtils
import com.pixelvibe.vedioplayer.utils.StorageUtils
import com.pixelvibe.vedioplayer.utils.SortUtils
import com.pixelvibe.vedioplayer.utils.SortType
import com.pixelvibe.vedioplayer.utils.SortOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Represents a media file entry in the browser.
 */
sealed class MediaEntry {
    data class Folder(val file: File) : MediaEntry()
    data class Video(
        val file: File,
        val title: String,
        val duration: Long = 0L,
        val fileSize: Long = 0L,
        val width: Int = 0,
        val height: Int = 0,
        val isNew: Boolean = false,
        val isWatched: Boolean = false
    ) : MediaEntry()
}

/**
 * Scans and manages media files in the file browser.
 */
class MediaScanManager {
    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: Flow<ScanState> = _scanState.asStateFlow()

    sealed class ScanState {
        data object Idle : ScanState()
        data class Scanning(val progress: Int) : ScanState()
        data class Complete(val files: List<MediaEntry>) : ScanState()
        data class Error(val message: String) : ScanState()
    }

    /**
     * Scan a directory for media files.
     */
    suspend fun scanDirectory(
        directory: File,
        includeHidden: Boolean = false,
        sortBy: SortType = SortType.NAME,
        order: SortOrder = SortOrder.ASCENDING,
        unplayedDaysThreshold: Int = 7,
        watchedPaths: Set<String> = emptySet()
    ): List<MediaEntry> = withContext(Dispatchers.IO) {
        try {
            _scanState.value = ScanState.Scanning(0)

            val files = directory.listFiles()?.toList() ?: emptyList()
            val filtered = files.filter { file ->
                if (includeHidden) true else !FileUtils.isHidden(file.name)
            }

            _scanState.value = ScanState.Scanning(50)

            val entries = filtered.mapNotNull { file ->
                when {
                    file.isDirectory -> MediaEntry.Folder(file)
                    MediaUtils.isVideoFile(file.name) -> {
                        val isNew = isFileNew(file, unplayedDaysThreshold) && file.absolutePath !in watchedPaths
                        val isWatched = file.absolutePath in watchedPaths

                        MediaEntry.Video(
                            file = file,
                            title = FileUtils.sanitizeDisplayName(file.name),
                            fileSize = file.length(),
                            isNew = isNew,
                            isWatched = isWatched
                        )
                    }
                    else -> null
                }
            }

            _scanState.value = ScanState.Scanning(90)

            val sorted = SortUtils.sortFiles(
                entries.map {
                    when (it) {
                        is MediaEntry.Folder -> it.file
                        is MediaEntry.Video -> it.file
                    }
                },
                sortBy, order
            ).map { sortedFile ->
                entries.find { entry ->
                    when (entry) {
                        is MediaEntry.Folder -> entry.file == sortedFile
                        is MediaEntry.Video -> entry.file == sortedFile
                    }
                } ?: MediaEntry.Folder(sortedFile)
            }

            _scanState.value = ScanState.Complete(sorted)
            sorted
        } catch (e: Exception) {
            _scanState.value = ScanState.Error(e.message ?: "Unknown error")
            emptyList()
        }
    }

    /**
     * Recursively scan directory for all video files.
     */
    suspend fun scanDirectoryFlat(
        directory: File,
        includeHidden: Boolean = false
    ): List<MediaEntry.Video> = withContext(Dispatchers.IO) {
        val videos = mutableListOf<MediaEntry.Video>()

        fun scanRecursive(dir: File) {
            val files = dir.listFiles() ?: return
            files.forEach { file ->
                if (!includeHidden && FileUtils.isHidden(file.name)) return@forEach

                if (file.isDirectory) {
                    scanRecursive(file)
                } else if (MediaUtils.isVideoFile(file.name)) {
                    videos.add(
                        MediaEntry.Video(
                            file = file,
                            title = FileUtils.sanitizeDisplayName(file.name),
                            fileSize = file.length()
                        )
                    )
                }
            }
        }

        scanRecursive(directory)
        videos
    }

    /**
     * Check if a file is considered "new" based on modification time.
     */
    private fun isFileNew(file: File, daysThreshold: Int): Boolean {
        val now = System.currentTimeMillis()
        val daysAgo = file.lastModified().let { modified ->
            (now - modified) / (1000L * 60 * 60 * 24)
        }
        return daysAgo <= daysThreshold
    }
}
