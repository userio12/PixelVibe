package com.pixelvibe.vedioplayer.core.data.repository

import com.pixelvibe.vedioplayer.core.common.result.DataError
import com.pixelvibe.vedioplayer.core.common.result.EmptyResult
import com.pixelvibe.vedioplayer.core.common.result.Result
import com.pixelvibe.vedioplayer.core.common.result.asEmptyResult
import com.pixelvibe.vedioplayer.core.data.db.dao.VideoDao
import com.pixelvibe.vedioplayer.core.data.db.entity.VideoEntity
import kotlinx.coroutines.flow.Flow

class VideoRepository(private val videoDao: VideoDao) {

    fun getAllVideos(): Flow<List<VideoEntity>> = videoDao.getAllVideos()

    fun getVideosByFolder(folder: String): Flow<List<VideoEntity>> = videoDao.getVideosByFolder(folder)

    fun getFavoriteVideos(): Flow<List<VideoEntity>> = videoDao.getFavoriteVideos()

    fun getFolders(): Flow<List<String>> = videoDao.getAllFolders()

    fun searchVideos(query: String): Flow<List<VideoEntity>> = videoDao.searchVideos(query)

    suspend fun getVideoById(id: String): VideoEntity? = videoDao.getVideoById(id)

    suspend fun insertVideos(videos: List<VideoEntity>): EmptyResult<DataError.Local> {
        return try {
            videoDao.insertAll(videos)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(DataError.Local.UNKNOWN)
        }
    }

    suspend fun updateResumePosition(id: String, positionMs: Long): EmptyResult<DataError.Local> {
        return try {
            videoDao.updateResumePosition(id, positionMs)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(DataError.Local.UNKNOWN)
        }
    }

    suspend fun toggleFavorite(id: String, favorite: Boolean): EmptyResult<DataError.Local> {
        return try {
            videoDao.updateFavorite(id, favorite)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(DataError.Local.UNKNOWN)
        }
    }
}
