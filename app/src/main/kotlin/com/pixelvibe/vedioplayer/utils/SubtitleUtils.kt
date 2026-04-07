package com.pixelvibe.vedioplayer.utils

import java.io.File
import java.io.InputStream

/**
 * Utility functions for subtitle parsing and format detection.
 */
object SubtitleUtils {
    /**
     * Supported subtitle formats with their common extensions.
     */
    enum class SubtitleFormat(val extensions: Set<String>) {
        SRT(setOf("srt", "subrip")),
        VTT(setOf("vtt", "webvtt")),
        ASS(setOf("ass")),
        SSA(setOf("ssa")),
        SUB(setOf("sub")),
        IDX(setOf("idx")),
        SUP(setOf("sup", "pgs")),
        TTML(setOf("ttml", "dfxp", "itt", "ebu", "imsc")),
        SAMI(setOf("sami", "smi")),
        MPL(setOf("mpl", "pjs")),
        STL(setOf("stl")),
        LRC(setOf("lrc", "krc")),
        SBV(setOf("sbv")),
        USF(setOf("usf")),
        JSON(setOf("json")),
        SRV(setOf("srv1", "srv2", "srv3")),
        SCC(setOf("scc", "vttx")),
        UNKNOWN(emptySet())
    }

    /**
     * Detect subtitle format from file extension.
     */
    fun detectFormat(fileName: String): SubtitleFormat {
        val ext = FileUtils.getExtension(fileName).lowercase()
        return SubtitleFormat.values().find { format ->
            format.extensions.any { it == ext }
        } ?: SubtitleFormat.UNKNOWN
    }

    /**
     * Detect subtitle format from content sniffing.
     */
    fun detectFormatFromContent(content: String): SubtitleFormat {
        val trimmed = content.trimStart()
        return when {
            trimmed.startsWith("WEBVTT") -> SubtitleFormat.VTT
            trimmed.startsWith("[Script Info]") && trimmed.contains("ScriptType.*ASS", RegexOption.DOT_MATCHES_ALL) -> SubtitleFormat.ASS
            trimmed.startsWith("[Script Info]") -> SubtitleFormat.SSA
            trimmed.startsWith("<smil") || trimmed.startsWith("<SAMI") -> SubtitleFormat.SAMI
            trimmed.startsWith("<?xml") && trimmed.contains("ttml", ignoreCase = true) -> SubtitleFormat.TTML
            trimmed.startsWith("{") || trimmed.startsWith("[") -> SubtitleFormat.JSON
            trimmed.contains("-->") -> SubtitleFormat.SRT
            else -> SubtitleFormat.UNKNOWN
        }
    }

    /**
     * Detect subtitle format from file (extension first, then content).
     */
    fun detectFormat(file: File): SubtitleFormat {
        val extFormat = detectFormat(file.name)
        if (extFormat != SubtitleFormat.UNKNOWN) return extFormat

        return try {
            val content = file.inputStream().bufferedReader().use { it.readText(4096) }
            detectFormatFromContent(content)
        } catch (e: Exception) {
            SubtitleFormat.UNKNOWN
        }
    }

    /**
     * Detect subtitle format from input stream.
     */
    fun detectFormat(inputStream: InputStream): SubtitleFormat {
        return try {
            val content = inputStream.bufferedReader().use { it.readText(4096) }
            detectFormatFromContent(content)
        } catch (e: Exception) {
            SubtitleFormat.UNKNOWN
        }
    }

    /**
     * Parse SRT subtitle file and extract entries.
     * Returns list of (index, startTime, endTime, text) tuples.
     */
    fun parseSRT(content: String): List<SubtitleEntry> {
        val entries = mutableListOf<SubtitleEntry>()
        val blocks = content.split(Regex("\n\\s*\\n"))

        blocks.forEach { block ->
            val lines = block.trim().lines()
            if (lines.size >= 3) {
                try {
                    val index = lines[0].trim().toIntOrNull() ?: entries.size + 1
                    val timeLine = lines[1].trim()
                    val text = lines.drop(2).joinToString("\n")

                    val timeParts = timeLine.split("-->")
                    if (timeParts.size == 2) {
                        val start = parseTimestamp(timeParts[0].trim())
                        val end = parseTimestamp(timeParts[1].trim())
                        entries.add(SubtitleEntry(index, start, end, text))
                    }
                } catch (_: Exception) {
                    // Skip malformed entries
                }
            }
        }

        return entries
    }

    /**
     * Parse VTT subtitle file.
     */
    fun parseVTT(content: String): List<SubtitleEntry> {
        // Remove WEBVTT header
        val body = content.substringAfter("\n\n", content)
        return parseSRT(body) // Similar format to SRT
    }

    /**
     * Parse ASS/SSA subtitle file header.
     * Returns metadata about the subtitle (title, script info, etc.)
     */
    fun parseASSHeader(content: String): Map<String, String> {
        val info = mutableMapOf<String, String>()
        val lines = content.lines()

        var inInfo = false
        lines.forEach { line ->
            when {
                line.startsWith("[Script Info]") -> inInfo = true
                line.startsWith("[") -> inInfo = false
                inInfo && line.contains("=") -> {
                    val parts = line.split("=", limit = 2)
                    if (parts.size == 2) {
                        info[parts[0].trim()] = parts[1].trim()
                    }
                }
            }
        }

        return info
    }

    /**
     * Check if a subtitle file is valid.
     */
    fun isValidSubtitle(file: File): Boolean {
        return try {
            val format = detectFormat(file)
            format != SubtitleFormat.UNKNOWN && file.length() > 0
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get file extension for a given subtitle format.
     */
    fun getExtensionForFormat(format: SubtitleFormat): String {
        return format.extensions.firstOrNull() ?: "srt"
    }

    /**
     * Parse a timestamp string (HH:MM:SS,mmm or HH:MM:SS.mm).
     */
    fun parseTimestamp(timestamp: String): Long {
        val cleaned = timestamp.replace(",", ".")
        val parts = cleaned.split(":")

        return when (parts.size) {
            3 -> {
                val hours = parts[0].toLongOrNull() ?: 0L
                val minutes = parts[1].toLongOrNull() ?: 0L
                val secondsParts = parts[2].split(".")
                val seconds = secondsParts[0].toLongOrNull() ?: 0L
                val millis = if (secondsParts.size > 1) secondsParts[1].toLongOrNull() ?: 0L else 0L
                hours * 3600000L + minutes * 60000L + seconds * 1000L + millis
            }
            2 -> {
                val minutes = parts[0].toLongOrNull() ?: 0L
                val secondsParts = parts[1].split(".")
                val seconds = secondsParts[0].toLongOrNull() ?: 0L
                val millis = if (secondsParts.size > 1) secondsParts[1].toLongOrNull() ?: 0L else 0L
                minutes * 60000L + seconds * 1000L + millis
            }
            else -> 0L
        }
    }

    /**
     * Convert milliseconds to timestamp string (HH:MM:SS,mmm).
     */
    fun formatTimestamp(millis: Long): String {
        val hours = millis / 3600000L
        val minutes = (millis % 3600000L) / 60000L
        val seconds = (millis % 60000L) / 1000L
        val ms = millis % 1000L
        return String.format("%02d:%02d:%02d,%03d", hours, minutes, seconds, ms)
    }

    /**
     * Represents a single subtitle entry with timing.
     */
    data class SubtitleEntry(
        val index: Int,
        val startTime: Long,
        val endTime: Long,
        val text: String
    )
}
