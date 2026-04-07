package com.pixelvibe.vedioplayer.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * TMDB API client for online subtitle search.
 * Used to find media by title and then search for matching subtitles.
 * Phase 5: Full integration requires TMDB API key.
 */
class TMDBClient(
    private val apiKey: String = "",
    private val language: String = "en"
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val baseUrl = "https://api.themoviedb.org/3"

    /**
     * Search for a movie by title.
     */
    fun searchMovie(query: String, year: Int? = null): TmdbSearchResult {
        if (apiKey.isBlank()) {
            return TmdbSearchResult(emptyList())
        }

        val url = StringBuilder("$baseUrl/search/movie?api_key=$apiKey&query=${query.encodeUrl()}&language=$language")
        year?.let { url.append("&year=$it") }

        val request = Request.Builder()
            .url(url.toString())
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.string()?.let { body ->
                        json.decodeFromString(body)
                    } ?: TmdbSearchResult(emptyList())
                } else {
                    TmdbSearchResult(emptyList())
                }
            }
        } catch (e: Exception) {
            TmdbSearchResult(emptyList())
        }
    }

    /**
     * Search for a TV show by title.
     */
    fun searchTV(query: String, firstAirYear: Int? = null): TmdbSearchResult {
        if (apiKey.isBlank()) {
            return TmdbSearchResult(emptyList())
        }

        val url = StringBuilder("$baseUrl/search/tv?api_key=$apiKey&query=${query.encodeUrl()}&language=$language")
        firstAirYear?.let { url.append("&first_air_date_year=$it") }

        val request = Request.Builder()
            .url(url.toString())
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.string()?.let { body ->
                        json.decodeFromString(body)
                    } ?: TmdbSearchResult(emptyList())
                } else {
                    TmdbSearchResult(emptyList())
                }
            }
        } catch (e: Exception) {
            TmdbSearchResult(emptyList())
        }
    }

    /**
     * Get TV show details with seasons and episodes.
     */
    fun getTVDetails(tvId: Int): TmdbTVDetails? {
        if (apiKey.isBlank()) return null

        val url = "$baseUrl/tv/$tvId?api_key=$apiKey&language=$language&external_source=imdb_id"
        val request = Request.Builder().url(url).build()

        return try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.string()?.let { body ->
                        json.decodeFromString(body)
                    }
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Search for subtitles using OpenSubtitles API (alternative to TMDB).
     * This is the actual subtitle download endpoint.
     */
    fun searchSubtitles(
        tmdbId: Int,
        type: String, // "movie" or "tv"
        season: Int? = null,
        episode: Int? = null,
        languages: List<String> = listOf("en")
    ): List<TmdbSubtitleResult> {
        // In production, this would call OpenSubtitles API with TMDB ID
        // For now, returns empty list as placeholder
        return emptyList()
    }

    /**
     * Check if the API key is configured.
     */
    fun isConfigured(): Boolean = apiKey.isNotBlank()
}

// ── TMDB Response Models ──

@Serializable
data class TmdbSearchResult(
    val page: Int = 0,
    @SerialName("total_results") val totalResults: Int = 0,
    @SerialName("total_pages") val totalPages: Int = 0,
    val results: List<TmdbMedia> = emptyList()
)

@Serializable
data class TmdbMedia(
    val id: Int = 0,
    val title: String = "",
    val name: String = "",
    @SerialName("release_date") val releaseDate: String = "",
    @SerialName("first_air_date") val firstAirDate: String = "",
    @SerialName("media_type") val mediaType: String = "unknown",
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("overview") val overview: String = "",
    @SerialName("vote_average") val voteAverage: Float = 0f,
    @SerialName("popularity") val popularity: Float = 0f
) {
    val displayName: String
        get() = title.ifBlank { name }

    val year: Int?
        get() = (releaseDate.ifBlank { firstAirDate }).takeIf { it.isNotBlank() }
            ?.substring(0, 4)?.toIntOrNull()
}

@Serializable
data class TmdbTVDetails(
    val id: Int = 0,
    val name: String = "",
    val seasons: List<TmdbSeason> = emptyList()
)

@Serializable
data class TmdbSeason(
    @SerialName("season_number") val seasonNumber: Int = 0,
    @SerialName("episode_count") val episodeCount: Int = 0,
    val name: String = ""
)

@Serializable
data class TmdbSubtitleResult(
    val id: String = "",
    val language: String = "",
    val fileName: String = "",
    val downloads: Int = 0,
    val rating: Float = 0f,
    val url: String = ""
)

// ── Utility ──

private fun String.encodeUrl(): String {
    return java.net.URLEncoder.encode(this, "UTF-8")
}
