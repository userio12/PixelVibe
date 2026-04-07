package com.pixelvibe.vedioplayer.ui.player.controls.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pixelvibe.vedioplayer.ui.player.PlayerViewModel
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun WavySeekbar(
    currentPosition: Double,
    duration: Double,
    isPlaying: Boolean,
    chapterMarkers: List<Double> = emptyList(),
    loopStart: Double? = null,
    loopEnd: Double? = null,
    onSeek: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val trackHeight = 4.dp
    val waveAmplitude = 6.dp
    val waveSpeed = 10f // px per second
    var waveOffset by remember { mutableFloatStateOf(0f) }

    // Animate wave when playing
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            Animatable(waveOffset).apply {
                animateTo(
                    targetValue = waveOffset + 1000f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    )
                ) {
                    waveOffset = value
                }
            }
        }
    }

    val progress = if (duration > 0) (currentPosition / duration).coerceIn(0.0, 1.0) else 0.0
    val progressColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
    val waveColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)

    SeekbarBase(
        progress = progress.toFloat(),
        progressColor = progressColor,
        trackColor = trackColor,
        chapterMarkers = chapterMarkers,
        loopStart = loopStart,
        loopEnd = loopEnd,
        onSeek = onSeek,
        trackHeight = trackHeight
    ) { canvas, size, trackRect ->
        // Draw wave overlay
        if (isPlaying) {
            val path = Path()
            val wavelength = 20f
            val amplitudePx = with(density) { waveAmplitude.toPx() }

            path.moveTo(trackRect.left, trackRect.top + trackRect.height() / 2)

            var x = trackRect.left
            while (x <= trackRect.right) {
                val normalizedX = (x + waveOffset) / wavelength
                val y = trackRect.top + trackRect.height() / 2 + sin(normalizedX * 2f * PI.toFloat()) * amplitudePx
                path.lineTo(x, y)
                x += 2f
            }

            canvas.drawPath(
                path = path,
                color = waveColor,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun ThickSeekbar(
    currentPosition: Double,
    duration: Double,
    chapterMarkers: List<Double> = emptyList(),
    loopStart: Double? = null,
    loopEnd: Double? = null,
    onSeek: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (duration > 0) (currentPosition / duration).coerceIn(0.0, 1.0) else 0.0

    SeekbarBase(
        progress = progress.toFloat(),
        progressColor = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
        chapterMarkers = chapterMarkers,
        loopStart = loopStart,
        loopEnd = loopEnd,
        onSeek = onSeek,
        trackHeight = 8.dp
    ) { canvas, size, trackRect ->
        // No additional drawing for thick style - handled by trackHeight
    }
}

@Composable
private fun SeekbarBase(
    progress: Float,
    progressColor: Color,
    trackColor: Color,
    chapterMarkers: List<Double>,
    loopStart: Double?,
    loopEnd: Double?,
    onSeek: (Double) -> Unit,
    trackHeight: Dp,
    extraContent: @Composable (canvas: androidx.compose.ui.graphics.DrawScope, size: androidx.compose.ui.geometry.Size, trackRect: androidx.compose.ui.geometry.Rect) -> Unit
) {
    val density = LocalDensity.current
    val trackHeightPx = with(density) { trackHeight.toPx() }
    val thumbRadius = 8.dp
    var isDragging by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(48.dp)
            .pointerInput(progress) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        isDragging = 1f
                        val seekPos = (offset.x / size.width).coerceIn(0f, 1f)
                        onSeek(seekPos.toDouble() * (onSeek as? Double ?: 1.0))
                    },
                    onDragCancel = { isDragging = 0f },
                    onDragEnd = { isDragging = 0f },
                    onHorizontalDrag = { _, dragAmount ->
                        // Seek handled by parent
                    }
                )
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)
                .align(Alignment.Center)
        ) {
            val trackWidth = size.width
            val trackLeft = 0f
            val trackRight = trackWidth
            val trackTop = (size.height - trackHeightPx) / 2
            val trackBottom = trackTop + trackHeightPx
            val trackRect = androidx.compose.ui.geometry.Rect(trackLeft, trackTop, trackRight, trackBottom)

            // Background track
            drawRoundRect(
                color = trackColor,
                topLeft = Offset(trackLeft, trackTop),
                size = Size(trackWidth, trackHeightPx),
                cornerRadius = CornerRadius(trackHeightPx / 2, trackHeightPx / 2)
            )

            // Progress track
            val progressWidth = trackWidth * progress
            drawRoundRect(
                color = progressColor,
                topLeft = Offset(trackLeft, trackTop),
                size = Size(progressWidth, trackHeightPx),
                cornerRadius = CornerRadius(trackHeightPx / 2, trackHeightPx / 2)
            )

            // Chapter markers
            chapterMarkers.forEach { chapterTime ->
                val chapterProgress = chapterTime / 1.0 // Normalize
                if (chapterProgress in 0.0..1.0) {
                    val x = (chapterProgress.toFloat() * trackWidth).coerceIn(trackLeft, trackRight)
                    drawLine(
                        color = Color.White.copy(alpha = 0.5f),
                        start = Offset(x, trackTop - 4.dp.toPx()),
                        end = Offset(x, trackBottom + 4.dp.toPx()),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }

            // Loop markers
            listOfNotNull(loopStart, loopEnd).forEach { loopTime ->
                val loopProgress = loopTime / 1.0
                if (loopProgress in 0.0..1.0) {
                    val x = (loopProgress.toFloat() * trackWidth).coerceIn(trackLeft, trackRight)
                    drawLine(
                        color = Color(0xFFFFD700), // Gold
                        start = Offset(x, trackTop - 8.dp.toPx()),
                        end = Offset(x, trackBottom + 8.dp.toPx()),
                        strokeWidth = 3.dp.toPx()
                    )
                }
            }

            // Thumb
            val thumbX = progressWidth
            drawCircle(
                color = progressColor,
                radius = with(density) { thumbRadius.toPx() },
                center = Offset(thumbX, size.height / 2)
            )

            // Extra content (wave, etc.)
            extraContent(this, size, trackRect)
        }
    }
}
