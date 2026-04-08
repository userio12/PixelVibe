package com.pixelvibe.vedioplayer.network

import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.mssmbj.SMBClient
import com.hierynomus.smbj.SmbConfig
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.connection.Connection
import com.hierynomus.smbj.session.Session
import com.hierynomus.smbj.share.DiskShare
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.concurrent.TimeUnit

/**
 * Represents a file/folder entry from SMB share.
 */
data class SMBFileEntry(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long
)

/**
 * SMB (Samba) client for accessing network shares.
 * Uses the SMBJ library for SMB2/3 protocol support.
 */
class SMBClientWrapper(
    private val host: String,
    private val port: Int = 445,
    private val username: String = "",
    private val password: String = "",
    private val isAnonymous: Boolean = false
) {
    private var connection: Connection? = null
    private var session: Session? = null
    private var share: DiskShare? = null

    suspend fun connect(shareName: String = ""): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            disconnect()
            val config = SmbConfig.builder().withTimeout(30, TimeUnit.SECONDS).build()
            val client = SMBClient(config)
            connection = client.connect(host)
            val authContext = if (isAnonymous) AuthenticationContext.anonymous()
            else AuthenticationContext(username, password.toCharArray(), null)
            session = connection?.authenticate(authContext)
            if (shareName.isNotBlank()) share = session?.connectShare(shareName) as? DiskShare
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun listFiles(remotePath: String = ""): Result<List<SMBFileEntry>> = withContext(Dispatchers.IO) {
        try {
            val currentShare = share ?: return@withContext Result.failure(IllegalStateException("Not connected"))
            val path = if (remotePath.startsWith("/")) remotePath.substring(1) else remotePath
            val entries = currentShare.list(path).map { info ->
                SMBFileEntry(
                    name = info.fileName,
                    path = "$remotePath/${info.fileName}",
                    isDirectory = info.fileAttributes.isDirectory,
                    size = info.endOfFile,
                    lastModified = info.lastWriteTime.toEpochMillis()
                )
            }.filter { it.name != "." && it.name != ".." }
            Result.success(entries)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getFileStream(remotePath: String): Result<InputStream> = withContext(Dispatchers.IO) {
        try {
            val currentShare = share ?: return@withContext Result.failure(IllegalStateException("Not connected"))
            val path = if (remotePath.startsWith("/")) remotePath.substring(1) else remotePath
            val file = currentShare.openFile(path, java.util.EnumSet.of(AccessMask.GENERIC_READ), null, null, com.hierynomus.mssmbj.SMB2CreateDisposition.FILE_OPEN, null)
            Result.success(file.inputStream)
        } catch (e: Exception) { Result.failure(e) }
    }

    fun isConnected(): Boolean = connection != null && session != null && share != null

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        try { share?.close(); session?.close(); connection?.close() } catch (_: Exception) {}
        share = null; session = null; connection = null
    }
}
