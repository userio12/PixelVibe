package com.pixelvibe.vedioplayer.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelvibe.vedioplayer.database.entities.PlaybackStateEntity
import com.pixelvibe.vedioplayer.database.repositories.PlaybackHistoryRepository
import com.pixelvibe.vedioplayer.domain.AudioTrack
import com.pixelvibe.vedioplayer.domain.SubtitleManager
import com.pixelvibe.vedioplayer.domain.SubtitleTrack
import com.pixelvibe.vedioplayer.preferences.PlayerPreferences
import is.xyz.mpv.MPVLib
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Sealed class representing repeat modes.
 */
enum class RepeatMode {
    OFF, ONE, ALL
}

/**
 * Sealed class representing player overlay state.
 */
data class PlayerState(
    val isPlaying: Boolean = false,
    val isPaused: Boolean = true,
    val isBuffering: Boolean = false,
    val isEofReached: Boolean = false,
    val currentPosition: Double = 0.0,
    val duration: Double = 0.0,
    val playbackSpeed: Double = 1.0,
    val volume: Float = 1.0f,
    val brightness: Float = -1f,
    val videoWidth: Int = 0,
    val videoHeight: Int = 0,
    val videoAspect: Double = 0.0,
    val mediaTitle: String = "",
    val filePath: String = "",
    val chapter: Int = 0,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val isShuffled: Boolean = false,
    val isControlsVisible: Boolean = true,
    val isLocked: Boolean = false,
    val zoomLevel: Int = 0,
    val aspectRatio: String = "fit",
    val isMirrored: Boolean = false,
    val isVerticallyFlipped: Boolean = false,
    val aLoopStart: Double? = null,
    val aLoopEnd: Double? = null,
    val isLoading: Boolean = true,
    val overlayMessage: String? = null,
    // Subtitle tracks
    val subtitleTracks: List<SubtitleTrack> = emptyList(),
    val audioTracks: List<AudioTrack> = emptyList()
)

class PlayerViewModel(
    private val preferences: PlayerPreferences,
    private val playbackHistoryRepository: PlaybackHistoryRepository,
    private val subtitleManager: SubtitleManager
) : ViewModel() {

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    // Track delegates for subtitle and audio
    var sid: Int = -1
        private set
    var secondarySid: Int = -1
        private set
    var aid: Int = -1
        private set

    /**
     * Handle MPV property changes from the observer.
     */
    fun onPropertyChange(property: String, value: Any?) {
        when (property) {
            "pause" -> {
                val paused = value as? Boolean ?: true
                _state.update { it.copy(isPaused = paused, isPlaying = !paused) }
            }
            "paused-for-cache" -> {
                val buffering = value as? Boolean ?: false
                _state.update { it.copy(isBuffering = buffering) }
            }
            "time-pos" -> {
                val pos = when (value) {
                    is Double -> value
                    is Long -> value.toDouble()
                    is Int -> value.toDouble()
                    else -> 0.0
                }
                _state.update { it.copy(currentPosition = pos) }
            }
            "duration" -> {
                val dur = when (value) {
                    is Double -> value
                    is Long -> value.toDouble()
                    is Int -> value.toDouble()
                    else -> 0.0
                }
                _state.update { it.copy(duration = dur) }
            }
            "eof-reached" -> {
                val eof = value as? Boolean ?: false
                _state.update { it.copy(isEofReached = eof) }
            }
            "video-params/aspect" -> {
                val aspect = when (value) {
                    is Double -> value
                    is Long -> value.toDouble()
                    is Int -> value.toDouble()
                    else -> 0.0
                }
                _state.update { it.copy(videoAspect = aspect) }
            }
            "video-params/w" -> {
                val w = (value as? Long)?.toInt() ?: (value as? Int) ?: 0
                _state.update { it.copy(videoWidth = w) }
            }
            "video-params/h" -> {
                val h = (value as? Long)?.toInt() ?: (value as? Int) ?: 0
                _state.update { it.copy(videoHeight = h) }
            }
            "speed" -> {
                val speed = when (value) {
                    is Double -> value
                    is Long -> value.toDouble()
                    is Int -> value.toDouble()
                    else -> 1.0
                }
                _state.update { it.copy(playbackSpeed = speed) }
            }
            "chapter" -> {
                val ch = (value as? Long)?.toInt() ?: (value as? Int) ?: 0
                _state.update { it.copy(chapter = ch) }
            }
            "media-title" -> {
                val title = value as? String ?: ""
                _state.update { it.copy(mediaTitle = title) }
            }
            "file-path" -> {
                val path = value as? String ?: ""
                _state.update { it.copy(filePath = path) }
            }
            "sid" -> {
                sid = (value as? Long)?.toInt() ?: (value as? Int) ?: -1
            }
            "aid" -> {
                aid = (value as? Long)?.toInt() ?: (value as? Int) ?: -1
            }
        }
    }

    /**
     * Handle MPV events.
     */
    fun onEvent(event: MPVLib.Event) {
        when (event) {
            MPVLib.Event.END_FILE -> {
                _state.update { it.copy(isLoading = false) }
            }
            MPVLib.PropertyChange -> {
                // Handled by property changes
            }
            else -> {}
        }
    }

    // ── Playback Commands ──

    fun play() {
        MPVLib.setPropertyBoolean("pause", false)
    }

    fun pause() {
        MPVLib.setPropertyBoolean("pause", true)
    }

    fun togglePlayback() {
        if (_state.value.isPaused) play() else pause()
    }

    fun seek(position: Double, precise: Boolean = false) {
        val abs = if (position >= 0) "absolute" else "relative"
        val preciseFlag = if (precise) "exact" else "keyframes"
        MPVLib.command(arrayOf("seek", position.toString(), abs, preciseFlag))
    }

    fun seekRelative(seconds: Double) {
        MPVLib.command(arrayOf("seek", seconds.toString(), "relative", "exact"))
    }

    fun setSpeed(speed: Double) {
        MPVLib.setPropertyDouble("speed", speed.coerceIn(0.25, 4.0))
    }

    fun cycleSpeed() {
        val speeds = listOf(0.25, 0.5, 0.75, 1.0, 1.25, 1.5, 1.75, 2.0, 2.5, 3.0, 4.0)
        val current = _state.value.playbackSpeed
        val nextIndex = speeds.indexOfFirst { it > current }.takeIf { it >= 0 } ?: 0
        setSpeed(speeds[nextIndex])
        showOverlayMessage("Speed: ${speeds[nextIndex]}x")
    }

    fun frameStep() {
        MPVLib.command(arrayOf("frame-step"))
    }

    fun frameBackStep() {
        MPVLib.command(arrayOf("frame-back-step"))
    }

    // ── Playlist Navigation ──

    fun nextVideo() {
        MPVLib.command(arrayOf("playlist-next"))
    }

    fun prevVideo() {
        MPVLib.command(arrayOf("playlist-prev"))
    }

    // ── Repeat / Shuffle ──

    fun cycleRepeatMode() {
        val next = when (_state.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.OFF
        }
        _state.update { it.copy(repeatMode = next) }
        MPVLib.setPropertyString("loop-playlist", if (next == RepeatMode.ALL) "inf" else "no")
        MPVLib.setPropertyString("loop-file", if (next == RepeatMode.ONE) "inf" else "no")
        showOverlayMessage("Repeat: ${next.name.lowercase()}")
    }

    fun toggleShuffle() {
        val shuffled = !_state.value.isShuffled
        _state.update { it.copy(isShuffled = shuffled) }
        MPVLib.setPropertyBoolean("shuffle", shuffled)
        showOverlayMessage("Shuffle: ${if (shuffled) "on" else "off"}")
    }

    // ── A-B Loop ──

    fun setLoopPointA() {
        val pos = _state.value.currentPosition
        _state.update { it.copy(aLoopStart = pos) }
        MPVLib.command(arrayOf("ab-loop", "a"))
        showOverlayMessage("Loop A: ${formatTime(pos)}")
    }

    fun setLoopPointB() {
        val pos = _state.value.currentPosition
        _state.update { it.copy(aLoopEnd = pos) }
        MPVLib.command(arrayOf("ab-loop", "b"))
        showOverlayMessage("Loop B: ${formatTime(pos)}")
    }

    fun clearLoopPoints() {
        _state.update { it.copy(aLoopStart = null, aLoopEnd = null) }
        MPVLib.command(arrayOf("ab-loop", "no"))
    }

    // ── Video Display ──

    fun cycleVideoZoom() {
        val next = (_state.value.zoomLevel + 1) % 5
        _state.update { it.copy(zoomLevel = next) }
        val values = listOf("window", "50", "100", "150", "200")
        MPVLib.setPropertyString("video-zoom", values[next])
        showOverlayMessage("Zoom: ${values[next]}%")
    }

    fun cycleAspectRatio() {
        val ratios = listOf("fit", "crop", "stretch", "16:9", "4:3", "1:1", "21:9")
        val current = _state.value.aspectRatio
        val currentIndex = ratios.indexOf(current)
        val nextIndex = (currentIndex + 1) % ratios.size
        val next = ratios[nextIndex]
        _state.update { it.copy(aspectRatio = next) }

        MPVLib.command(arrayOf("set", "video-aspect-override", next))
        showOverlayMessage("Aspect: $next")
    }

    fun toggleMirror() {
        val mirrored = !_state.value.isMirrored
        _state.update { it.copy(isMirrored = mirrored) }
        MPVLib.setPropertyBoolean("vf", mirrored) // Toggle via video filter
        showOverlayMessage("Mirror: ${if (mirrored) "on" else "off"}")
    }

    fun toggleVerticalFlip() {
        val flipped = !_state.value.isVerticallyFlipped
        _state.update { it.copy(isVerticallyFlipped = flipped) }
        showOverlayMessage("V-Flip: ${if (flipped) "on" else "off"}")
    }

    // ── Subtitle Commands ──

    fun setSubtitleTrack(trackId: Int) {
        sid = trackId
        MPVLib.setPropertyInt("sid", trackId)
        showOverlayMessage("Subtitle: ${if (trackId == -1) "Off" else "#$trackId"}")
    }

    fun setSecondarySubtitleTrack(trackId: Int) {
        secondarySid = trackId
        MPVLib.setPropertyInt("secondary-sid", trackId)
        showOverlayMessage("Secondary Subtitle: ${if (trackId == -1) "Off" else "#$trackId"}")
    }

    fun setSubtitleDelay(delayMs: Float) {
        subtitleManager.setSubtitleDelay(delayMs)
        showOverlayMessage("Subtitle delay: ${delayMs.toInt()}ms")
    }

    fun setSecondarySubtitleDelay(delayMs: Float) {
        subtitleManager.setSecondarySubtitleDelay(delayMs)
        showOverlayMessage("Secondary subtitle delay: ${delayMs.toInt()}ms")
    }

    fun loadExternalSubtitle(filePath: String, asSecondary: Boolean = false) {
        subtitleManager.loadExternalSubtitle(filePath, asSecondary)
        showOverlayMessage("Loaded: ${filePath.substringAfterLast('/')}")
    }

    fun applySubtitleTypography() {
        subtitleManager.applySubtitleTypography()
    }

    fun updateSubtitleTracks(tracks: List<SubtitleTrack>) {
        _state.update { it.copy(subtitleTracks = tracks) }
    }

    fun cycleSubtitleTrack() {
        val tracks = _state.value.subtitleTracks
        if (tracks.isEmpty()) {
            setSubtitleTrack(-1)
            return
        }
        val currentIndex = tracks.indexOfFirst { it.id == sid }
        val nextIndex = if (currentIndex < 0) 0 else (currentIndex + 1) % tracks.size
        val nextTrack = tracks[nextIndex]
        setSubtitleTrack(nextTrack.id)
    }

    // ── Audio Commands ──

    fun setAudioTrack(trackId: Int) {
        aid = trackId
        MPVLib.setPropertyInt("aid", trackId)
        showOverlayMessage("Audio: ${if (trackId == -1) "Off" else "#$trackId"}")
    }

    fun setAudioDelay(delayMs: Float) {
        MPVLib.setPropertyDouble("audio-delay", delayMs / 1000.0)
        showOverlayMessage("Audio delay: ${delayMs.toInt()}ms")
    }

    fun updateAudioTracks(tracks: List<AudioTrack>) {
        _state.update { it.copy(audioTracks = tracks) }
    }

    fun cycleAudioTrack() {
        val tracks = _state.value.audioTracks
        if (tracks.isEmpty()) {
            setAudioTrack(1)
            return
        }
        val currentIndex = tracks.indexOfFirst { it.id == aid }
        val nextIndex = if (currentIndex < 0) 0 else (currentIndex + 1) % tracks.size
        val nextTrack = tracks[nextIndex]
        setAudioTrack(nextTrack.id)
    }

    fun setVolumeBoostCap(cap: Int) {
        MPVLib.setPropertyInt("volume-max", cap)
    }

    fun setAudioChannels(channels: String) {
        val afValue = when (channels) {
            "mono" -> "channels=mono"
            "stereo" -> "channels=stereo"
            "reversed" -> "channels=stereo,pan=stereo|c1|c0"
            else -> "" // auto
        }
        if (afValue.isNotBlank()) {
            MPVLib.setPropertyString("af", afValue)
        } else {
            MPVLib.setPropertyString("af", "")
        }
        showOverlayMessage("Audio channels: $channels")
    }

    fun setVolumeNormalization(enabled: Boolean) {
        if (enabled) {
            MPVLib.setPropertyString("af", "dynaudnorm")
        } else {
            // Remove dynaudnorm from af chain
            MPVLib.setPropertyString("af", "")
        }
        showOverlayMessage("Volume normalization: ${if (enabled) "on" else "off"}")
    }

    // ── Snapshot ──

    fun takeSnapshot(includeSubtitles: Boolean = false) {
        val mode = if (includeSubtitles) "subtitles" else "video"
        MPVLib.command(arrayOf("screenshot", mode))
    }

    // ── Controls Visibility ──

    fun showControls() {
        _state.update { it.copy(isControlsVisible = true) }
    }

    fun hideControls() {
        _state.update { it.copy(isControlsVisible = false) }
    }

    fun toggleControls() {
        _state.update { it.copy(isControlsVisible = !_state.value.isControlsVisible) }
    }

    fun toggleLock() {
        _state.update {
            it.copy(
                isLocked = !_state.value.isLocked,
                isControlsVisible = if (it.isLocked) false else true
            )
        }
    }

    // ── Overlay Message ──

    fun showOverlayMessage(message: String, durationMs: Long = 1500L) {
        _state.update { it.copy(overlayMessage = message) }
        // In production, use a coroutine to clear after duration
    }

    fun clearOverlayMessage() {
        _state.update { it.copy(overlayMessage = null) }
    }

    // ── Loading State ──

    fun setLoading(isLoading: Boolean) {
        _state.update { it.copy(isLoading = isLoading) }
    }

    // ── Playback State Persistence ──

    fun savePlaybackState() {
        val s = _state.value
        if (s.mediaTitle.isBlank()) return

        viewModelScope.launch {
            val state = PlaybackStateEntity(
                mediaTitle = s.mediaTitle,
                lastPosition = s.currentPosition.toLong(),
                playbackSpeed = s.playbackSpeed.toFloat(),
                videoZoom = s.zoomLevel,
                sid = sid,
                secondarySid = secondarySid,
                aid = aid,
                hasBeenWatched = s.currentPosition > s.duration * 0.9
            )
            playbackHistoryRepository.save(state)
        }
    }

    fun restorePlaybackState(mediaTitle: String) {
        viewModelScope.launch {
            val saved = playbackHistoryRepository.getSync(mediaTitle) ?: return@launch
            sid = saved.sid
            secondarySid = saved.secondarySid
            aid = saved.aid

            // Restore position
            if (saved.lastPosition > 0) {
                seek(saved.lastPosition.toDouble(), precise = true)
            }

            // Restore speed
            if (saved.playbackSpeed != 1.0f) {
                setSpeed(saved.playbackSpeed.toDouble())
            }
        }
    }

    // ── Cleanup ──

    override fun onCleared() {
        super.onCleared()
        savePlaybackState()
    }

    // ── Utility ──

    companion object {
        fun formatTime(seconds: Double): String {
            if (seconds < 0 || seconds.isNaN() || seconds.isInfinite()) return "--:--"
            val totalSeconds = seconds.toLong()
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val secs = totalSeconds % 60
            return if (hours > 0) {
                String.format("%d:%02d:%02d", hours, minutes, secs)
            } else {
                String.format("%02d:%02d", minutes, secs)
            }
        }
    }
}
