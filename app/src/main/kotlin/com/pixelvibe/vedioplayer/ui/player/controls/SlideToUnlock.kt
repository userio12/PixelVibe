package com.pixelvibe.vedioplayer.ui.player.controls

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun SlideToUnlock(
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val iconSize = 48.dp
    val iconSizePx = with(density) { iconSize.toPx() }
    val paddingPx = with(density) { 16.dp.toPx() }

    var offsetX by remember { mutableFloatStateOf(0f) }
    val maxX = remember { mutableFloatStateOf(0f) }
    val animatable = remember { Animatable(0f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(32.dp))
            .padding(horizontal = 16.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { /* nothing */ },
                    onDragEnd = {
                        // Snap back if not fully slid
                        if (offsetX < maxX.value * 0.7f) {
                            offsetX = 0f
                        }
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        val newX = (offsetX + dragAmount).coerceIn(0f, maxX.value)
                        offsetX = newX
                        if (newX >= maxX.value * 0.9f) {
                            onUnlock()
                            offsetX = 0f
                        }
                    }
                )
            },
        contentAlignment = Alignment.CenterStart
    ) {
        // Track background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(
                    Color.White.copy(alpha = 0.1f),
                    RoundedCornerShape(24.dp)
                )
        )

        // Sliding icon
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .size(iconSize)
                .background(
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(24.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.LockOpen,
                contentDescription = "Slide to unlock",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(24.dp)
            )
        }

        // Hint text
        androidx.compose.material3.Text(
            text = "Slide to unlock",
            modifier = Modifier
                .align(Alignment.Center)
                .alpha(if (offsetX > 0) 0.5f else 1f),
            color = Color.White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodyMedium
        )
    }

    LaunchedEffect(Unit) {
        maxX.value = with(density) { 300.dp.toPx() }
    }
}
