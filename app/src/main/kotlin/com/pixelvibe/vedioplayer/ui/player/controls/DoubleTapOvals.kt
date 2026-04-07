package com.pixelvibe.vedioplayer.ui.player.controls

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Visual indicator shown during double-tap seek gestures.
 * Shows an oval overlay with arrow icon and seek time delta.
 */
@Composable
fun DoubleTapSeekOvals(
    visible: Boolean,
    side: SeekSide,
    seekTime: Double,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(100.dp)
                    .alpha(0.3f)
            ) {
                val ovalSize = Size(size.width * 0.8f, size.height * 0.4f)
                val ovalOffset = Offset(
                    x = (size.width - ovalSize.width) / 2,
                    y = (size.height - ovalSize.height) / 2
                )

                drawOval(
                    color = Color.White,
                    topLeft = ovalOffset,
                    size = ovalSize,
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            androidx.compose.foundation.layout.Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = when (side) {
                        SeekSide.LEFT -> Icons.Default.ArrowBackIosNew
                        SeekSide.RIGHT -> Icons.AutoMirrored.Default.ArrowForwardIos
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.alpha(0.8f)
                )

                Text(
                    text = "${if (seekTime < 0) "" else "+"}${String.format("%.0f", seekTime)}s",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

enum class SeekSide { LEFT, RIGHT }

/**
 * Full-screen overlay showing left/right seek ovals during double-tap.
 */
@Composable
fun DoubleTapSeekOverlay(
    showLeft: Boolean,
    showRight: Boolean,
    leftSeekTime: Double,
    rightSeekTime: Double,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Left oval
        DoubleTapSeekOvals(
            visible = showLeft,
            side = SeekSide.LEFT,
            seekTime = leftSeekTime,
            modifier = Modifier.align(Alignment.CenterStart)
        )

        // Right oval
        DoubleTapSeekOvals(
            visible = showRight,
            side = SeekSide.RIGHT,
            seekTime = rightSeekTime,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}
