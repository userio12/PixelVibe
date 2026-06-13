package com.pixelvibe.vedioplayer.feature.home

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import androidx.lifecycle.SavedStateHandle
import com.pixelvibe.vedioplayer.core.data.db.dao.PlaylistDao
import com.pixelvibe.vedioplayer.core.data.db.entity.PlaylistEntity
import com.pixelvibe.vedioplayer.core.data.db.entity.PlaylistVideoEntity
import com.pixelvibe.vedioplayer.core.data.db.entity.VideoEntity
import com.pixelvibe.vedioplayer.core.data.repository.VideoRepository
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

private class FakeVideoRepository : VideoRepository {
    private val _videos = MutableStateFlow<List<VideoEntity>>(emptyList())
    override fun getAllVideos(): Flow<List<VideoEntity>> = _videos
    override fun getFolders(): Flow<List<String>> = flowOf(emptyList())
    override suspend fun refresh() {}
    override suspend fun getById(id: String): VideoEntity? = null
    override suspend fun toggleFavorite(id: String) {}
    override suspend fun updateLastPlayed(id: String, position: Long) {}
}

private class FakePlaylistDao : PlaylistDao {
    private val _playlists = MutableStateFlow<List<PlaylistEntity>>(emptyList())
    private val _videos = mutableListOf<PlaylistVideoEntity>()
    override fun getAllPlaylists(): Flow<List<PlaylistEntity>> = _playlists
    override fun getVideosForPlaylist(playlistId: String): Flow<List<PlaylistVideoEntity>> = flowOf(_videos.filter { it.playlistId == playlistId })
    override suspend fun insertPlaylist(playlist: PlaylistEntity) { _playlists.value = _playlists.value + playlist }
    override suspend fun deletePlaylist(playlistId: String) { _playlists.value = _playlists.value.filter { it.id != playlistId } }
    override suspend fun addVideoToPlaylist(entry: PlaylistVideoEntity) { _videos.add(entry) }
    override suspend fun removeVideoFromPlaylist(playlistId: String, videoId: String) { _videos.removeAll { it.playlistId == playlistId && it.videoId == videoId } }
    override suspend fun isVideoInPlaylist(playlistId: String, videoId: String): Boolean = _videos.any { it.playlistId == playlistId && it.videoId == videoId }
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads videos`() {
        val vm = createViewModel()
        assertThat(vm.state.value.isLoading).isFalse()
    }

    @Test
    fun `showCreatePlaylist dialog toggles`() {
        val vm = createViewModel()
        vm.onAction(HomeAction.OnShowCreatePlaylist)
        assertThat(vm.state.value.showCreatePlaylistDialog).isTrue()
        vm.onAction(HomeAction.OnDismissCreatePlaylist)
        assertThat(vm.state.value.showCreatePlaylistDialog).isFalse()
    }

    @Test
    fun `tab selection updates state`() {
        val vm = createViewModel()
        vm.onAction(HomeAction.OnTabSelected(HomeTab.FAVORITES))
        assertThat(vm.state.value.selectedTab).isEqualTo(HomeTab.FAVORITES)
    }

    @Test
    fun `search query updates state`() {
        val vm = createViewModel()
        vm.onAction(HomeAction.OnSearchQueryChange("test"))
        assertThat(vm.state.value.searchQuery).isEqualTo("test")
    }

    @Test
    fun `navigate emits player event`() = runTest(testDispatcher) {
        val vm = createViewModel()
        vm.events.test {
            vm.onAction(HomeAction.OnVideoClick("video-1"))
            val event = awaitItem()
            assertThat(event).isEqualTo(HomeEvent.NavigateToPlayer("video-1"))
        }
    }

    private fun createViewModel(): HomeViewModel {
        return HomeViewModel(
            savedStateHandle = SavedStateHandle(),
            videoRepository = FakeVideoRepository(),
            playlistDao = FakePlaylistDao()
        )
    }
}
