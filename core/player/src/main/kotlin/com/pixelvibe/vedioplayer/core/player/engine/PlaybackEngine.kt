package com.pixelvibe.vedioplayer.core.player.engine

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlaybackState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentPositionMs: Long = 0,
    val durationMs: Long = 0,
    val playbackSpeed: Float = 1f,
    val error: String? = null,
    val isFinished: Boolean = false
)

class PlaybackEngine(private val context: Context) {

    private val _state = MutableStateFlow(PlaybackState())
    open val state: StateFlow<PlaybackState> = _state.asStateFlow()

    open val audioSessionId: Int get() = player.audioSessionId
    open val playerRef: Player get() = player

    private val player: ExoPlayer by lazy {
        ExoPlayer.Builder(context)
            .build()
            .apply {
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _state.value = _state.value.copy(isPlaying = isPlaying)
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        _state.value = _state.value.copy(
                            isBuffering = playbackState == Player.STATE_BUFFERING,
                            isFinished = playbackState == Player.STATE_ENDED
                        )
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        _state.value = _state.value.copy(error = error.localizedMessage)
                    }
                })
            }
    }

    private var positionUpdateJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    open fun play(uri: String) {
        val mediaItem = MediaItem.fromUri(uri)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
        startPositionUpdates()
    }

    open fun play(mediaItems: List<String>, startIndex: Int = 0) {
        val items = mediaItems.map { MediaItem.fromUri(it) }
        player.setMediaItems(items, startIndex, 0)
        player.prepare()
        player.play()
        startPositionUpdates()
    }

    open fun togglePlay() {
        if (player.isPlaying) player.pause() else player.play()
    }

    open fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
    }

    open fun setSpeed(speed: Float) {
        player.playbackParameters = PlaybackParameters(speed)
        _state.value = _state.value.copy(playbackSpeed = speed)
    }

    open fun setVolume(volume: Float) {
        player.volume = volume.coerceIn(0f, 1f)
    }

    open fun stepForward() {
        val fps = player.videoFormat?.frameRate ?: 60f
        val stepMs = (1000f / fps).toLong().coerceAtLeast(1)
        player.seekTo(player.currentPosition + stepMs)
    }

    open fun stepBackward() {
        val fps = player.videoFormat?.frameRate ?: 60f
        val stepMs = (1000f / fps).toLong().coerceAtLeast(1)
        player.seekTo(maxOf(0, player.currentPosition - stepMs))
    }

    open val currentPosition: Long get() = player.currentPosition
    open val duration: Long get() = if (player.duration > 0) player.duration else 0

    open fun release() {
        positionUpdateJob?.cancel()
        player.release()
    }

    private fun startPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = scope.launch {
            while (true) {
                delay(250)
                try {
                    _state.value = _state.value.copy(
                        currentPositionMs = player.currentPosition,
                        durationMs = if (player.duration > 0) player.duration else 0
                    )
                } catch (_: Exception) {
                    break
                }
            }
        }
    }
}
