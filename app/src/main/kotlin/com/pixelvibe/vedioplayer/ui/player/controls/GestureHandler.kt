package com.pixelvibe.vedioplayer.ui.player.controls

import android.content.Context
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntSize
import com.pixelvibe.vedioplayer.preferences.PlayerPreferences
import com.pixelvibe.vedioplayer.ui.player.PlayerViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

/**
 * Enum representing the type of gesture overlay feedback.
 */
sealed class GestureOverlayType {
    data object None : GestureOverlayType()
    data class Brightness(val value: Float) : GestureOverlayType()
    data class Volume(val value: Float, val isPercentage: Boolean) : GestureOverlayType()
    data class Speed(val value: Double) : GestureOverlayType()
    data class Seek(val currentValue: Double, val totalDuration: Double) : GestureOverlayType()
    data class Zoom(val value: Int) : GestureOverlayType()
}

/**
 * Enum representing double-tap action types.
 */
enum class DoubleTapAction {
    SEEK, PLAY_PAUSE, CUSTOM, NONE
}

/**
 * Multi-touch gesture handler for the video player.
 * Handles: double-tap seeking, vertical swipe (brightness/volume),
 * horizontal swipe seeking, long-press speed, pinch-to-zoom, pan & zoom.
 */
class GestureHandler(
    private val context: Context,
    private val preferences: PlayerPreferences,
    private val onSeek: (Double) -> Unit,
    private val onPlayPause: () -> Unit,
    private val onSpeedChange: (Double) -> Unit,
    private val onBrightnessChange: (Float) -> Unit,
    private val onVolumeChange: (Float) -> Unit,
    private val onZoomChange: (Int) -> Unit,
    private val onOverlayUpdate: (GestureOverlayType) -> Unit,
    private val getCurrentPosition: () -> Double,
    private val getDuration: () -> Double,
    private val getCurrentBrightness: () -> Float,
    private val getCurrentVolume: () -> Float,
    private val getCurrentSpeed: () -> Double,
    private val getCurrentZoom: () -> Int,
    private val isPlaying: () -> Boolean
) {
    // Double-tap state
    private var lastTapTime = 0L
    private var lastTapSide = TapSide.NONE
    private var consecutiveTaps = 0
    private var seekAccumulator = 0.0

    // Long-press state
    private var longPressJob: Job? = null
    private var originalSpeed = 1.0

    // Swipe state
    private var swipeStartY = 0f
    private var swipeStartX = 0f
    private var isSwipeActive = false
    private var swipeSide = SwipeSide.NONE
    private var initialBrightness = 0f
    private var initialVolume = 0f
    private var initialSeekPosition = 0.0

    // Pinch-to-zoom state
    private var initialZoom = 0
    private var initialSpan = 0f

    // Pan & zoom state
    private var isPanActive = false
    private var panOffsetX = 0f
    private var panOffsetY = 0f

    private enum class TapSide { LEFT, CENTER, RIGHT, NONE }
    private enum class SwipeSide { LEFT, RIGHT, NONE }

    /**
     * Determine which third of the screen a tap is in.
     */
    private fun getTapSide(x: Float, width: Int): TapSide {
        val third = width / 3
        return when {
            x < third -> TapSide.LEFT
            x < third * 2 -> TapSide.CENTER
            else -> TapSide.RIGHT
        }
    }

    /**
     * Determine left/right side for swipe gestures.
     */
    private fun getSwipeSide(x: Float, width: Int): SwipeSide {
        return if (x < width / 2) SwipeSide.LEFT else SwipeSide.RIGHT
    }

    /**
     * Get the configured double-tap action for a given side.
     */
    private fun getDoubleTapAction(side: TapSide): DoubleTapAction {
        return when (side) {
            TapSide.LEFT -> when (preferences.doubleTapLeft.get()) {
                "seek" -> DoubleTapAction.SEEK
                "play_pause" -> DoubleTapAction.PLAY_PAUSE
                "custom" -> DoubleTapAction.CUSTOM
                else -> DoubleTapAction.SEEK
            }
            TapSide.CENTER -> when (preferences.doubleTapCenter.get()) {
                "seek" -> DoubleTapAction.SEEK
                "play_pause" -> DoubleTapAction.PLAY_PAUSE
                "custom" -> DoubleTapAction.CUSTOM
                else -> DoubleTapAction.PLAY_PAUSE
            }
            TapSide.RIGHT -> when (preferences.doubleTapRight.get()) {
                "seek" -> DoubleTapAction.SEEK
                "play_pause" -> DoubleTapAction.PLAY_PAUSE
                "custom" -> DoubleTapAction.CUSTOM
                else -> DoubleTapAction.SEEK
            }
            TapSide.NONE -> DoubleTapAction.NONE
        }
    }

    /**
     * Handle a tap event (double-tap detection with multi-tap).
     */
    fun onTap(position: Offset, size: IntSize) {
        val currentTime = System.currentTimeMillis()
        val side = getTapSide(position.x, size.width)

        if (currentTime - lastTapTime < 300 && side == lastTapSide) {
            // Multi-tap: continue seeking in same direction
            consecutiveTaps++
            val seekDuration = preferences.doubleTapSeekDuration.get() * consecutiveTaps.toDouble()
            val seekAmount = if (side == TapSide.LEFT) -seekDuration else seekDuration
            seekAccumulator += seekAmount
            onSeek(seekAccumulator)
            onOverlayUpdate(GestureOverlayType.Seek(seekAccumulator, getDuration()))
        } else if (currentTime - lastTapTime < 300) {
            // Double-tap
            consecutiveTaps = 1
            lastTapSide = side
            val action = getDoubleTapAction(side)

            when (action) {
                DoubleTapAction.SEEK -> {
                    val seekDuration = preferences.doubleTapSeekDuration.get().toDouble()
                    val seekAmount = if (side == TapSide.LEFT) -seekDuration else seekDuration
                    seekAccumulator = seekAmount
                    onSeek(seekAmount)
                    onOverlayUpdate(GestureOverlayType.Seek(seekAmount, getDuration()))
                }
                DoubleTapAction.PLAY_PAUSE -> onPlayPause()
                DoubleTapAction.CUSTOM -> { /* Custom action via input.conf */ }
                DoubleTapAction.NONE -> {}
            }
        } else {
            // Single tap (may be first of double-tap)
            consecutiveTaps = 0
            lastTapSide = side
            seekAccumulator = 0.0
        }

        lastTapTime = currentTime
    }

    /**
     * Handle long-press start for speed-up gesture.
     */
    fun onLongPressStart(
        coroutineScope: kotlinx.coroutines.CoroutineScope,
        hapticFeedback: androidx.compose.ui.hapticfeedback.HapticFeedback
    ) {
        if (!preferences.dynamicSpeedOverlay.get()) return

        originalSpeed = getCurrentSpeed()
        val newSpeed = (originalSpeed * 2.0).coerceIn(0.25, 4.0)
        onSpeedChange(newSpeed)
        onOverlayUpdate(GestureOverlayType.Speed(newSpeed))
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    /**
     * Handle long-press end to restore original speed.
     */
    fun onLongPressEnd() {
        if (!preferences.dynamicSpeedOverlay.get()) return
        onSpeedChange(originalSpeed)
        onOverlayUpdate(GestureOverlayType.None)
    }

    /**
     * Handle vertical swipe start for brightness/volume.
     */
    fun onVerticalSwipeStart(position: Offset, size: IntSize) {
        swipeStartY = position.y
        swipeStartX = position.x
        isSwipeActive = false
        swipeSide = getSwipeSide(position.x, size.width)

        initialBrightness = getCurrentBrightness()
        initialVolume = getCurrentVolume()
    }

    /**
     * Handle vertical swipe drag for brightness/volume.
     */
    fun onVerticalSwipeDrag(deltaY: Float, size: IntSize, hapticFeedback: androidx.compose.ui.hapticfeedback.HapticFeedback) {
        val useGestureBrightness = preferences.gestureBrightness.get()
        val useGestureVolume = preferences.gestureVolume.get()
        val sideSwap = preferences.mediaControlSideSwap.get()

        // Determine effective side (with swap)
        val effectiveSide = if (sideSwap) {
            if (swipeSide == SwipeSide.LEFT) SwipeSide.RIGHT else SwipeSide.LEFT
        } else {
            swipeSide
        }

        val sensitivity = 0.003f
        val delta = -deltaY * sensitivity

        when (effectiveSide) {
            SwipeSide.LEFT -> {
                if (useGestureBrightness) {
                    val newBrightness = (initialBrightness + delta).coerceIn(0f, 1f)
                    initialBrightness = newBrightness
                    onBrightnessChange(newBrightness)
                    onOverlayUpdate(GestureOverlayType.Brightness(newBrightness))
                }
            }
            SwipeSide.RIGHT -> {
                if (useGestureVolume) {
                    val newVolume = (initialVolume + delta).coerceIn(0f, 1f)
                    initialVolume = newVolume
                    onVolumeChange(newVolume)
                    onOverlayUpdate(GestureOverlayType.Volume(newVolume, preferences.displayVolumeAsPercentage.get()))
                }
            }
            SwipeSide.NONE -> {}
        }

        isSwipeActive = true
    }

    /**
     * Handle horizontal swipe for seeking.
     */
    fun onHorizontalSwipeDrag(deltaX: Float) {
        if (!preferences.horizontalSwipeToSeek.get()) return

        val sensitivity = preferences.horizontalSwipeSensitivity.get()
        val duration = getDuration()
        if (duration <= 0) return

        val seekAmount = (deltaX * sensitivity * 0.001) * duration
        val newPosition = (getCurrentPosition() + seekAmount).coerceIn(0.0, duration)
        onSeek(newPosition)
        onOverlayUpdate(GestureOverlayType.Seek(newPosition, duration))
    }

    /**
     * Handle pinch-to-zoom gesture.
     */
    fun onPinchToZoom(zoomDelta: Float) {
        if (!preferences.gesturePinchToZoom.get()) return

        val currentZoom = getCurrentZoom()
        val zoomChange = (zoomDelta * 10).toInt()
        val newZoom = (currentZoom + zoomChange).coerceIn(-2, 3) // -2 to 3 range
        if (newZoom != currentZoom) {
            onZoomChange(newZoom)
            onOverlayUpdate(GestureOverlayType.Zoom(newZoom))
        }
    }

    /**
     * Reset gesture overlay after a delay.
     */
    fun resetOverlay(coroutineScope: kotlinx.coroutines.CoroutineScope) {
        coroutineScope.launch {
            delay(1500)
            onOverlayUpdate(GestureOverlayType.None)
        }
    }
}

/**
 * Modifier extension that applies the full gesture handler to the player overlay.
 */
@Composable
fun Modifier.playerGestures(
    viewModel: PlayerViewModel,
    preferences: PlayerPreferences,
    onSeek: (Double) -> Unit,
    onPlayPause: () -> Unit,
    onSpeedChange: (Double) -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onZoomChange: (Int) -> Unit,
    onOverlayUpdate: (GestureOverlayType) -> Unit,
    getCurrentPosition: () -> Double,
    getDuration: () -> Double,
    getCurrentBrightness: () -> Float,
    getCurrentVolume: () -> Float,
    getCurrentSpeed: () -> Double,
    getCurrentZoom: () -> Int,
    isPlaying: () -> Boolean
): Modifier = composed {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val scope = androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycleScope

    val gestureHandler = remember {
        GestureHandler(
            context = context,
            preferences = preferences,
            onSeek = onSeek,
            onPlayPause = onPlayPause,
            onSpeedChange = onSpeedChange,
            onBrightnessChange = onBrightnessChange,
            onVolumeChange = onVolumeChange,
            onZoomChange = onZoomChange,
            onOverlayUpdate = onOverlayUpdate,
            getCurrentPosition = getCurrentPosition,
            getDuration = getDuration,
            getCurrentBrightness = getCurrentBrightness,
            getCurrentVolume = getCurrentVolume,
            getCurrentSpeed = getCurrentSpeed,
            getCurrentZoom = getCurrentZoom,
            isPlaying = isPlaying
        )
    }

    var zoomLevel by remember { mutableFloatStateOf(1f) }
    var zoomOffset by remember { mutableStateOf(Offset.Zero) }

    this
        // Tap gestures (single, double, multi-tap)
        .pointerInput(Unit) {
            detectTapGestures(
                onTap = { offset ->
                    gestureHandler.onTap(offset, size)
                },
                onLongPress = {
                    gestureHandler.onLongPressStart(scope, hapticFeedback)
                },
                onLongPressTimeout = {
                    gestureHandler.onLongPressEnd()
                }
            )
        }
        // Vertical swipe (brightness/volume)
        .pointerInput(Unit) {
            detectVerticalDragGestures(
                onDragStart = { offset ->
                    gestureHandler.onVerticalSwipeStart(offset, size)
                },
                onVerticalDrag = { change, dragAmount ->
                    gestureHandler.onVerticalSwipeDrag(dragAmount, size, hapticFeedback)
                    change.consume()
                }
            )
        }
        // Horizontal swipe (seek)
        .pointerInput(Unit) {
            detectHorizontalDragGestures(
                onDragStart = { /* nothing */ },
                onHorizontalDrag = { change, dragAmount ->
                    gestureHandler.onHorizontalSwipeDrag(dragAmount)
                    change.consume()
                }
            )
        }
        // Pinch-to-zoom
        .pointerInput(Unit) {
            detectTransformGestures(
                onGesture = { centroid, pan, zoom, rotation ->
                    gestureHandler.onPinchToZoom(zoom)
                }
            )
        }
        // Apply zoom transform
        .graphicsLayer(
            scaleX = zoomLevel,
            scaleY = zoomLevel,
            translationX = zoomOffset.x,
            translationY = zoomOffset.y
        )
}
