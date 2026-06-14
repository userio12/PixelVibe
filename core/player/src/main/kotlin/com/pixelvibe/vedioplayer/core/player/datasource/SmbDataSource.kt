package com.pixelvibe.vedioplayer.core.player.datasource

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.TransferListener
import jcifs.CIFSContext
import jcifs.CIFSException
import jcifs.context.BaseContext
import jcifs.smb.NtlmPasswordAuthenticator
import jcifs.smb.SmbFile
import java.io.InputStream

class SmbDataSource : DataSource {

    private var inputStream: InputStream? = null
    private var smbFile: SmbFile? = null
    private var uri: Uri? = null
    private var context: CIFSContext? = null

    override fun open(dataSpec: DataSpec): Long {
        val rawUri = dataSpec.uri.toString()
        val parsed = Uri.parse(rawUri)
        val user = parsed.userInfo?.substringBefore(":") ?: "guest"
        val pass = parsed.userInfo?.substringAfter(":") ?: ""

        val cleanUri = Uri.Builder()
            .scheme("smb")
            .encodedAuthority(parsed.host + if (parsed.port > 0) ":${parsed.port}" else "")
            .encodedPath(parsed.encodedPath)
            .build().toString()

        val ctx = BaseContext(null)
        val authCtx = if (user.isNotEmpty()) {
            ctx.withCredentials(NtlmPasswordAuthenticator(user, pass))
        } else ctx

        val file = try {
            SmbFile(cleanUri, authCtx)
        } catch (e: CIFSException) {
            throw java.io.IOException("Failed to open SMB file", e)
        }

        val stream = file.getInputStream()
        if (dataSpec.position > 0) {
            stream.skip(dataSpec.position)
        }

        inputStream = stream
        smbFile = file
        uri = Uri.parse(cleanUri)
        context = authCtx

        return C.LENGTH_UNSET
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Long {
        return (inputStream?.read(buffer, offset, length) ?: -1).toLong()
    }

    override fun getUri(): Uri = uri ?: Uri.EMPTY

    override fun close() {
        try {
            inputStream?.close()
        } catch (_: Exception) {}
        inputStream = null
        smbFile = null
        uri = null
        context = null
    }

    override fun addTransferListener(transferListener: TransferListener) {}

    class Factory : DataSource.Factory {
        override fun createDataSource(): DataSource = SmbDataSource()
    }
}
