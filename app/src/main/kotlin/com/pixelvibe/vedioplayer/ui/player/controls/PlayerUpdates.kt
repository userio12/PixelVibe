package com.pixelvibe.vedioplayer.ui.player.controls

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixelvibe.vedioplayer.ui.player.PlayerViewModel
import kotlinx.coroutines.delay

/**
 * Transient overlay that shows feedback for gesture actions
 * (speed change, zoom, seek, volume, brightness, etc.)
 */
@Composable
fun PlayerUpdates(
    overlayMessage: String?,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible && overlayMessage != null,
        enter = fadeIn() + scaleIn(initialScale = 0.8f),
        exit = fadeOut() + scaleOut(targetScale = 0.8f),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Color.Black.copy(alpha = 0.7f),
                        MaterialTheme.shapes.large
                    )
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = overlayMessage ?: "",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Format gesture feedback message.
 */
fun formatGestureMessage(type: GestureOverlayType): String {
    return when (type) {
        is GestureOverlayType.None -> ""
        is GestureOverlayType.Brightness -> "Brightness: ${(type.value * 100).toInt()}%"
        is GestureOverlayType.Volume -> {
            val pct = (type.value * 100).toInt()
            if (type.isPercentage) "Volume: $pct%" else "Volume: ${type.value}"
        }
        is GestureOverlayType.Speed -> "Speed: ${type.value}x"
        is GestureOverlayType.Seek -> {
            val current = PlayerViewModel.formatTime(type.currentValue)
            val total = PlayerViewModel.formatTime(type.totalDuration)
            "Seek: $current / $total"
        }
        is GestureOverlayType.Zoom -> {
            val pct = when (type.value) {
                -2 -> 50
                -1 -> 75
                0 -> 100
                1 -> 150
                2 -> 200
                3 -> 300
                else -> 100
            }
            "Zoom: $pct%"
        }
    }
}
