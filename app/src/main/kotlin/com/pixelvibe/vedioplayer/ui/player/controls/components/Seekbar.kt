package com.pixelvibe.vedioplayer.ui.player.controls.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StandardSeekbar(
    currentPosition: Double,
    duration: Double,
    onSeek: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (duration > 0) (currentPosition / duration).toFloat().coerceIn(0f, 1f) else 0f

    Slider(
        value = progress,
        onValueChange = { newProgress ->
            onSeek(newProgress.toDouble() * duration)
        },
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        valueRange = 0f..1f,
        colors = SliderDefaults.colors(
            thumbColor = MaterialTheme.colorScheme.primary,
            activeTrackColor = MaterialTheme.colorScheme.primary,
            inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        )
    )
}

@Composable
fun WavySeekbar(
    currentPosition: Double,
    duration: Double,
    isPlaying: Boolean,
    loopStart: Double? = null,
    loopEnd: Double? = null,
    onSeek: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    StandardSeekbar(currentPosition, duration, onSeek, modifier)
}

@Composable
fun ThickSeekbar(
    currentPosition: Double,
    duration: Double,
    loopStart: Double? = null,
    loopEnd: Double? = null,
    onSeek: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    StandardSeekbar(currentPosition, duration, onSeek, modifier)
}
