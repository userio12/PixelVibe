package com.pixelvibe.vedioplayer.ui.player

import androidx.lifecycle.ViewModel
import com.pixelvibe.vedioplayer.database.entities.PlaybackStateEntity
import com.pixelvibe.vedioplayer.database.repositories.PlaybackHistoryRepository
import com.pixelvibe.vedioplayer.domain.AudioTrack
import com.pixelvibe.vedioplayer.domain.SubtitleTrack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Sealed class representing repeat modes.
 */
enum class RepeatMode {
    OFF, ONE, ALL
}

/**
 * Player UI state — pure UI state with no video engine dependencies.
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
    val isLoading: Boolean = false,
    val overlayMessage: String? = null,
    val subtitleTracks: List<SubtitleTrack> = emptyList(),
    val audioTracks: List<AudioTrack> = emptyList()
)

/**
 * Player ViewModel — manages UI state without any video engine dependency.
 */
class PlayerViewModel(
    private val playbackHistoryRepository: PlaybackHistoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    // ── Playback ──

    fun play() = _state.update { it.copy(isPlaying = true, isPaused = false) }
    fun pause() = _state.update { it.copy(isPlaying = false, isPaused = true) }
    fun togglePlayback() = _state.update { it.copy(isPlaying = !it.isPlaying, isPaused = it.isPlaying) }

    fun seek(position: Double) = _state.update { it.copy(currentPosition = position.coerceIn(0.0, it.duration)) }
    fun seekRelative(seconds: Double) = _state.update { it.copy(currentPosition = (it.currentPosition + seconds).coerceIn(0.0, it.duration)) }

    fun setSpeed(speed: Double) = _state.update { it.copy(playbackSpeed = speed.coerceIn(0.25, 4.0)) }
    fun cycleSpeed() {
        val speeds = listOf(0.25, 0.5, 0.75, 1.0, 1.25, 1.5, 1.75, 2.0, 2.5, 3.0, 4.0)
        val current = _state.value.playbackSpeed
        val nextIndex = speeds.indexOfFirst { it > current }.takeIf { it >= 0 } ?: 0
        setSpeed(speeds[nextIndex])
        showOverlayMessage("Speed: ${speeds[nextIndex]}x")
    }

    // ── Playlist ──

    fun nextVideo() = showOverlayMessage("Next")
    fun prevVideo() = showOverlayMessage("Previous")

    // ── Repeat / Shuffle ──

    fun cycleRepeatMode() {
        val next = when (_state.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.OFF
        }
        _state.update { it.copy(repeatMode = next) }
        showOverlayMessage("Repeat: ${next.name.lowercase()}")
    }

    fun toggleShuffle() {
        val shuffled = !_state.value.isShuffled
        _state.update { it.copy(isShuffled = shuffled) }
        showOverlayMessage("Shuffle: ${if (shuffled) "on" else "off"}")
    }

    // ── A-B Loop ──

    fun setLoopPointA() {
        val pos = _state.value.currentPosition
        _state.update { it.copy(aLoopStart = pos) }
        showOverlayMessage("Loop A: ${formatTime(pos)}")
    }

    fun setLoopPointB() {
        val pos = _state.value.currentPosition
        _state.update { it.copy(aLoopEnd = pos) }
        showOverlayMessage("Loop B: ${formatTime(pos)}")
    }

    fun clearLoopPoints() = _state.update { it.copy(aLoopStart = null, aLoopEnd = null) }

    // ── Video Display ──

    fun cycleVideoZoom() {
        val next = (_state.value.zoomLevel + 1) % 5
        _state.update { it.copy(zoomLevel = next) }
        showOverlayMessage("Zoom: ${next * 50 + 50}%")
    }

    fun cycleAspectRatio() {
        val ratios = listOf("fit", "crop", "stretch", "16:9", "4:3", "1:1", "21:9")
        val current = _state.value.aspectRatio
        val nextIndex = (ratios.indexOf(current) + 1) % ratios.size
        _state.update { it.copy(aspectRatio = ratios[nextIndex]) }
        showOverlayMessage("Aspect: ${ratios[nextIndex]}")
    }

    fun toggleMirror() = _state.update { it.copy(isMirrored = !it.isMirrored) }
    fun toggleVerticalFlip() = _state.update { it.copy(isVerticallyFlipped = !it.isVerticallyFlipped) }

    // ── Subtitles ──

    fun setSubtitleTrack(trackId: Int) {
        showOverlayMessage("Subtitle: ${if (trackId == -1) "Off" else "#$trackId"}")
    }

    fun setSecondarySubtitleTrack(trackId: Int) {
        showOverlayMessage("Secondary Subtitle: ${if (trackId == -1) "Off" else "#$trackId"}")
    }

    fun setSubtitleDelay(delayMs: Float) = showOverlayMessage("Subtitle delay: ${delayMs.toInt()}ms")
    fun setSecondarySubtitleDelay(delayMs: Float) = showOverlayMessage("Secondary delay: ${delayMs.toInt()}ms")
    fun updateSubtitleTracks(tracks: List<SubtitleTrack>) = _state.update { it.copy(subtitleTracks = tracks) }

    // ── Audio ──

    fun setAudioTrack(trackId: Int) = showOverlayMessage("Audio: ${if (trackId == -1) "Off" else "#$trackId"}")
    fun setAudioDelay(delayMs: Float) = showOverlayMessage("Audio delay: ${delayMs.toInt()}ms")
    fun updateAudioTracks(tracks: List<AudioTrack>) = _state.update { it.copy(audioTracks = tracks) }

    // ── Controls ──

    fun showControls() = _state.update { it.copy(isControlsVisible = true) }
    fun hideControls() = _state.update { it.copy(isControlsVisible = false) }
    fun toggleControls() = _state.update { it.copy(isControlsVisible = !it.isControlsVisible) }
    fun toggleLock() = _state.update { it.copy(isLocked = !it.isLocked, isControlsVisible = if (it.isLocked) true else false) }

    // ── Overlay ──

    fun showOverlayMessage(message: String, durationMs: Long = 1500L) = _state.update { it.copy(overlayMessage = message) }
    fun clearOverlayMessage() = _state.update { it.copy(overlayMessage = null) }
    fun setLoading(isLoading: Boolean) = _state.update { it.copy(isLoading = isLoading) }

    // ── Persistence ──

    fun savePlaybackState() {
        // TODO: Implement when video engine is added
    }

    fun restorePlaybackState(mediaTitle: String) {
        // TODO: Implement when video engine is added
    }

    // ── Utility ──

    companion object {
        fun formatTime(seconds: Double): String {
            if (seconds < 0 || seconds.isNaN() || seconds.isInfinite()) return "--:--"
            val totalSeconds = seconds.toLong()
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val secs = totalSeconds % 60
            return if (hours > 0) String.format("%d:%02d:%02d", hours, minutes, secs)
            else String.format("%02d:%02d", minutes, secs)
        }
    }
}
