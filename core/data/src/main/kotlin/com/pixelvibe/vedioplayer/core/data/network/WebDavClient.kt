package com.pixelvibe.vedioplayer.core.data.network

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

open class WebDavClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private var credentials: String? = null

    open suspend fun listFiles(source: NetworkSource.WebDav): List<NetworkFile> = withContext(Dispatchers.IO) {
        val base = source.baseUrl.trimEnd('/')
        val path = source.path.trimStart('/')
        val url = "$base/$path"
        credentials = if (source.username.isNotEmpty()) {
            Credentials.basic(source.username, source.password)
        } else null

        val request = Request.Builder()
            .url(url)
            .apply { credentials?.let { header("Authorization", it) } }
            .header("Depth", "1")
            .method("PROPFIND", null)
            .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: return@withContext emptyList()
        parseDavResponse(body, source)
    }

    private fun parseDavResponse(xml: String, source: NetworkSource.WebDav): List<NetworkFile> {
        val files = mutableListOf<NetworkFile>()
        try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(xml.reader())
            var currentHref = ""
            var currentName = ""
            var isDirectory = false
            var size = 0L
            var insideResponse = false

            while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                when (parser.eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "response" -> insideResponse = true
                            "href" -> currentHref = parser.nextText()
                            "displayname" -> currentName = parser.nextText()
                            "collection" -> isDirectory = true
                            "getcontentlength" -> size = parser.nextText().toLongOrNull() ?: 0
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name == "response" && insideResponse) {
                            if (currentHref.isNotEmpty()) {
                                val hrefPath = Uri.parse(currentHref).path ?: currentHref
                                val basePath = Uri.parse(source.baseUrl).path ?: ""
                                val path = hrefPath.removePrefix(basePath).trim('/')
                                val name = currentName.ifEmpty { path.split("/").last().ifEmpty { path } }
                                files.add(NetworkFile(
                                    name = name,
                                    path = path,
                                    isDirectory = isDirectory,
                                    size = size,
                                    source = source
                                ))
                            }
                            currentHref = ""
                            currentName = ""
                            isDirectory = false
                            size = 0L
                            insideResponse = false
                        }
                    }
                }
                parser.next()
            }
        } catch (_: Exception) {}
        return files
    }

    open suspend fun authenticate(source: NetworkSource.WebDav): Boolean = withContext(Dispatchers.IO) {
        try {
            val creds = Credentials.basic(source.username, source.password)
            credentials = creds
            val request = Request.Builder()
                .url(source.baseUrl)
                .header("Authorization", creds)
                .method("PROPFIND", null)
                .header("Depth", "0")
                .build()
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (_: Exception) {
            false
        }
    }

    fun release() {}
}
