package com.pixelvibe.vedioplayer.utils

import java.io.File

/**
 * Sort types for file browser.
 */
enum class SortType {
    NAME, DATE, SIZE, DURATION
}

/**
 * Sort order.
 */
enum class SortOrder {
    ASCENDING, DESCENDING
}

/**
 * Utility functions for sorting file lists.
 */
object SortUtils {
    /**
     * Sort a list of files based on the given type and order.
     * Folders always come first, then sorted by the specified criteria.
     */
    fun sortFiles(
        files: List<File>,
        sortBy: SortType = SortType.NAME,
        order: SortOrder = SortOrder.ASCENDING
    ): List<File> {
        val (folders, regularFiles) = files.partition { it.isDirectory }

        val sortComparator = when (sortBy) {
            SortType.NAME -> compareBy<File> { it.name.lowercase() }
            SortType.DATE -> compareBy<File> { it.lastModified() }
            SortType.SIZE -> compareBy<File> { if (it.isDirectory) 0L else it.length() }
            SortType.DURATION -> compareBy<File> { 0L } // Duration handled separately with metadata
        }

        val comparator = if (order == SortOrder.ASCENDING) sortComparator else sortComparator.reversed()

        return folders.sortedWith(compareBy { it.name.lowercase() }) +
               regularFiles.sortedWith(comparator)
    }
}
