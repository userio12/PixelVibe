package com.pixelvibe.vedioplayer.core.player.datasource

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.TransferListener
import org.apache.commons.net.ftp.FTPClient
import java.io.InputStream

class FtpDataSource : DataSource {

    private var inputStream: InputStream? = null
    private var ftpClient: FTPClient? = null
    private var uri: Uri? = null

    override fun open(dataSpec: DataSpec): Long {
        val parsed = Uri.parse(dataSpec.uri.toString())
        val host = parsed.host ?: throw java.io.IOException("No host in FTP URI")
        val port = if (parsed.port > 0) parsed.port else 21
        val user = parsed.userInfo?.substringBefore(":") ?: "anonymous"
        val pass = parsed.userInfo?.substringAfter(":") ?: "anonymous@"
        val path = parsed.encodedPath ?: "/"

        val ftp = FTPClient()
        ftp.connect(host, port)
        if (!ftp.login(user, pass)) {
            ftp.disconnect()
            throw java.io.IOException("FTP login failed")
        }
        ftp.enterLocalPassiveMode()
        ftp.setFileType(FTPClient.BINARY_FILE_TYPE)

        if (dataSpec.position > 0) {
            ftp.setRestartOffset(dataSpec.position)
        }

        val stream = ftp.retrieveFileStream(path)
        if (stream == null) {
            ftp.disconnect()
            throw java.io.IOException("FTP retrieveFileStream returned null")
        }

        inputStream = stream
        ftpClient = ftp
        uri = Uri.parse("ftp://$host:$port$path")

        return C.LENGTH_UNSET
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        return inputStream?.read(buffer, offset, length) ?: -1
    }

    override fun getUri(): Uri = uri ?: Uri.EMPTY

    override fun close() {
        try {
            inputStream?.close()
        } catch (_: Exception) {}
        try {
            ftpClient?.completePendingCommand()
        } catch (_: Exception) {}
        try {
            ftpClient?.logout()
        } catch (_: Exception) {}
        try {
            ftpClient?.disconnect()
        } catch (_: Exception) {}
        inputStream = null
        ftpClient = null
        uri = null
    }

    override fun addTransferListener(transferListener: TransferListener) {}

    class Factory : DataSource.Factory {
        override fun create(): DataSource = FtpDataSource()
    }
}
