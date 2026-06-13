package com.pixelvibe.vedioplayer.core.data.scanner

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.runBlocking
import com.pixelvibe.vedioplayer.core.common.result.DataError
import com.pixelvibe.vedioplayer.core.common.result.EmptyResult
import com.pixelvibe.vedioplayer.core.common.result.Result
import com.pixelvibe.vedioplayer.core.data.db.dao.VideoDao
import com.pixelvibe.vedioplayer.core.data.db.entity.VideoEntity
class MediaScanner(
    private val context: Context,
    private val videoDao: VideoDao
) {

    companion object {
        private val SUPPORTED_MIME_TYPES = setOf(
            "video/mp4",
            "video/x-matroska",
            "video/avi",
            "video/quicktime",
            "video/webm",
            "video/x-m4v",
            "video/x-flv",
            "video/mp2t",
            "video/3gpp",
            "video/mpeg"
        )
    }

    fun scanVideos(): EmptyResult<DataError.Local> {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_VIDEO
        else
            Manifest.permission.READ_EXTERNAL_STORAGE
        if (context.checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            return Result.Error(DataError.Local.PERMISSION_DENIED)
        }
        return try {
            val videos = queryMediaStore()
            val videoEntities = videos.map { it.toVideoEntity() }
            runBlocking { videoDao.insertAll(videoEntities) }
            Result.Success(Unit)
        } catch (e: SecurityException) {
            Result.Error(DataError.Local.PERMISSION_DENIED)
        } catch (e: Exception) {
            Result.Error(DataError.Local.UNKNOWN)
        }
    }

    private fun queryMediaStore(): List<ScannedVideo> {
        val resolver = context.contentResolver
        val collection = if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DATE_MODIFIED
        )

        val sortOrder = "${MediaStore.Video.Media.DATE_MODIFIED} DESC"

        val videos = mutableListOf<ScannedVideo>()

        resolver.query(collection, projection, null, null, sortOrder)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val durCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
            val addedCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            val modCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)

            while (cursor.moveToNext()) {
                val mimeType = cursor.getString(mimeCol)
                if (mimeType == null || mimeType !in SUPPORTED_MIME_TYPES) continue

                val filePath = cursor.getString(dataCol) ?: continue

                val id = cursor.getLong(idCol)
                val title = cursor.getString(titleCol) ?: filePath.substringAfterLast('/').ifEmpty { "Unknown" }
                val size = cursor.getLong(sizeCol)
                val duration = cursor.getLong(durCol)
                val added = cursor.getLong(addedCol)
                val modified = cursor.getLong(modCol)

                videos.add(
                    ScannedVideo(
                        id = "media_$id",
                        mediaStoreId = id,
                        title = title,
                        uri = filePath,
                        filePath = filePath,
                        mimeType = mimeType,
                        durationMs = duration,
                        fileSize = size,
                        addedAt = added,
                        modifiedAt = modified
                    )
                )
            }
        }
        return videos
    }

    private data class ScannedVideo(
        val id: String,
        val mediaStoreId: Long,
        val title: String,
        val uri: String,
        val filePath: String,
        val mimeType: String?,
        val durationMs: Long,
        val fileSize: Long,
        val addedAt: Long,
        val modifiedAt: Long
    )

    private fun ScannedVideo.toVideoEntity(): VideoEntity {
        val contentUri = Uri.withAppendedPath(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            mediaStoreId.toString()
        ).toString()
        return VideoEntity(
            id = id,
            title = title,
            uri = contentUri,
            filePath = filePath,
            mimeType = mimeType,
            durationMs = durationMs,
            fileSize = fileSize,
            folderName = filePath.substringBeforeLast('/').substringAfterLast('/').ifEmpty { null },
            addedAt = addedAt,
            modifiedAt = modifiedAt
        )
    }
}
