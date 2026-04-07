package com.pixelvibe.vedioplayer.ui.player.controls

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.pixelvibe.vedioplayer.preferences.PlayerPreferences
import com.pixelvibe.vedioplayer.ui.player.PlayerButtonType
import com.pixelvibe.vedioplayer.ui.player.PlayerState
import com.pixelvibe.vedioplayer.ui.player.controls.components.BottomBar
import com.pixelvibe.vedioplayer.ui.player.controls.components.ButtonSlot
import com.pixelvibe.vedioplayer.ui.player.controls.components.ChapterDisplay
import com.pixelvibe.vedioplayer.ui.player.controls.components.MarqueeTitle
import com.pixelvibe.vedioplayer.ui.player.controls.components.StandardSeekbar
import com.pixelvibe.vedioplayer.ui.player.controls.components.TopBar
import com.pixelvibe.vedioplayer.ui.player.sheets.PlaybackSpeedSheet
import com.pixelvibe.vedioplayer.ui.player.sheets.SubtitleTracksSheet
import kotlinx.coroutines.delay

/**
 * Enum representing which sheet/panel is currently open.
 */
sealed class PlayerSheet {
    data object None : PlayerSheet()
    data object PlaybackSpeed : PlayerSheet()
    data object SubtitleTracks : PlayerSheet()
}

@Composable
fun PlayerControls(
    state: PlayerState,
    preferences: PlayerPreferences,
    onBackClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onSeek: (Double) -> Unit,
    onNextClick: () -> Unit = {},
    onPrevClick: () -> Unit = {},
    onButtonClick: (PlayerButtonType) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    var controlsVisible by remember { mutableStateOf(true) }
    var autoHideTimerActive by remember { mutableStateOf(false) }
    var currentSheet by remember { mutableStateOf<PlayerSheet>(PlayerSheet.None) }

    // Auto-hide controls after 4 seconds
    LaunchedEffect(controlsVisible, autoHideTimerActive, state.isPlaying) {
        if (controlsVisible && autoHideTimerActive && state.isPlaying) {
            delay(4000)
            controlsVisible = false
        }
    }

    ConstraintLayout(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        controlsVisible = !controlsVisible
                        autoHideTimerActive = true
                    },
                    onDoubleTap = { offset ->
                        val width = size.width
                        val third = width / 3
                        when {
                            offset.x < third -> onSeek(state.currentPosition - 10)
                            offset.x > third * 2 -> onSeek(state.currentPosition + 10)
                            else -> onPlayPauseClick()
                        }
                        autoHideTimerActive = true
                    }
                )
            }
    ) {
        val topBarRef = createRef()
        val bottomRef = createRef()
        val centerRef = createRef()
        val loadingRef = createRef()

        // Top gradient + bar
        AnimatedVisibility(
            visible = controlsVisible && !state.isLocked,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.constrainAs(topBarRef) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent),
                            startY = 0f, endY = 120f
                        )
                    )
            ) {
                // Chapter display
                if (state.chapter > 0) {
                    ChapterDisplay(
                        chapterTitle = "Chapter ${state.chapter + 1}",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                TopBar(
                    mediaTitle = state.mediaTitle,
                    onBackClick = onBackClick
                )

                // Top-right buttons
                ButtonSlot(
                    buttons = listOf(PlayerButtonType.SPEED, PlayerButtonType.SUBTITLES, PlayerButtonType.AUDIO),
                    onButtonClick = { type ->
                        when (type) {
                            PlayerButtonType.SPEED -> currentSheet = PlayerSheet.PlaybackSpeed
                            PlayerButtonType.SUBTITLES -> currentSheet = PlayerSheet.SubtitleTracks
                            PlayerButtonType.AUDIO -> {}
                            else -> {}
                        }
                    },
                    showBackground = true,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(end = 8.dp, top = 8.dp)
                )
            }
        }

        // Center play button
        AnimatedVisibility(
            visible = controlsVisible && !state.isLocked,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.constrainAs(centerRef) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        ) {
            Box(
                modifier = Modifier
                    .padding(48.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { onPlayPauseClick() })
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (state.isPlaying) "Pause" else "Play",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        // Loading indicator
        if (state.isLoading || state.isBuffering) {
            CircularProgressIndicator(
                modifier = Modifier.constrainAs(loadingRef) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Bottom gradient + controls
        AnimatedVisibility(
            visible = controlsVisible && !state.isLocked,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.constrainAs(bottomRef) {
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 0f, endY = 200f
                        )
                    )
            ) {
                StandardSeekbar(
                    currentPosition = state.currentPosition,
                    duration = state.duration,
                    onSeek = onSeek
                )

                // Bottom controls row
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ButtonSlot(
                        buttons = listOf(PlayerButtonType.CHAPTERS, PlayerButtonType.ASPECT_RATIO, PlayerButtonType.PIP),
                        onButtonClick = { /* TODO */ },
                        showBackground = true
                    )
                    Box(Modifier.weight(1f))
                    ButtonSlot(
                        buttons = listOf(PlayerButtonType.MORE, PlayerButtonType.ZOOM),
                        onButtonClick = { /* TODO */ },
                        showBackground = true
                    )
                }

                // Play/Pause + Prev/Next row
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 4.dp),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onPrevClick) {
                        Icon(Icons.Default.SkipPrevious, "Previous", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(onClick = onPlayPauseClick) {
                        Icon(
                            if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            if (state.isPlaying) "Pause" else "Play",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onNextClick) {
                        Icon(Icons.Default.SkipNext, "Next", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }

                // Time labels
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(state.currentPosition),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = formatTime(state.duration),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }

    // ── Sheets ──

    if (currentSheet == PlayerSheet.PlaybackSpeed) {
        PlaybackSpeedSheet(
            currentSpeed = state.playbackSpeed,
            onSpeedSelected = { speed -> onSeek(state.currentPosition) },
            onDismiss = { currentSheet = PlayerSheet.None }
        )
    }

    if (currentSheet == PlayerSheet.SubtitleTracks) {
        SubtitleTracksSheet(
            primaryTracks = emptyList(),
            secondaryTracks = emptyList(),
            selectedPrimary = -1,
            selectedSecondary = -1,
            onPrimarySelected = {},
            onSecondarySelected = {},
            onAddExternal = {},
            onOpenSettings = {},
            onDismiss = { currentSheet = PlayerSheet.None }
        )
    }
}

fun formatTime(seconds: Double): String {
    if (seconds < 0 || seconds.isNaN() || seconds.isInfinite()) return "--:--"
    val totalSeconds = seconds.toLong()
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val secs = totalSeconds % 60
    return if (hours > 0) String.format("%d:%02d:%02d", hours, minutes, secs)
    else String.format("%02d:%02d", minutes, secs)
}
