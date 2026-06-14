package com.pixelvibe.vedioplayer.core.player.datasource

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.TransferListener
import okhttp3.Call
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStream
import java.util.concurrent.TimeUnit

class WebDavDataSource : DataSource {

    private var inputStream: InputStream? = null
    private var uri: Uri? = null
    private var activeCall: okhttp3.Call? = null

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    override fun open(dataSpec: DataSpec): Long {
        val rawUri = dataSpec.uri.toString()
        val parsed = Uri.parse(rawUri)
        val user = parsed.userInfo?.substringBefore(":") ?: ""
        val pass = parsed.userInfo?.substringAfter(":") ?: ""
        val path = parsed.encodedPath ?: "/"
        val baseUrl = parsed.getQueryParameter("baseUrl") ?: throw java.io.IOException("Missing baseUrl in WebDAV URI")

        val fullUrl = baseUrl.trimEnd('/') + "/" + path.trimStart('/')
        val credentials = if (user.isNotEmpty()) Credentials.basic(user, pass) else null

        val requestBuilder = Request.Builder().url(fullUrl)

        if (dataSpec.position > 0) {
            requestBuilder.header("Range", "bytes=${dataSpec.position}-")
        }

        credentials?.let { requestBuilder.header("Authorization", it) }

        val request = requestBuilder.build()
        val newCall = client.newCall(request)
        activeCall = newCall
        val response = newCall.execute()
        val body = response.body ?: throw java.io.IOException("No response body from WebDAV")

        if (!response.isSuccessful) {
            body.close()
            throw java.io.IOException("WebDAV request failed: ${response.code}")
        }

        val length = body.contentLength().let {
            if (it > 0) it else C.LENGTH_UNSET
        }

        inputStream = body.byteStream()
        this.uri = Uri.parse(fullUrl)

        return length
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
            activeCall?.cancel()
        } catch (_: Exception) {}
        inputStream = null
        activeCall = null
        uri = null
    }

    override fun addTransferListener(transferListener: TransferListener) {}

    class Factory : DataSource.Factory {
        override fun create(): DataSource = WebDavDataSource()
    }
}
