package com.pixelvibe.vedioplayer.feature.player

import android.app.NotificationManager
import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import com.pixelvibe.vedioplayer.core.data.db.dao.HistoryDao
import com.pixelvibe.vedioplayer.core.data.db.entity.HistoryEntity
import com.pixelvibe.vedioplayer.core.data.repository.VideoRepository
import com.pixelvibe.vedioplayer.core.data.security.IncognitoManager
import com.pixelvibe.vedioplayer.core.player.audio.AudioEffectManager
import com.pixelvibe.vedioplayer.core.player.audio.AudioEffectState
import com.pixelvibe.vedioplayer.core.player.audio.EqualizerPreset
import com.pixelvibe.vedioplayer.core.player.controller.PlayerController
import com.pixelvibe.vedioplayer.core.player.pip.PipHandler
import com.pixelvibe.vedioplayer.core.player.subtitle.SubtitleManager
import com.pixelvibe.vedioplayer.core.player.subtitle.SubtitleSearchClient
import com.pixelvibe.vedioplayer.core.player.subtitle.SubtitleSearchResult
import com.pixelvibe.vedioplayer.core.player.subtitle.SubtitleStyle
import com.pixelvibe.vedioplayer.core.player.subtitle.SubtitleStylePreferences
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class RepeatPoint(
    val startMs: Long = -1L,
    val endMs: Long = -1L
)

data class SleepTimerState(
    val isActive: Boolean = false,
    val remainingSeconds: Int = 0,
    val totalSeconds: Int = 0
)

data class PlayerState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentPositionMs: Long = 0,
    val durationMs: Long = 0,
    val playbackSpeed: Float = 1f,
    val subtitleText: String? = null,
    val error: String? = null,
    val isFinished: Boolean = false,
    val showControls: Boolean = true,
    val repeatPoint: RepeatPoint = RepeatPoint(),
    val isLooping: Boolean = false,
    val sleepTimer: SleepTimerState = SleepTimerState(),
    val showEqualizer: Boolean = false,
    val showSubtitleStyle: Boolean = false,
    val showSubtitleSearch: Boolean = false,
    val subtitleSearchQuery: String = "",
    val subtitleSearchResults: List<SubtitleSearchResult> = emptyList(),
    val isSearchingSubtitles: Boolean = false,
    val subtitleStyle: SubtitleStyle = SubtitleStyle(),
    val audioEffectState: AudioEffectState = AudioEffectState()
)

sealed interface PlayerAction {
    data object OnTogglePlay : PlayerAction
    data class OnSeek(val positionMs: Long) : PlayerAction
    data class OnSpeedChange(val speed: Float) : PlayerAction
    data object OnStepForward : PlayerAction
    data object OnStepBackward : PlayerAction
    data object OnToggleControls : PlayerAction
    data object OnBackPress : PlayerAction
    data object OnSetRepeatA : PlayerAction
    data object OnSetRepeatB : PlayerAction
    data object OnClearRepeat : PlayerAction
    data object OnToggleLoop : PlayerAction
    data class OnStartSleepTimer(val minutes: Int) : PlayerAction
    data object OnCancelSleepTimer : PlayerAction
    data object OnToggleEqualizer : PlayerAction
    data class OnSetBandLevel(val band: Int, val level: Short) : PlayerAction
    data class OnSetPreset(val preset: EqualizerPreset) : PlayerAction
    data class OnSetBassBoost(val level: Short) : PlayerAction
    data class OnSetVirtualizer(val strength: Short) : PlayerAction
    data class OnSetLoudnessGain(val gain: Int) : PlayerAction
    data object OnToggleAudioEffects : PlayerAction
    data object OnDoubleTapLeft : PlayerAction
    data object OnDoubleTapRight : PlayerAction
    data object OnToggleSubtitleStyle : PlayerAction
    data class OnUpdateSubtitleStyle(val style: SubtitleStyle) : PlayerAction
    data object OnToggleSubtitleSearch : PlayerAction
    data class OnSubtitleSearchQuery(val query: String) : PlayerAction
    data class OnDownloadSubtitle(val result: SubtitleSearchResult) : PlayerAction
    data object OnRetry : PlayerAction
    data object OnEnterPipMode : PlayerAction
}

sealed interface PlayerEvent {
    data object OnBackPress : PlayerEvent
    data object OnSleepTimerExpired : PlayerEvent
}

class PlayerViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val playerController: PlayerController,
    private val subtitleManager: SubtitleManager,
    private val pipHandler: PipHandler,
    private val audioEffectManager: AudioEffectManager,
    private val subtitleStylePrefs: SubtitleStylePreferences,
    private val subtitleSearchClient: SubtitleSearchClient,
    private val historyDao: HistoryDao,
    private val videoRepository: VideoRepository,
    private val incognitoManager: IncognitoManager,
    private val context: Context? = null
) : ViewModel() {

    val player: Player get() = playerController.player

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<PlayerEvent>()
    val events = _events.asSharedFlow()

    private var sleepTimerJob: Job? = null
    private var loopCheckJob: Job? = null
    private var currentVideoId: String = ""
    private var currentVideoTitle: String = ""

    init {
        savedStateHandle.get<Float>("playbackSpeed")?.let { speed ->
            _state.value = _state.value.copy(playbackSpeed = speed)
        }
        savedStateHandle.get<Long>("repeatStart")?.let { start ->
            savedStateHandle.get<Long>("repeatEnd")?.let { end ->
                _state.value = _state.value.copy(repeatPoint = RepeatPoint(start, end))
            }
        }
    }

    fun startPlayback(videoUri: String) {
        viewModelScope.launch {
            val resolved = if (videoUri.startsWith("content://") || videoUri.startsWith("http") ||
                videoUri.startsWith("/")
            ) {
                videoUri
            } else {
                videoRepository.getVideoById(videoUri)?.uri ?: videoUri
            }
            currentVideoId = resolved
            currentVideoTitle = resolved.substringAfterLast('/').substringBeforeLast('?')
                .ifEmpty { resolved }
            playerController.play(resolved)
            val savedSpeed = savedStateHandle.get<Float>("playbackSpeed")
            if (savedSpeed != null) {
                playerController.setSpeed(savedSpeed)
            }
            checkResumePosition(resolved)
        }
        observePlaybackState()
        observeAudioEffects()
        observeSubtitles()
        observeSubtitleStyle()
        persistState()
    }

    private fun persistState() {
        viewModelScope.launch {
            _state.collect { s ->
                savedStateHandle["playbackSpeed"] = s.playbackSpeed
                savedStateHandle["repeatStart"] = s.repeatPoint.startMs
                savedStateHandle["repeatEnd"] = s.repeatPoint.endMs
                if (s.currentPositionMs > 0) {
                    savedStateHandle["currentPositionMs"] = s.currentPositionMs
                }
            }
        }
    }

    private fun checkResumePosition(videoUri: String) {
        viewModelScope.launch {
            if (incognitoManager.isIncognito.first()) return@launch
            val entry = historyDao.getByVideoId(videoUri)
            if (entry != null && entry.positionMs > 0 && entry.durationMs > 0) {
                playerController.seekTo(entry.positionMs)
            }
        }
    }

    private fun saveToHistory(positionMs: Long, durationMs: Long) {
        viewModelScope.launch {
            if (incognitoManager.isIncognito.first()) return@launch
            if (currentVideoId.isEmpty()) return@launch
            historyDao.insert(
                HistoryEntity(
                    id = currentVideoId,
                    videoId = currentVideoId,
                    videoTitle = currentVideoTitle,
                    videoUri = currentVideoId,
                    watchedAt = System.currentTimeMillis(),
                    positionMs = positionMs,
                    durationMs = durationMs
                )
            )
        }
    }

    fun onAction(action: PlayerAction) {
        when (action) {
            PlayerAction.OnTogglePlay -> playerController.togglePlay()
            is PlayerAction.OnSeek -> playerController.seekTo(action.positionMs)
            is PlayerAction.OnSpeedChange -> playerController.setSpeed(action.speed)
            PlayerAction.OnStepForward -> playerController.stepForward()
            PlayerAction.OnStepBackward -> playerController.stepBackward()
            PlayerAction.OnToggleControls -> {
                _state.value = _state.value.copy(showControls = !_state.value.showControls)
            }
            PlayerAction.OnBackPress -> {
                sleepTimerJob?.cancel()
                loopCheckJob?.cancel()
                _events.tryEmit(PlayerEvent.OnBackPress)
            }
            PlayerAction.OnSetRepeatA -> {
                _state.value = _state.value.copy(
                    repeatPoint = _state.value.repeatPoint.copy(startMs = _state.value.currentPositionMs),
                    isLooping = false
                )
            }
            PlayerAction.OnSetRepeatB -> {
                _state.value = _state.value.copy(
                    repeatPoint = _state.value.repeatPoint.copy(endMs = _state.value.currentPositionMs),
                    isLooping = false
                )
            }
            PlayerAction.OnClearRepeat -> {
                _state.value = _state.value.copy(repeatPoint = RepeatPoint(), isLooping = false)
                loopCheckJob?.cancel()
            }
            PlayerAction.OnToggleLoop -> {
                val s = _state.value
                if (s.repeatPoint.startMs >= 0 && s.repeatPoint.endMs >= 0) {
                    val newLooping = !s.isLooping
                    _state.value = s.copy(isLooping = newLooping)
                    if (newLooping) startLoopCheck() else loopCheckJob?.cancel()
                }
            }
            is PlayerAction.OnStartSleepTimer -> startSleepTimer(action.minutes)
            PlayerAction.OnCancelSleepTimer -> cancelSleepTimer()
            PlayerAction.OnToggleEqualizer -> {
                _state.value = _state.value.copy(showEqualizer = !_state.value.showEqualizer)
            }
            is PlayerAction.OnSetBandLevel -> audioEffectManager.setBandLevel(action.band, action.level)
            is PlayerAction.OnSetPreset -> audioEffectManager.setPreset(action.preset)
            is PlayerAction.OnSetBassBoost -> audioEffectManager.setBassBoost(action.level)
            is PlayerAction.OnSetVirtualizer -> audioEffectManager.setVirtualizer(action.strength)
            is PlayerAction.OnSetLoudnessGain -> audioEffectManager.setLoudnessGain(action.gain)
            PlayerAction.OnToggleAudioEffects -> audioEffectManager.toggleEnabled()
            PlayerAction.OnDoubleTapLeft -> {
                val newPos = maxOf(0L, _state.value.currentPositionMs - SEEK_STEP_MS)
                playerController.seekTo(newPos)
            }
            PlayerAction.OnDoubleTapRight -> {
                val newPos = _state.value.currentPositionMs + SEEK_STEP_MS
                playerController.seekTo(newPos)
            }
            PlayerAction.OnToggleSubtitleStyle -> {
                _state.value = _state.value.copy(showSubtitleStyle = !_state.value.showSubtitleStyle)
            }
            is PlayerAction.OnUpdateSubtitleStyle -> {
                viewModelScope.launch { subtitleStylePrefs.updateStyle(action.style) }
            }
            PlayerAction.OnToggleSubtitleSearch -> {
                val showing = !_state.value.showSubtitleSearch
                _state.value = _state.value.copy(
                    showSubtitleSearch = showing,
                    subtitleSearchQuery = if (showing) currentVideoTitle else _state.value.subtitleSearchQuery
                )
                if (showing) {
                    searchSubtitles(currentVideoTitle)
                }
            }
            is PlayerAction.OnSubtitleSearchQuery -> {
                _state.value = _state.value.copy(subtitleSearchQuery = action.query)
                searchSubtitles(action.query)
            }
            is PlayerAction.OnDownloadSubtitle -> {
                viewModelScope.launch {
                    _state.value = _state.value.copy(isSearchingSubtitles = true)
                    val data = subtitleSearchClient.downloadSubtitle(action.result)
                    if (data != null) {
                        subtitleManager.addExternalSubtitle(
                            uri = action.result.downloadUrl,
                            name = action.result.name,
                            language = action.result.language,
                            content = data
                        )
                    }
                    _state.value = _state.value.copy(
                        isSearchingSubtitles = false,
                        showSubtitleSearch = false
                    )
                }
            }
            PlayerAction.OnRetry -> {
                _state.value = _state.value.copy(error = null, isBuffering = true)
                if (currentVideoId.isNotEmpty()) {
                    startPlayback(currentVideoId)
                }
            }
            PlayerAction.OnEnterPipMode -> {
                (context as? android.app.Activity)?.let { pipHandler.enterPipMode(it) }
            }
        }
    }

    private fun observePlaybackState() {
        var wasPlaying = false
        viewModelScope.launch {
            playerController.state.collect { playbackState ->
                _state.value = _state.value.copy(
                    isPlaying = playbackState.isPlaying,
                    isBuffering = playbackState.isBuffering,
                    currentPositionMs = playbackState.currentPositionMs,
                    durationMs = playbackState.durationMs,
                    playbackSpeed = playbackState.playbackSpeed,
                    error = playbackState.error,
                    isFinished = playbackState.isFinished
                )
                if (playbackState.isPlaying) {
                    updateSubtitlePosition(playbackState.currentPositionMs)
                }
                val state = _state.value
                if (state.isFinished || (!playbackState.isPlaying && wasPlaying)) {
                    saveToHistory(state.currentPositionMs, state.durationMs)
                }
                wasPlaying = playbackState.isPlaying
            }
        }
    }

    private fun observeSubtitleStyle() {
        viewModelScope.launch {
            subtitleStylePrefs.style.collect { style ->
                _state.value = _state.value.copy(subtitleStyle = style)
            }
        }
    }

    private fun observeAudioEffects() {
        viewModelScope.launch {
            audioEffectManager.init(playerController.audioSessionId)
        }
        viewModelScope.launch {
            audioEffectManager.state.collect { audioState ->
                _state.value = _state.value.copy(audioEffectState = audioState)
            }
        }
    }

    private fun observeSubtitles() {
        viewModelScope.launch {
            subtitleManager.state.collect { subState ->
                _state.value = _state.value.copy(
                    subtitleText = subState.currentText
                )
            }
        }
    }

    private fun updateSubtitlePosition(positionMs: Long) {
        subtitleManager.updatePosition(positionMs)
    }

    private fun startLoopCheck() {
        loopCheckJob?.cancel()
        loopCheckJob = viewModelScope.launch {
            while (true) {
                delay(100)
                val s = _state.value
                if (s.isLooping && s.repeatPoint.endMs >= 0 && s.currentPositionMs >= s.repeatPoint.endMs) {
                    playerController.seekTo(s.repeatPoint.startMs)
                }
            }
        }
    }

    private fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        val totalSeconds = minutes * 60
        _state.value = _state.value.copy(
            sleepTimer = SleepTimerState(isActive = true, remainingSeconds = totalSeconds, totalSeconds = totalSeconds)
        )
        sleepTimerJob = viewModelScope.launch {
            for (i in totalSeconds downTo 0) {
                _state.value = _state.value.copy(
                    sleepTimer = _state.value.sleepTimer.copy(remainingSeconds = i)
                )
                if (i == 0) {
                    playerController.togglePlay()
                    _events.tryEmit(PlayerEvent.OnSleepTimerExpired)
                    _state.value = _state.value.copy(sleepTimer = SleepTimerState())
                    (context?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)
                        ?.cancel(1001)
                }
                delay(1000)
            }
        }
    }

    private fun searchSubtitles(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isSearchingSubtitles = true)
            val results = subtitleSearchClient.search(query)
            _state.value = _state.value.copy(
                subtitleSearchResults = results,
                isSearchingSubtitles = false
            )
        }
    }

    private fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        _state.value = _state.value.copy(sleepTimer = SleepTimerState())
    }

    override fun onCleared() {
        super.onCleared()
        sleepTimerJob?.cancel()
        loopCheckJob?.cancel()
        audioEffectManager.release()
        playerController.release()
    }
}
