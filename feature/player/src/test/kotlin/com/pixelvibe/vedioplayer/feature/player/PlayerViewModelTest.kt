package com.pixelvibe.vedioplayer.feature.player

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import androidx.lifecycle.SavedStateHandle
import androidx.media3.common.Player
import com.pixelvibe.vedioplayer.core.data.db.dao.HistoryDao
import com.pixelvibe.vedioplayer.core.data.db.entity.HistoryEntity
import com.pixelvibe.vedioplayer.core.data.security.IncognitoManager
import com.pixelvibe.vedioplayer.core.player.audio.AudioEffectManager
import com.pixelvibe.vedioplayer.core.player.controller.PlayerController
import com.pixelvibe.vedioplayer.core.player.engine.PlaybackEngine
import com.pixelvibe.vedioplayer.core.player.engine.PlaybackState
import com.pixelvibe.vedioplayer.core.player.pip.PipHandler
import com.pixelvibe.vedioplayer.core.player.subtitle.SubtitleManager
import com.pixelvibe.vedioplayer.core.player.subtitle.SubtitleSearchClient
import com.pixelvibe.vedioplayer.core.player.subtitle.SubtitleSearchResult
import com.pixelvibe.vedioplayer.core.player.subtitle.SubtitleStyle
import com.pixelvibe.vedioplayer.core.player.subtitle.SubtitleStylePreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private class FakePlaybackEngine : PlaybackEngine(null!!) {
    private val _state = MutableStateFlow(PlaybackState())
    override val state: MutableStateFlow<PlaybackState> = _state
    override fun play(uri: String) {}
    override fun play(mediaItems: List<String>, startIndex: Int) {}
    override fun togglePlay() {}
    override fun seekTo(positionMs: Long) {}
    override fun setSpeed(speed: Float) {}
    override fun setVolume(volume: Float) {}
    override fun stepForward() {}
    override fun stepBackward() {}
    override val currentPosition: Long get() = 0
    override val duration: Long get() = 0
    override val audioSessionId: Int get() = 0
    override val playerRef: Player get() = null!!
    override fun release() {}
}

private class FakePlayerController(engine: PlaybackEngine = FakePlaybackEngine()) : PlayerController(engine) {
    private val _state = MutableStateFlow(PlaybackState())
    override val state: MutableStateFlow<PlaybackState> = _state
    override fun play(uri: String) {}
    override fun playAll(uris: List<String>, startIndex: Int) {}
    override fun togglePlay() {}
    override fun seekTo(positionMs: Long) {}
    override fun setSpeed(speed: Float) {}
    override fun setVolume(volume: Float) {}
    override fun stepForward() {}
    override fun stepBackward() {}
    override val currentPosition: Long get() = 0
    override val duration: Long get() = 0
    override val audioSessionId: Int get() = 0
    override val player: Player get() = null!!
    override fun release() {}
}

private class FakePipHandler : PipHandler() {
    override fun release() {}
}

private class FakeSubtitleStylePreferences : SubtitleStylePreferences(null!!) {
    override val style: Flow<SubtitleStyle> = MutableStateFlow(SubtitleStyle())
    override suspend fun updateStyle(style: SubtitleStyle) {}
    override suspend fun updateFontSize(size: Int) {}
    override suspend fun updateFontColor(color: String) {}
    override suspend fun updateBilingual(enabled: Boolean) {}
}

private class FakeSubtitleSearchClient : SubtitleSearchClient {
    override suspend fun search(query: String): List<SubtitleSearchResult> = emptyList()
}

private class FakeIncognitoManager : IncognitoManager(null!!) {
    override val isIncognito: Flow<Boolean> = MutableStateFlow(false)
    override suspend fun setIncognito(enabled: Boolean) {}
}

private class FakeHistoryDaoImpl : HistoryDao {
    override fun getAllHistory(): Flow<List<HistoryEntity>> = flowOf(emptyList())
    override suspend fun insert(entry: HistoryEntity) {}
    override suspend fun deleteById(id: String) {}
    override suspend fun deleteAll() {}
    override suspend fun getByVideoId(videoId: String): HistoryEntity? = null
}

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var audioEffectManager: AudioEffectManager

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        audioEffectManager = AudioEffectManager(null!!)
    }

    @AfterEach
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `repeatPoint defaults to -1`() {
        val vm = createViewModel()
        assertThat(vm.state.value.repeatPoint.startMs).isEqualTo(-1)
        assertThat(vm.state.value.repeatPoint.endMs).isEqualTo(-1)
    }

    @Test
    fun `clearRepeat resets A and B points`() {
        val vm = createViewModel()
        vm.onAction(PlayerAction.OnSetRepeatA)
        vm.onAction(PlayerAction.OnSetRepeatB)
        vm.onAction(PlayerAction.OnClearRepeat)
        assertThat(vm.state.value.repeatPoint.startMs).isEqualTo(-1)
        assertThat(vm.state.value.repeatPoint.endMs).isEqualTo(-1)
    }

    @Test
    fun `sleep timer starts with correct countdown`() {
        val vm = createViewModel()
        vm.onAction(PlayerAction.OnStartSleepTimer(2))
        assertThat(vm.state.value.sleepTimer.isActive).isTrue()
        assertThat(vm.state.value.sleepTimer.remainingSeconds).isEqualTo(120)
    }

    @Test
    fun `sleep timer cancels resets state`() {
        val vm = createViewModel()
        vm.onAction(PlayerAction.OnStartSleepTimer(5))
        vm.onAction(PlayerAction.OnCancelSleepTimer)
        assertThat(vm.state.value.sleepTimer.isActive).isFalse()
        assertThat(vm.state.value.sleepTimer.remainingSeconds).isEqualTo(0)
    }

    @Test
    fun `back press emits OnBackPress event`() = runTest(testDispatcher) {
        val vm = createViewModel()
        vm.events.test {
            vm.onAction(PlayerAction.OnBackPress)
            assertThat(awaitItem()).isEqualTo(PlayerEvent.OnBackPress)
        }
    }

    @Test
    fun `toggle controls flips showControls`() {
        val vm = createViewModel()
        vm.onAction(PlayerAction.OnToggleControls)
        assertThat(vm.state.value.showControls).isFalse()
        vm.onAction(PlayerAction.OnToggleControls)
        assertThat(vm.state.value.showControls).isTrue()
    }

    @Test
    fun `initial state has defaults`() {
        val vm = createViewModel()
        assertThat(vm.state.value.showControls).isTrue()
        assertThat(vm.state.value.isLooping).isFalse()
        assertThat(vm.state.value.sleepTimer.isActive).isFalse()
        assertThat(vm.state.value.repeatPoint.startMs).isEqualTo(-1)
        assertThat(vm.state.value.repeatPoint.endMs).isEqualTo(-1)
        assertThat(vm.state.value.showEqualizer).isFalse()
    }

    @Test
    fun `toggle equalizer flips showEqualizer`() {
        val vm = createViewModel()
        vm.onAction(PlayerAction.OnToggleEqualizer)
        assertThat(vm.state.value.showEqualizer).isTrue()
        vm.onAction(PlayerAction.OnToggleEqualizer)
        assertThat(vm.state.value.showEqualizer).isFalse()
    }

    @Test
    fun `initial audio effect state has defaults`() {
        val vm = createViewModel()
        assertThat(vm.state.value.audioEffectState.isEnabled).isFalse()
        assertThat(vm.state.value.audioEffectState.bassBoostLevel).isEqualTo(0)
    }

    private fun createViewModel(): PlayerViewModel {
        return PlayerViewModel(
            savedStateHandle = SavedStateHandle(),
            playerController = FakePlayerController(),
            subtitleManager = SubtitleManager(),
            pipHandler = FakePipHandler(),
            audioEffectManager = audioEffectManager,
            subtitleStylePrefs = FakeSubtitleStylePreferences(),
            subtitleSearchClient = FakeSubtitleSearchClient(),
            historyDao = FakeHistoryDaoImpl(),
            incognitoManager = FakeIncognitoManager(),
            context = null
        )
    }
}
