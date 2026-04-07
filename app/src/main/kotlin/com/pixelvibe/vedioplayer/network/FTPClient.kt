package com.pixelvibe.vedioplayer.network

import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

/**
 * Represents a file/folder entry from FTP server.
 */
data class FTPFileEntry(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long
)

/**
 * FTP client for accessing remote files.
 * Uses Apache Commons Net library.
 */
class FTPClientWrapper(
    private val host: String,
    private val port: Int = 21,
    private val username: String = "",
    private val password: String = "",
    private val isAnonymous: Boolean = false
) {
    private val client = FTPClient()
    private var connected = false

    /**
     * Connect and login to the FTP server.
     */
    suspend fun connect(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            client.connect(host, port)
            client.enterLocalPassiveMode()
            client.setFileType(FTP.BINARY_FILE_TYPE)
            client.setFileTransferMode(FTP.STREAM_TRANSFER_MODE)

            val loginSuccess = if (isAnonymous) {
                client.login("anonymous", "anonymous@")
            } else {
                client.login(username, password)
            }

            if (loginSuccess) {
                connected = true
                Result.success(Unit)
            } else {
                disconnect()
                Result.failure(Exception("FTP login failed"))
            }
        } catch (e: Exception) {
            disconnect()
            Result.failure(e)
        }
    }

    /**
     * List files in the current or specified directory.
     */
    suspend fun listFiles(remotePath: String = ""): Result<List<FTPFileEntry>> = withContext(Dispatchers.IO) {
        try {
            if (!connected) return@withContext Result.failure(IllegalStateException("Not connected"))

            if (remotePath.isNotBlank()) {
                client.changeWorkingDirectory(remotePath)
            }

            val files = client.listFiles()?.mapNotNull { ftpFile ->
                if (ftpFile.name == "." || ftpFile.name == "..") return@mapNotNull null
                FTPFileEntry(
                    name = ftpFile.name,
                    path = "${client.printWorkingDirectory()}/${ftpFile.name}",
                    isDirectory = ftpFile.isDirectory,
                    size = ftpFile.size,
                    lastModified = ftpFile.timestamp?.timeInMillis ?: 0L
                )
            } ?: emptyList()

            Result.success(files)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get an input stream for a remote file.
     */
    suspend fun getFileStream(remotePath: String): Result<InputStream> = withContext(Dispatchers.IO) {
        try {
            if (!connected) return@withContext Result.failure(IllegalStateException("Not connected"))

            val inputStream = client.retrieveFileStream(remotePath)
            if (inputStream != null) {
                Result.success(inputStream)
            } else {
                Result.failure(Exception("Failed to retrieve file: $remotePath"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get current working directory.
     */
    fun getWorkingDirectory(): String = try {
        client.printWorkingDirectory()
    } catch (e: Exception) {
        "/"
    }

    /**
     * Change working directory.
     */
    suspend fun changeDirectory(path: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (client.changeWorkingDirectory(path)) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to change directory to: $path"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Check if connected.
     */
    fun isConnected(): Boolean = connected && client.isConnected

    /**
     * Disconnect from the FTP server.
     */
    suspend fun disconnect() = withContext(Dispatchers.IO) {
        try {
            if (client.isConnected) {
                client.logout()
                client.disconnect()
            }
        } catch (_: Exception) {}
        connected = false
    }
}
