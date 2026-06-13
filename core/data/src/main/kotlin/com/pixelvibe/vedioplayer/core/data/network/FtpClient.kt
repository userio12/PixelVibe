package com.pixelvibe.vedioplayer.core.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.net.ftp.FTPClient as ApacheFtpClient

open class FtpClient {

    private var client: ApacheFtpClient? = null

    open suspend fun listFiles(source: NetworkSource.Ftp): List<NetworkFile> = withContext(Dispatchers.IO) {
        val ftp = client ?: ApacheFtpClient().also {
            it.connect(source.host, source.port)
            it.login(source.username, source.password)
            it.enterLocalPassiveMode()
            client = it
        }
        ftp.changeWorkingDirectory(source.path)
        val files = ftp.listFiles()
        files.map { file ->
            NetworkFile(
                name = file.name,
                path = "${source.path}/${file.name}",
                isDirectory = file.isDirectory,
                size = file.size,
                lastModified = file.timestamp?.time?.time ?: 0,
                source = source
            )
        }
    }

    open suspend fun authenticate(source: NetworkSource.Ftp): Boolean = withContext(Dispatchers.IO) {
        try {
            release()
            val ftp = ApacheFtpClient()
            ftp.connect(source.host, source.port)
            val loggedIn = ftp.login(source.username, source.password)
            if (loggedIn) {
                ftp.enterLocalPassiveMode()
                client = ftp
            } else {
                ftp.disconnect()
            }
            loggedIn
        } catch (_: Exception) {
            false
        }
    }

    fun release() {
        try {
            client?.logout()
            client?.disconnect()
        } catch (_: Exception) {}
        client = null
    }
}
