package com.pixelvibe.vedioplayer.domain

import android.content.Context
import android.graphics.Bitmap
import com.pixelvibe.vedioplayer.utils.MediaUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Manages thumbnail generation and caching with LRU eviction.
 */
class ThumbnailManager(
    private val context: Context,
    private val maxCacheSize: Int = 100
) {
    private val cacheDir = File(context.cacheDir, "thumbnails").apply { mkdirs() }
    private val inMemoryCache = object : LinkedHashMap<String, Bitmap>(maxCacheSize, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Bitmap>?): Boolean {
            return size > maxCacheSize
        }
    }

    /**
     * Get thumbnail for a file, from cache or generated.
     */
    suspend fun getThumbnail(filePath: String, size: Int = 256): Bitmap? = withContext(Dispatchers.IO) {
        val cacheKey = filePath.hashCode().toString()

        // Check in-memory cache
        inMemoryCache[cacheKey]?.let { return@withContext it }

        // Check disk cache
        val cacheFile = File(cacheDir, cacheKey)
        if (cacheFile.exists()) {
            val bitmap = android.graphics.BitmapFactory.decodeFile(cacheFile.absolutePath)
            bitmap?.let {
                inMemoryCache[cacheKey] = it
                return@withContext it
            }
        }

        // Generate new thumbnail
        MediaUtils.generateThumbnail(context, filePath, size)?.also { bitmap ->
            inMemoryCache[cacheKey] = bitmap
            // Save to disk
            try {
                FileOutputStream(cacheFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.WEBP, 80, out)
                }
            } catch (_: Exception) {}
        }
    }

    /**
     * Clear all cached thumbnails.
     */
    suspend fun clearCache() = withContext(Dispatchers.IO) {
        inMemoryCache.clear()
        cacheDir.listFiles()?.forEach { it.delete() }
    }
}
