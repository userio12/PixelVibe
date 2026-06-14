package com.pixelvibe.vedioplayer.core.player.engine

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.datasource.DataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
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

open class PlaybackEngine(
    private val context: Context,
    private val dataSourceFactory: DataSource.Factory? = null
) {

    private val _state = MutableStateFlow(PlaybackState())
    open val state: StateFlow<PlaybackState> = _state.asStateFlow()

    open val audioSessionId: Int get() = player!!.audioSessionId
    open val playerRef: Player get() = player!!

    private var isReleased = false
    private var player: ExoPlayer? = null

    private var positionUpdateJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private fun ensurePlayer(): ExoPlayer {
        val existing = player
        if (existing != null && !isReleased) return existing
        val builder = ExoPlayer.Builder(context)
        if (dataSourceFactory != null) {
            builder.setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
        }
        val newPlayer = builder.build().apply {
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
        player = newPlayer
        isReleased = false
        return newPlayer
    }

    open fun play(uri: String) {
        _state.value = _state.value.copy(error = null)
        ensurePlayer().let { p ->
            val mediaItem = MediaItem.fromUri(uri)
            p.setMediaItem(mediaItem)
            p.prepare()
            p.play()
            startPositionUpdates()
        }
    }

    open fun play(mediaItems: List<String>, startIndex: Int = 0) {
        _state.value = _state.value.copy(error = null)
        ensurePlayer().let { p ->
            val items = mediaItems.map { MediaItem.fromUri(it) }
            p.setMediaItems(items, startIndex, 0)
            p.prepare()
            p.play()
            startPositionUpdates()
        }
    }

    open fun togglePlay() {
        ensurePlayer().let { p ->
            if (p.isPlaying) p.pause() else p.play()
        }
    }

    open fun seekTo(positionMs: Long) {
        ensurePlayer().seekTo(positionMs)
    }

    open fun setSpeed(speed: Float) {
        ensurePlayer().playbackParameters = PlaybackParameters(speed)
        _state.value = _state.value.copy(playbackSpeed = speed)
    }

    open fun setVolume(volume: Float) {
        ensurePlayer().volume = volume.coerceIn(0f, 1f)
    }

    open fun stepForward() {
        val p = ensurePlayer()
        val pos = p.currentPosition
        if (pos == C.TIME_UNSET) return
        val fps = p.videoFormat?.frameRate ?: 60f
        val stepMs = (1000f / fps).toLong().coerceAtLeast(1)
        p.seekTo(pos + stepMs)
    }

    open fun stepBackward() {
        val p = ensurePlayer()
        val pos = p.currentPosition
        if (pos == C.TIME_UNSET) return
        val fps = p.videoFormat?.frameRate ?: 60f
        val stepMs = (1000f / fps).toLong().coerceAtLeast(1)
        p.seekTo(maxOf(0, pos - stepMs))
    }

    open val currentPosition: Long get() {
        val pos = player?.currentPosition ?: return 0
        return if (pos == C.TIME_UNSET) 0 else pos
    }

    open val duration: Long get() {
        val dur = player?.duration ?: return 0
        return if (dur > 0 && dur != C.TIME_UNSET) dur else 0
    }

    open fun release() {
        isReleased = true
        positionUpdateJob?.cancel()
        scope.cancel()
        player?.release()
        player = null
    }

    private fun startPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = scope.launch {
            while (true) {
                delay(250)
                try {
                    _state.value = _state.value.copy(
                        currentPositionMs = player?.currentPosition?.let {
                            if (it == C.TIME_UNSET) 0 else it
                        } ?: 0,
                        durationMs = player?.duration?.let {
                            if (it > 0 && it != C.TIME_UNSET) it else 0
                        } ?: 0
                    )
                } catch (_: Exception) {
                    break
                }
            }
        }
    }
}
