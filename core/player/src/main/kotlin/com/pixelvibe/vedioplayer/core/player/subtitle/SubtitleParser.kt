package com.pixelvibe.vedioplayer.core.player.subtitle

object SubtitleParser {

    fun parseSrt(content: String): List<SubtitleManager.SubtitleCue> {
        val cues = mutableListOf<SubtitleManager.SubtitleCue>()
        val blocks = content.split(Regex("\\n\\s*\\n"))

        for (block in blocks) {
            val lines = block.trim().lines()
            if (lines.size < 3) continue

            val timeLine = lines.firstOrNull { it.contains("-->") } ?: continue
            val timings = parseTimingLine(timeLine) ?: continue
            val text = lines.dropWhile { it.contains("-->") || it.toIntOrNull() != null }
                .joinToString("\n")
                .trim()
            if (text.isNotEmpty()) {
                cues.add(SubtitleManager.SubtitleCue(timings.first, timings.second, text))
            }
        }
        return cues
    }

    fun parseVtt(content: String): List<SubtitleManager.SubtitleCue> {
        val cleaned = content.replace(Regex("WEBVTT\\s*"), "")
        return parseSrt(cleaned)
    }

    private fun parseTimingLine(line: String): Pair<Long, Long>? {
        val parts = line.split(Regex("\\s+-->\\s+"))
        if (parts.size != 2) return null
        val start = parseTime(parts[0].trim()) ?: return null
        val end = parseTime(parts[1].trim().split(" ").first()) ?: return null
        return start to end
    }

    private fun parseTime(time: String): Long? {
        val regex = Regex("(\\d+):(\\d{2}):(\\d{2})[.,](\\d{3})")
        val match = regex.find(time) ?: run {
            val shortRegex = Regex("(\\d{2}):(\\d{2})[.,](\\d{3})")
            val shortMatch = shortRegex.find(time)
            if (shortMatch != null) {
                val (minutes, seconds, millis) = shortMatch.destructured
                return minutes.toLong() * 60_000 + seconds.toLong() * 1000 + millis.toLong()
            }
            return null
        }
        val (hours, minutes, seconds, millis) = match.destructured
        return hours.toLong() * 3_600_000 + minutes.toLong() * 60_000 + seconds.toLong() * 1000 + millis.toLong()
    }
}
