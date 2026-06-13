package com.pixelvibe.vedioplayer.core.player.subtitle

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URI
import java.net.URL

data class SubtitleState(
    val isEnabled: Boolean = false,
    val currentText: String? = null,
    val delayMs: Long = 0,
    val tracks: List<SubtitleTrack> = emptyList(),
    val selectedTrackIndex: Int = -1
)

data class SubtitleTrack(
    val id: String,
    val name: String,
    val language: String?,
    val isEmbedded: Boolean,
    val isExternal: Boolean
)

class SubtitleManager {

    private val _state = MutableStateFlow(SubtitleState())
    val state: StateFlow<SubtitleState> = _state.asStateFlow()

    private val subtitles = mutableListOf<SubtitleEntry>()

    suspend fun addExternalSubtitle(uri: String, name: String, language: String?, content: ByteArray? = null) {
        val text = content?.toString(Charsets.UTF_8) ?: withContext(Dispatchers.IO) {
            try {
                if (uri.startsWith("http")) URL(uri).readText()
                else File(URI.create(uri)).readText()
            } catch (_: Exception) { null }
        } ?: return
        val cues = if (uri.endsWith(".vtt", ignoreCase = true) ||
            content.trimStart().startsWith("WEBVTT")
        ) SubtitleParser.parseVtt(content)
        else SubtitleParser.parseSrt(content)
        subtitles.add(SubtitleEntry(uri, name, language, cues))
        val currentTracks = _state.value.tracks
        _state.value = _state.value.copy(
            tracks = currentTracks + SubtitleTrack(
                id = "ext_${subtitles.size}",
                name = name,
                language = language,
                isEmbedded = false,
                isExternal = true
            ),
            selectedTrackIndex = currentTracks.size,
            isEnabled = true
        )
    }

    fun selectTrack(index: Int) {
        _state.value = _state.value.copy(selectedTrackIndex = index, isEnabled = index >= 0)
    }

    fun setDelay(delayMs: Long) {
        _state.value = _state.value.copy(delayMs = delayMs)
    }

    fun toggleEnabled() {
        val current = _state.value
        _state.value = current.copy(isEnabled = !current.isEnabled)
    }

    fun updatePosition(positionMs: Long) {
        val s = _state.value
        val adjustedMs = positionMs - s.delayMs
        val cues = subtitles.getOrNull(s.selectedTrackIndex)?.cues ?: emptyList()
        val active = cues.find { adjustedMs in it.startMs until it.endMs }
        if (_state.value.currentText != active?.text) {
            _state.value = s.copy(currentText = active?.text)
        }
    }

    private data class SubtitleEntry(
        val uri: String,
        val name: String,
        val language: String?,
        val cues: List<SubtitleCue>
    )

    data class SubtitleCue(
        val startMs: Long,
        val endMs: Long,
        val text: String
    )
}
