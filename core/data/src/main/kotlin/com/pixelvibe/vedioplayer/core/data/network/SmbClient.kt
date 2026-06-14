package com.pixelvibe.vedioplayer.core.data.network

import jcifs.CIFSContext
import jcifs.Config
import jcifs.context.BaseContext
import jcifs.smb.NtlmPasswordAuthenticator
import jcifs.smb.SmbFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

open class SmbClient {

    init {
        Config.setProperty("jcifs.smb.client.responseTimeout", "30000")
        Config.setProperty("jcifs.smb.client.connTimeout", "15000")
        Config.setProperty("jcifs.smb.client.soTimeout", "30000")
    }

    private var context: CIFSContext? = null

    open suspend fun listFiles(source: NetworkSource.Smb): List<NetworkFile> = withContext(Dispatchers.IO) {
        val ctx = context ?: BaseContext(null).apply { context = this }
        val baseUrl = "smb://${source.host}/${source.share}/${source.path}"
        val smbFile = SmbFile(baseUrl, ctx)
        val parts = smbFile.path.split("/").filter { it.isNotBlank() }
        val breadcrumbs = parts.mapIndexed { i, _ ->
            val crumbPath = parts.take(i + 1).joinToString("/")
            Breadcrumb(parts[i], "smb://${source.host}/${source.share}/$crumbPath")
        }
        smbFile.listFiles().map { file ->
            NetworkFile(
                name = file.name,
                path = file.path,
                isDirectory = file.isDirectory,
                size = file.length(),
                lastModified = file.lastModified(),
                source = source
            )
        }
    }

    open suspend fun authenticate(source: NetworkSource.Smb): Boolean = withContext(Dispatchers.IO) {
        try {
            val base = BaseContext(null)
            val authCtx = base.withCredentials(
                NtlmPasswordAuthenticator(source.username, source.password)
            )
            val url = "smb://${source.host}/${source.share}/"
            val file = SmbFile(url, authCtx)
            file.listFiles()
            context = authCtx
            true
        } catch (_: Exception) {
            false
        }
    }

    open fun release() {
        context = null
    }
}
