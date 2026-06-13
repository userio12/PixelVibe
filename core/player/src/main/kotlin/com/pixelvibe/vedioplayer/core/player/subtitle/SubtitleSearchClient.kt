package com.pixelvibe.vedioplayer.core.player.subtitle

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class SubtitleSearchResult(
    val id: String,
    val name: String,
    val language: String,
    val downloadUrl: String,
    val format: String,
    val rating: Float
)

class SubtitleSearchClient(
    private val apiKey: String = ""
) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json".toMediaType()

    suspend fun search(query: String, language: String = "en"): List<SubtitleSearchResult> {
        return withContext(Dispatchers.IO) {
            try {
                if (apiKey.isNotBlank()) {
                    searchV2(query, language)
                } else {
                    searchV1(query)
                }
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    suspend fun searchByHash(
        fileHash: String,
        movieBytesize: Long,
        language: String = "en"
    ): List<SubtitleSearchResult> {
        return withContext(Dispatchers.IO) {
            try {
                if (apiKey.isNotBlank()) {
                    searchByHashV2(fileHash, movieBytesize, language)
                } else {
                    searchByHashV1(fileHash, movieBytesize)
                }
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    private fun searchV2(query: String, language: String): List<SubtitleSearchResult> {
        val url = "https://api.opensubtitles.com/api/v1/subtitles" +
            "?query=${java.net.URLEncoder.encode(query, "UTF-8")}" +
            "&languages=$language"
        val request = Request.Builder()
            .url(url)
            .addHeader("Api-Key", apiKey)
            .addHeader("User-Agent", "PixelVibe v1.0")
            .addHeader("Accept", "application/json")
            .build()
        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: return emptyList()
        return parseV2Results(body)
    }

    private fun searchByHashV2(fileHash: String, movieBytesize: Long, language: String): List<SubtitleSearchResult> {
        val url = "https://api.opensubtitles.com/api/v1/subtitles" +
            "?moviehash=$fileHash" +
            "&moviebytesize=$movieBytesize" +
            "&languages=$language"
        val request = Request.Builder()
            .url(url)
            .addHeader("Api-Key", apiKey)
            .addHeader("User-Agent", "PixelVibe v1.0")
            .addHeader("Accept", "application/json")
            .build()
        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: return emptyList()
        return parseV2Results(body)
    }

    private fun parseV2Results(json: String): List<SubtitleSearchResult> {
        val results = mutableListOf<SubtitleSearchResult>()
        val root = JSONObject(json)
        val data = root.optJSONArray("data") ?: return results
        for (i in 0 until data.length()) {
            val item = data.getJSONObject(i)
            val attrs = item.optJSONObject("attributes") ?: continue
            val files = attrs.optJSONArray("files")
            val fileId = files?.optJSONObject(0)?.optString("file_id", "") ?: ""
            val featureDetails = attrs.optJSONObject("feature_details")
            val title = featureDetails?.optString("title", "") ?: attrs.optString("title", "")
            results.add(
                SubtitleSearchResult(
                    id = attrs.optString("subtitle_id", item.optString("id", "")),
                    name = title,
                    language = attrs.optString("language", ""),
                    downloadUrl = if (fileId.isNotBlank()) "https://api.opensubtitles.com/api/v1/download" else "",
                    format = attrs.optString("subtitle_format", "srt"),
                    rating = attrs.optDouble("ratings", 0.0).toFloat()
                )
            )
        }
        return results
    }

    private fun searchV1(query: String): List<SubtitleSearchResult> {
        val url = "https://rest.opensubtitles.org/search/query-${java.net.URLEncoder.encode(query, "UTF-8")}"
        val request = Request.Builder()
            .url(url)
            .addHeader("User-Agent", "PixelVibe v1.0")
            .addHeader("Accept", "application/json")
            .build()
        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: return emptyList()
        return parseV1Results(body)
    }

    private fun searchByHashV1(fileHash: String, movieBytesize: Long): List<SubtitleSearchResult> {
        val url = "https://rest.opensubtitles.org/search/subtitles-by-hash-$fileHash-$movieBytesize"
        val request = Request.Builder()
            .url(url)
            .addHeader("User-Agent", "PixelVibe v1.0")
            .addHeader("Accept", "application/json")
            .build()
        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: return emptyList()
        return parseV1Results(body)
    }

    private fun parseV1Results(json: String): List<SubtitleSearchResult> {
        val results = mutableListOf<SubtitleSearchResult>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                results.add(
                    SubtitleSearchResult(
                        id = item.optString("IDSubtitleFile", ""),
                        name = item.optString("MovieReleaseName", ""),
                        language = item.optString("LanguageName", ""),
                        downloadUrl = item.optString("SubDownloadLink", ""),
                        format = item.optString("SubFormat", "srt"),
                        rating = item.optDouble("SubRating", 0.0).toFloat()
                    )
                )
            }
        } catch (_: Exception) {
            return emptyList()
        }
        return results
    }
}
