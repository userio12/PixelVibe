package com.pixelvibe.vedioplayer.feature.player

import android.content.Context
import android.media.AudioManager
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults as MaterialSliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.pixelvibe.vedioplayer.core.player.audio.AudioEffectState
import com.pixelvibe.vedioplayer.core.player.audio.EqualizerPreset
import com.pixelvibe.vedioplayer.core.player.subtitle.SubtitlePosition
import com.pixelvibe.vedioplayer.core.player.subtitle.SubtitleSearchResult
import com.pixelvibe.vedioplayer.core.player.subtitle.SubtitleStyle
import com.pixelvibe.vedioplayer.core.common.util.UiText
import com.pixelvibe.vedioplayer.core.ui.component.ErrorView
import com.pixelvibe.vedioplayer.core.ui.component.LoadingIndicator
import com.pixelvibe.vedioplayer.core.ui.theme.WindowSize
import com.pixelvibe.vedioplayer.core.ui.theme.rememberWindowSize
import org.koin.androidx.compose.koinViewModel

internal const val SEEK_STEP_MS = 10_000L

@Composable
fun PlayerRoot(
    videoId: String,
    onBackPress: () -> Unit = {},
    viewModel: PlayerViewModel = koinViewModel()
) {
    val onAction = viewModel::onAction
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(videoId) {
        viewModel.startPlayback(videoId)
    }

    LaunchedEffect(state.isFinished) {
        if (state.isFinished) {
            onBackPress()
        }
    }

    KeepScreenOn()

    PlayerScreen(
        state = state,
        onAction = viewModel::onAction,
        exoPlayer = viewModel.player,
        onBackPress = onBackPress
    )

    if (state.showEqualizer) {
        EqualizerSheet(
            state = state.audioEffectState,
            onDismiss = { onAction(PlayerAction.OnToggleEqualizer) },
            onSetBand = { band, level -> onAction(PlayerAction.OnSetBandLevel(band, level)) },
            onSetPreset = { onAction(PlayerAction.OnSetPreset(it)) },
            onSetBassBoost = { onAction(PlayerAction.OnSetBassBoost(it)) },
            onSetVirtualizer = { onAction(PlayerAction.OnSetVirtualizer(it)) },
            onSetLoudness = { onAction(PlayerAction.OnSetLoudnessGain(it)) },
            onToggle = { onAction(PlayerAction.OnToggleAudioEffects) }
        )
    }
    if (state.showSubtitleStyle) {
        SubtitleStyleSheet(
            style = state.subtitleStyle,
            onDismiss = { onAction(PlayerAction.OnToggleSubtitleStyle) },
            onUpdate = { onAction(PlayerAction.OnUpdateSubtitleStyle(it)) }
        )
    }
    if (state.showSubtitleSearch) {
        SubtitleSearchSheet(
            state = state,
            onDismiss = { onAction(PlayerAction.OnToggleSubtitleSearch) },
            onQueryChange = { onAction(PlayerAction.OnSubtitleSearchQuery(it)) },
            onDownload = { onAction(PlayerAction.OnDownloadSubtitle(it)) }
        )
    }
}

@Composable
private fun KeepScreenOn() {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val window = (context as? ComponentActivity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}

@Composable
fun PlayerScreen(
    state: PlayerState,
    onAction: (PlayerAction) -> Unit,
    exoPlayer: Player,
    onBackPress: () -> Unit
) {
    val windowSize = rememberWindowSize()
    val subtitleFontScale = if (windowSize.isTablet) 1.5f else 1f
    val controlIconSize = if (windowSize.isTablet) 32.dp else 20.dp
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        PlayerSurface(exoPlayer = exoPlayer, windowSize = windowSize)

        SubtitleOverlay(state.subtitleText, fontSizeScale = subtitleFontScale)

        GestureLayer(onAction = onAction)

        if (state.isBuffering && state.error == null) {
            LoadingIndicator(modifier = Modifier.align(Alignment.Center))
        }

        state.error?.let { errorMsg ->
            ErrorView(
                message = UiText.DynamicString(errorMsg),
                onRetry = { onAction(PlayerAction.OnRetry) },
                modifier = Modifier.align(Alignment.Center)
            )
        }

        SleepTimerOverlay(state = state, modifier = Modifier.align(Alignment.TopCenter))

        RepeatPointIndicator(state = state, modifier = Modifier.align(Alignment.CenterStart))

        AnimatedVisibility(
            visible = state.showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            PlayerControlsOverlay(state = state, onAction = onAction, onBackPress = onBackPress)
        }
    }
}

@Composable
private fun PlayerSurface(exoPlayer: Player, windowSize: WindowSize = WindowSize(isCompact = true, isTablet = false)) {
    val maxHeight = if (windowSize.isTablet) 0.7f else 1f
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            }
        }
    )
}

@Composable
private fun SubtitleOverlay(text: String?, fontSizeScale: Float = 1f) {
    if (text != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = (18 * fontSizeScale).sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 80.dp)
                    .background(Color(0x80000000), RoundedCornerShape(4.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun GestureLayer(onAction: (PlayerAction) -> Unit) {
    val context = LocalContext.current
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    val maxVolume = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { offset ->
                        val centerX = size.width / 2f
                        if (offset.x < centerX) {
                            onAction(PlayerAction.OnDoubleTapLeft)
                        } else {
                            onAction(PlayerAction.OnDoubleTapRight)
                        }
                    },
                    onTap = { onAction(PlayerAction.OnToggleControls) }
                )
            }
            .pointerInput(Unit) {
                val isLeftSide = { x: Float -> x < size.width / 3f }
                val isRightSide = { x: Float -> x > size.width * 2f / 3f }
                detectVerticalDragGestures(
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        val x = change.position.x
                        if (isLeftSide(x)) {
                            audioManager?.let { am ->
                                val current = am.getStreamVolume(AudioManager.STREAM_MUSIC)
                                val step = if (dragAmount < 0) 1 else -1
                                am.setStreamVolume(
                                    AudioManager.STREAM_MUSIC,
                                    (current + step).coerceIn(0, maxVolume),
                                    AudioManager.FLAG_SHOW_UI
                                )
                            }
                        } else if (isRightSide(x)) {
                            val window = (context as? ComponentActivity)?.window ?: return@detectVerticalDragGestures
                            val attrs = window.attributes
                            val brightness = (attrs.screenBrightness ?: 1f) - dragAmount / 1000f
                            attrs.screenBrightness = brightness.coerceIn(0.01f, 1f)
                            window.attributes = attrs
                        }
                    }
                )
            }
    )
}

@Composable
private fun SleepTimerOverlay(state: PlayerState, modifier: Modifier = Modifier) {
    if (state.sleepTimer.isActive) {
        val minutes = state.sleepTimer.remainingSeconds / 60
        val seconds = state.sleepTimer.remainingSeconds % 60
        Row(
            modifier = modifier
                .padding(top = 16.dp)
                .background(Color(0x80000000), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Bedtime, contentDescription = "Sleep timer", tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("%d:%02d".format(minutes, seconds), color = Color.White, fontSize = 12.sp)
        }
    }
}

@Composable
private fun RepeatPointIndicator(state: PlayerState, modifier: Modifier = Modifier) {
    if (state.repeatPoint.startMs >= 0 || state.repeatPoint.endMs >= 0) {
        Column(
            modifier = modifier.padding(start = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("A", color = if (state.repeatPoint.startMs >= 0) Color.Green else Color.Gray,
                fontSize = 10.sp, modifier = Modifier.background(Color(0x80000000), CircleShape).padding(4.dp))
            Text("B", color = if (state.repeatPoint.endMs >= 0) Color.Red else Color.Gray,
                fontSize = 10.sp, modifier = Modifier.background(Color(0x80000000), CircleShape).padding(4.dp))
        }
    }
}

@Composable
private fun PlayerControlsOverlay(state: PlayerState, onAction: (PlayerAction) -> Unit, onBackPress: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().background(Color(0x80000000)).padding(16.dp)
    ) {
        TopControlsRow(state = state, onAction = onAction, onBackPress = onBackPress)
        Spacer(Modifier.height(8.dp))
        PlaybackCenterControls(state = state, onAction = onAction)
        Spacer(Modifier.height(8.dp))
        SpeedAndLoopControls(state = state, onAction = onAction)
        Spacer(Modifier.height(8.dp))
        SeekBarRow(state = state, onAction = onAction)
        Spacer(Modifier.height(8.dp))
        BottomControlsRow(state = state, onAction = onAction)
    }
}

@Composable
private fun TopControlsRow(state: PlayerState, onAction: (PlayerAction) -> Unit, onBackPress: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackPress) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
        Text(formatTime(state.currentPositionMs), color = Color.White, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun PlaybackCenterControls(state: PlayerState, onAction: (PlayerAction) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onAction(PlayerAction.OnSetRepeatA) }) {
            Icon(Icons.Default.Flag, contentDescription = "Set A", tint = if (state.repeatPoint.startMs >= 0) Color.Green else Color.White, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(8.dp))
        IconButton(onClick = { onAction(PlayerAction.OnStepBackward) }) { Icon(Icons.Default.FastRewind, contentDescription = "Step back", tint = Color.White) }
        Spacer(Modifier.width(32.dp))
        IconButton(
            onClick = { onAction(PlayerAction.OnTogglePlay) },
            modifier = Modifier.width(64.dp).height(64.dp).background(Color(0x33FFFFFF), shape = RoundedCornerShape(32.dp))
        ) {
            Icon(if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (state.isPlaying) "Pause" else "Play", tint = Color.White, modifier = Modifier.fillMaxSize())
        }
        Spacer(Modifier.width(32.dp))
        IconButton(onClick = { onAction(PlayerAction.OnStepForward) }) { Icon(Icons.Default.FastForward, contentDescription = "Step forward", tint = Color.White) }
        Spacer(Modifier.width(8.dp))
        IconButton(onClick = { onAction(PlayerAction.OnSetRepeatB) }) {
            Icon(Icons.Default.Flag, contentDescription = "Set B", tint = if (state.repeatPoint.endMs >= 0) Color.Red else Color.White, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun SpeedAndLoopControls(state: PlayerState, onAction: (PlayerAction) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val speeds = listOf(0.25f, 0.5f, 1f, 1.5f, 2f, 3f, 4f)
        var speedIndex = speeds.indexOf(state.playbackSpeed)
        if (speedIndex < 0) speedIndex = 2
        val nextIndex = (speedIndex + 1) % speeds.size
        Button(
            onClick = { onAction(PlayerAction.OnSpeedChange(speeds[nextIndex])) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFFFFF)),
            shape = RoundedCornerShape(16.dp)
        ) { Text("%.2fx".format(state.playbackSpeed), color = Color.White, fontSize = 12.sp) }
        Spacer(Modifier.width(12.dp))
        val hasBoth = state.repeatPoint.startMs >= 0 && state.repeatPoint.endMs >= 0
        Button(
            onClick = {
                when {
                    state.isLooping -> onAction(PlayerAction.OnToggleLoop)
                    hasBoth -> onAction(PlayerAction.OnToggleLoop)
                    state.repeatPoint.startMs < 0 -> onAction(PlayerAction.OnSetRepeatA)
                    else -> onAction(PlayerAction.OnSetRepeatB)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = if (state.isLooping) Color(0xFF4CAF50) else Color(0x33FFFFFF)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Loop, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(when { state.isLooping -> "LOOP ON"; state.repeatPoint.startMs < 0 -> "SET A"; state.repeatPoint.endMs < 0 -> "SET B"; else -> "LOOP OFF" },
                color = Color.White, fontSize = 10.sp)
        }
    }
}

@Composable
private fun SeekBarRow(state: PlayerState, onAction: (PlayerAction) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(formatTime(state.currentPositionMs), color = Color.White, style = MaterialTheme.typography.bodySmall)
        Slider(
            value = if (state.durationMs > 0) state.currentPositionMs.toFloat() / state.durationMs else 0f,
            onValueChange = { onAction(PlayerAction.OnSeek((it * state.durationMs).toLong())) },
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            colors = MaterialSliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White, inactiveTrackColor = Color(0x66FFFFFF))
        )
        Text(formatTime(state.durationMs), color = Color.White, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun BottomControlsRow(state: PlayerState, onAction: (PlayerAction) -> Unit) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onAction(PlayerAction.OnToggleEqualizer) }) {
            Icon(Icons.Default.Equalizer, contentDescription = "Equalizer", tint = Color.White, modifier = Modifier.size(20.dp))
        }
        IconButton(onClick = { onAction(PlayerAction.OnToggleSubtitleStyle) }) {
            Icon(Icons.Default.Flag, contentDescription = "Subtitles", tint = Color.White, modifier = Modifier.size(20.dp))
        }
        IconButton(onClick = { onAction(PlayerAction.OnEnterPipMode) }) {
            Icon(Icons.Default.PictureInPicture, contentDescription = "PiP", tint = Color.White, modifier = Modifier.size(20.dp))
        }
        if (state.sleepTimer.isActive) {
            Button(
                onClick = { onAction(PlayerAction.OnCancelSleepTimer) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0x66FF0000)),
                shape = RoundedCornerShape(16.dp)
            ) { Text("CANCEL SLEEP", color = Color.White, fontSize = 10.sp) }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                listOf(15, 30, 45, 60).forEach { minutes ->
                    Button(
                        onClick = { onAction(PlayerAction.OnStartSleepTimer(minutes)) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFFFFF)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(horizontal = 2.dp)
                    ) { Text(if (minutes < 60) "%dm".format(minutes) else "${minutes / 60}h", color = Color.White, fontSize = 10.sp) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EqualizerSheet(
    state: AudioEffectState,
    onDismiss: () -> Unit,
    onSetBand: (Int, Short) -> Unit,
    onSetPreset: (EqualizerPreset) -> Unit,
    onSetBassBoost: (Short) -> Unit,
    onSetVirtualizer: (Short) -> Unit,
    onSetLoudness: (Int) -> Unit,
    onToggle: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF1A1A1A)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text("Equalizer", color = Color.White, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text("Headphones: ${if (state.isHeadphoneConnected) "Connected" else "Not connected"}",
                color = Color.Gray, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Enabled", color = Color.White)
                Switch(checked = state.isEnabled, onCheckedChange = { onToggle() })
            }

            if (state.isEqualizerAvailable && state.isEnabled) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EqualizerPreset.entries.forEach { preset ->
                        FilterChip(
                            selected = state.currentPreset == preset,
                            onClick = { onSetPreset(preset) },
                            label = { Text(preset.label, fontSize = 10.sp) }
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                state.bandFrequencies.forEachIndexed { index, freq ->
                    val bandLevel = state.bandLevels.getOrElse(index) { 0 }
                    val freqText = when {
                        freq >= 1_000_000 -> "${freq / 1_000_000}kHz"
                        freq >= 1000 -> "${freq / 1000}kHz"
                        else -> "${freq}Hz"
                    }
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(freqText, color = Color.Gray, fontSize = 10.sp, modifier = Modifier.width(48.dp))
                        Slider(
                            value = bandLevel.toFloat(),
                            onValueChange = { onSetBand(index, it.toInt().toShort()) },
                            valueRange = state.bandLevelRange.start.toFloat()..state.bandLevelRange.endInclusive.toFloat(),
                            modifier = Modifier.weight(1f),
                            colors = MaterialSliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color(0xFF4CAF50), inactiveTrackColor = Color(0x33FFFFFF))
                        )
                        Text("$bandLevel", color = Color.White, fontSize = 10.sp, modifier = Modifier.width(32.dp), textAlign = TextAlign.End)
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text("Bass Boost", color = Color.White, style = MaterialTheme.typography.bodySmall)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Slider(value = state.bassBoostLevel.toFloat(), onValueChange = { onSetBassBoost(it.toInt().toShort()) }, valueRange = 0f..1000f, modifier = Modifier.weight(1f))
                    Text("${state.bassBoostLevel / 10}%", color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp))
                }

                Spacer(Modifier.height(8.dp))
                Text("Virtualizer", color = Color.White, style = MaterialTheme.typography.bodySmall)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Slider(value = state.virtualizerStrength.toFloat(), onValueChange = { onSetVirtualizer(it.toInt().toShort()) }, valueRange = 0f..1000f, modifier = Modifier.weight(1f))
                    Text("${state.virtualizerStrength / 10}%", color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp))
                }

                Spacer(Modifier.height(8.dp))
                Text("Loudness Normalization", color = Color.White, style = MaterialTheme.typography.bodySmall)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Slider(value = state.loudnessGain.toFloat(), onValueChange = { onSetLoudness(it.toInt()) }, valueRange = 0f..3000f, modifier = Modifier.weight(1f))
                    Text("${state.loudnessGain / 10}", color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp))
                }
            } else if (!state.isEqualizerAvailable) {
                Spacer(Modifier.height(16.dp))
                Text("Equalizer not available on this device", color = Color.Gray)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubtitleSearchSheet(
    state: PlayerState,
    onDismiss: () -> Unit,
    onQueryChange: (String) -> Unit,
    onDownload: (SubtitleSearchResult) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF1A1A1A)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text("Search Subtitles", color = Color.White, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.subtitleSearchQuery,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by title...", color = Color.Gray) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedBorderColor = Color(0xFF4CAF50),
                    unfocusedBorderColor = Color.Gray
                )
            )
            Spacer(Modifier.height(8.dp))
            when {
                state.isSearchingSubtitles -> {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
                state.subtitleSearchResults.isEmpty() -> {
                    Text("No subtitles found", color = Color.Gray, modifier = Modifier.padding(16.dp))
                }
                else -> {
                    LazyColumn(modifier = Modifier.height(280.dp)) {
                        items(state.subtitleSearchResults) { result ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { onDownload(result) }.padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(result.name, color = Color.White, fontSize = 14.sp, maxLines = 1)
                                    Text("${result.language} • ${result.format}", color = Color.Gray, fontSize = 12.sp)
                                }
                                Text("★ ${result.rating}", color = Color(0xFFFFC107), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubtitleStyleSheet(
    style: SubtitleStyle,
    onDismiss: () -> Unit,
    onUpdate: (SubtitleStyle) -> Unit,
) {
    val positions = listOf(SubtitlePosition.BOTTOM, SubtitlePosition.MIDDLE, SubtitlePosition.TOP)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF1A1A1A)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text("Subtitle Style", color = Color.White, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            Text("Font Size: ${style.fontSize}", color = Color.Gray, fontSize = 13.sp)
            Slider(
                value = style.fontSize.toFloat(),
                onValueChange = { onUpdate(style.copy(fontSize = it.toInt())) },
                valueRange = 10f..40f,
                colors = MaterialSliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color(0xFF4CAF50))
            )

            Spacer(Modifier.height(8.dp))
            Text("Position", color = Color.Gray, fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                positions.forEach { pos ->
                    FilterChip(
                        selected = style.position == pos,
                        onClick = { onUpdate(style.copy(position = pos)) },
                        label = { Text(pos.name, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Bilingual", color = Color.White)
                Switch(checked = style.isBilingual, onCheckedChange = { onUpdate(style.copy(isBilingual = it)) })
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}


private fun formatTime(ms: Long): String {
    if (ms < 0) return "0:00"
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds)
    else "%02d:%02d".format(minutes, seconds)
}
