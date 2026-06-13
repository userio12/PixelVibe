package com.pixelvibe.vedioplayer.core.player.controller

import androidx.media3.common.Player
import com.pixelvibe.vedioplayer.core.player.engine.PlaybackEngine
import com.pixelvibe.vedioplayer.core.player.engine.PlaybackState
import kotlinx.coroutines.flow.StateFlow

open class PlayerController(private val engine: PlaybackEngine) {

    open val state: StateFlow<PlaybackState> = engine.state

    open fun play(uri: String) = engine.play(uri)
    open fun playAll(uris: List<String>, startIndex: Int = 0) = engine.play(uris, startIndex)
    open fun togglePlay() = engine.togglePlay()
    open fun seekTo(positionMs: Long) = engine.seekTo(positionMs)
    open fun setSpeed(speed: Float) = engine.setSpeed(speed)
    open fun setVolume(volume: Float) = engine.setVolume(volume)
    open fun stepForward() = engine.stepForward()
    open fun stepBackward() = engine.stepBackward()

    open val currentPosition: Long get() = engine.currentPosition
    open val duration: Long get() = engine.duration
    open val audioSessionId: Int get() = engine.audioSessionId
    open val player: Player get() = engine.playerRef

    open fun release() = engine.release()
}
