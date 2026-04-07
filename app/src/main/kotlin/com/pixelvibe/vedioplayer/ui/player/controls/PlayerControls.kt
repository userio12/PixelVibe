package com.pixelvibe.vedioplayer.ui.player.controls

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.hapticfeedback.LocalHapticFeedback
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.pixelvibe.vedioplayer.preferences.PlayerPreferences
import com.pixelvibe.vedioplayer.preferences.collectAsState
import com.pixelvibe.vedioplayer.ui.player.PlayerButtonType
import com.pixelvibe.vedioplayer.ui.player.PlayerState
import com.pixelvibe.vedioplayer.ui.player.controls.components.BottomBar
import com.pixelvibe.vedioplayer.ui.player.controls.components.ButtonSlot
import com.pixelvibe.vedioplayer.ui.player.controls.components.ChapterDisplay
import com.pixelvibe.vedioplayer.ui.player.controls.components.MarqueeTitle
import com.pixelvibe.vedioplayer.ui.player.controls.components.PlayerButton
import com.pixelvibe.vedioplayer.ui.player.controls.components.StandardSeekbar
import com.pixelvibe.vedioplayer.ui.player.controls.components.ThickSeekbar
import com.pixelvibe.vedioplayer.ui.player.controls.components.TopBar
import com.pixelvibe.vedioplayer.ui.player.controls.components.WavySeekbar
import com.pixelvibe.vedioplayer.ui.player.panels.AudioDelayPanel
import com.pixelvibe.vedioplayer.ui.player.panels.MultiCardPanel
import com.pixelvibe.vedioplayer.ui.player.panels.SubtitleDelayPanel
import com.pixelvibe.vedioplayer.ui.player.panels.SubtitleSettingsPanel
import com.pixelvibe.vedioplayer.ui.player.panels.VideoSettingsPanel
import com.pixelvibe.vedioplayer.ui.player.sheets.AspectRatioSheet
import com.pixelvibe.vedioplayer.ui.player.sheets.AudioTracksSheet
import com.pixelvibe.vedioplayer.ui.player.sheets.ChaptersSheet
import com.pixelvibe.vedioplayer.ui.player.sheets.DecodersSheet
import com.pixelvibe.vedioplayer.ui.player.sheets.FrameNavigationSheet
import com.pixelvibe.vedioplayer.ui.player.sheets.MoreSheet
import com.pixelvibe.vedioplayer.ui.player.sheets.OnlineSubtitleSearchSheet
import com.pixelvibe.vedioplayer.ui.player.sheets.PlaybackSpeedSheet
import com.pixelvibe.vedioplayer.ui.player.sheets.PlaylistSheet
import com.pixelvibe.vedioplayer.ui.player.sheets.SubtitleTracksSheet
import com.pixelvibe.vedioplayer.ui.player.sheets.VideoZoomSheet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Enum representing which sheet/panel is currently open.
 */
sealed class PlayerSheet {
    data object None : PlayerSheet()
    data object PlaybackSpeed : PlayerSheet()
    data object SubtitleTracks : PlayerSheet()
    data object OnlineSubtitleSearch : PlayerSheet()
    data object AudioTracks : PlayerSheet()
    data object Chapters : PlayerSheet()
    data object Decoders : PlayerSheet()
    data object More : PlayerSheet()
    data object VideoZoom : PlayerSheet()
    data object AspectRatio : PlayerSheet()
    data object Playlist : PlayerSheet()
    data object FrameNavigation : PlayerSheet()
    data object SubtitleSettings : PlayerSheet()
    data object SubtitleDelay : PlayerSheet()
    data object AudioDelay : PlayerSheet()
    data object VideoSettings : PlayerSheet()
    data object MultiCard : PlayerSheet()
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
    val context = LocalContext.current

    var controlsVisible by remember { mutableStateOf(true) }
    var autoHideTimerActive by remember { mutableStateOf(false) }
    var currentSheet by remember { mutableStateOf<PlayerSheet>(PlayerSheet.None) }
    var overlayMessage by remember { mutableStateOf<String?>(null) }
    var overlayVisible by remember { mutableStateOf(false) }

    // Read preferences
    val seekbarStyle by preferences.seekbarStyle.collectAsState()
    val showButtonBg by preferences.hideButtonBackground.collectAsState().let { remember(it) { !it.value } }

    // Auto-hide controls
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
                    },
                    onLongPress = {
                        // Phase 2: Speed up gesture
                    }
                )
            }
    ) {
        val topBarRef = createRef()
        val bottomRef = createRef()
        val centerRef = createRef()
        val loadingRef = createRef()
        val unlockRef = createRef()

        // Top gradient + bar
        AnimatedVisibility(
            visible = controlsVisible && !state.isLocked,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(animationSpec = tween(100)),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(animationSpec = tween(300)),
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
                            PlayerButtonType.AUDIO -> currentSheet = PlayerSheet.AudioTracks
                            else -> {}
                        }
                    },
                    showBackground = showButtonBg,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(end = 8.dp, top = 8.dp)
                )
            }
        }

        // Center play button
        AnimatedVisibility(
            visible = controlsVisible && !state.isLocked,
            enter = fadeIn(animationSpec = tween(100)),
            exit = fadeOut(animationSpec = tween(300)),
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
                androidx.compose.material3.Icon(
                    if (state.isPlaying)
                        androidx.compose.material.icons.Icons.Default.Pause
                    else
                        androidx.compose.material.icons.Icons.Default.PlayArrow,
                    contentDescription = if (state.isPlaying) "Pause" else "Play",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier
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

        // Lock overlay
        if (state.isLocked) {
            SlideToUnlock(
                onUnlock = { /* toggle lock */ },
                modifier = Modifier.constrainAs(unlockRef) {
                    bottom.linkTo(parent.bottom, margin = 32.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
            )
        }

        // Bottom gradient + controls
        AnimatedVisibility(
            visible = controlsVisible && !state.isLocked,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(animationSpec = tween(100)),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(animationSpec = tween(300)),
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
                // Seekbar based on preference
                when (seekbarStyle) {
                    "wavy" -> WavySeekbar(
                        currentPosition = state.currentPosition,
                        duration = state.duration,
                        isPlaying = state.isPlaying,
                        loopStart = state.aLoopStart,
                        loopEnd = state.aLoopEnd,
                        onSeek = onSeek
                    )
                    "thick" -> ThickSeekbar(
                        currentPosition = state.currentPosition,
                        duration = state.duration,
                        onSeek = onSeek
                    )
                    else -> StandardSeekbar(
                        currentPosition = state.currentPosition,
                        duration = state.duration,
                        onSeek = onSeek
                    )
                }

                // Bottom controls row
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left buttons
                    ButtonSlot(
                        buttons = listOf(PlayerButtonType.CHAPTERS, PlayerButtonType.ASPECT_RATIO, PlayerButtonType.PIP),
                        onButtonClick = { type ->
                            when (type) {
                                PlayerButtonType.CHAPTERS -> currentSheet = PlayerSheet.Chapters
                                PlayerButtonType.ASPECT_RATIO -> currentSheet = PlayerSheet.AspectRatio
                                PlayerButtonType.PIP -> { /* PiP handled by Activity */ }
                                else -> {}
                            }
                        },
                        showBackground = showButtonBg
                    )

                    // Spacer
                    Box(Modifier.weight(1f))

                    // Right buttons
                    ButtonSlot(
                        buttons = listOf(PlayerButtonType.MORE, PlayerButtonType.ZOOM),
                        onButtonClick = { type ->
                            when (type) {
                                PlayerButtonType.MORE -> currentSheet = PlayerSheet.More
                                PlayerButtonType.ZOOM -> currentSheet = PlayerSheet.VideoZoom
                                else -> {}
                            }
                        },
                        showBackground = showButtonBg
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
                    androidx.compose.material3.IconButton(onClick = onPrevClick) {
                        androidx.compose.material3.Icon(
                            androidx.compose.material.icons.Icons.Default.SkipPrevious,
                            "Previous",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    androidx.compose.material3.IconButton(onClick = onPlayPauseClick) {
                        androidx.compose.material3.Icon(
                            if (state.isPlaying) androidx.compose.material.icons.Icons.Default.Pause
                            else androidx.compose.material.icons.Icons.Default.PlayArrow,
                            if (state.isPlaying) "Pause" else "Play",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    androidx.compose.material3.IconButton(onClick = onNextClick) {
                        androidx.compose.material3.Icon(
                            androidx.compose.material.icons.Icons.Default.SkipNext,
                            "Next",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
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
                        text = PlayerViewModel.formatTime(state.currentPosition),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = PlayerViewModel.formatTime(state.duration),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Overlay message
        PlayerUpdates(
            overlayMessage = overlayMessage,
            visible = overlayVisible,
            modifier = Modifier
        )
    }

    // ── Sheets ──

    if (currentSheet == PlayerSheet.PlaybackSpeed) {
        PlaybackSpeedSheet(
            currentSpeed = state.playbackSpeed,
            onSpeedSelected = { speed -> onButtonClick(PlayerButtonType.SPEED) },
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
            onOpenSettings = { currentSheet = PlayerSheet.SubtitleSettings },
            onDismiss = { currentSheet = PlayerSheet.None }
        )
    }

    if (currentSheet == PlayerSheet.AudioTracks) {
        AudioTracksSheet(
            tracks = emptyList(),
            selectedTrack = -1,
            onTrackSelected = {},
            onAddExternal = {},
            onDismiss = { currentSheet = PlayerSheet.None }
        )
    }

    if (currentSheet == PlayerSheet.Chapters) {
        ChaptersSheet(
            chapters = emptyList(),
            currentChapter = state.chapter,
            onChapterSelected = {},
            onDismiss = { currentSheet = PlayerSheet.None }
        )
    }

    if (currentSheet == PlayerSheet.Decoders) {
        DecodersSheet(
            currentDecoder = "mediacodec",
            onDecoderSelected = {},
            onDismiss = { currentSheet = PlayerSheet.None }
        )
    }

    if (currentSheet == PlayerSheet.More) {
        MoreSheet(
            isBackgroundPlaybackEnabled = false,
            onBackgroundPlaybackToggle = {},
            onSnapshot = {},
            onSnapshotWithSubs = {},
            onSleepTimer = {},
            onMediaInfo = {},
            onDismiss = { currentSheet = PlayerSheet.None }
        )
    }

    if (currentSheet == PlayerSheet.VideoZoom) {
        VideoZoomSheet(
            currentZoom = state.zoomLevel,
            onZoomSelected = {},
            onDismiss = { currentSheet = PlayerSheet.None }
        )
    }

    if (currentSheet == PlayerSheet.AspectRatio) {
        AspectRatioSheet(
            currentRatio = state.aspectRatio,
            onRatioSelected = {},
            onDismiss = { currentSheet = PlayerSheet.None }
        )
    }

    if (currentSheet == PlayerSheet.Playlist) {
        PlaylistSheet(
            items = emptyList(),
            currentIndex = 0,
            onItemSelected = {},
            onDismiss = { currentSheet = PlayerSheet.None }
        )
    }

    if (currentSheet == PlayerSheet.FrameNavigation) {
        FrameNavigationSheet(
            currentFrame = 0,
            onFrameStep = {},
            onFrameBackStep = {},
            onSnapshot = {},
            onDismiss = { currentSheet = PlayerSheet.None }
        )
    }

    if (currentSheet == PlayerSheet.OnlineSubtitleSearch) {
        OnlineSubtitleSearchSheet(
            onDismiss = { currentSheet = PlayerSheet.None }
        )
    }

    // ── Panels (draggable side panels - simplified for Phase 2) ──

    if (currentSheet == PlayerSheet.SubtitleSettings) {
        SubtitleSettingsPanel(
            fontName = "",
            fontSize = 0,
            isBold = false,
            isItalic = false,
            borderColor = 0,
            textColor = -1,
            backgroundColor = 0,
            borderSize = 0,
            shadowOffset = 0,
            scale = 1f,
            position = 0,
            isAssOverride = false,
            isScaleByWindow = false,
            onFontChange = {},
            onFontSizeChange = {},
            onBoldChange = {},
            onTextColorChange = {},
            onBorderColorChange = {},
            onBackgroundColorChange = {},
            onBorderSizeChange = {},
            onShadowOffsetChange = {},
            onScaleChange = {},
            onPositionChange = {},
            onAssOverrideChange = {},
            onScaleByWindowChange = {},
            onClose = { currentSheet = PlayerSheet.None }
        )
    }

    if (currentSheet == PlayerSheet.SubtitleDelay) {
        SubtitleDelayPanel(
            primaryDelay = 0f,
            secondaryDelay = 0f,
            primarySpeed = 1f,
            secondarySpeed = 1f,
            onPrimaryDelayChange = {},
            onSecondaryDelayChange = {},
            onPrimarySpeedChange = {},
            onSecondarySpeedChange = {},
            onClose = { currentSheet = PlayerSheet.None }
        )
    }

    if (currentSheet == PlayerSheet.AudioDelay) {
        AudioDelayPanel(
            delay = 0f,
            onDelayChange = {},
            onClose = { currentSheet = PlayerSheet.None }
        )
    }

    if (currentSheet == PlayerSheet.VideoSettings) {
        VideoSettingsPanel(
            filters = com.pixelvibe.vedioplayer.ui.player.panels.VideoFilterState(),
            currentPreset = "None",
            debanding = "none",
            onFilterChange = {},
            onPresetChange = {},
            onDebandingChange = {},
            onClose = { currentSheet = PlayerSheet.None }
        )
    }
}
