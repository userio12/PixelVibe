package com.pixelvibe.vedioplayer.feature.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelvibe.vedioplayer.core.data.db.dao.PlaylistDao
import com.pixelvibe.vedioplayer.core.common.util.UiText
import com.pixelvibe.vedioplayer.core.data.db.entity.PlaylistEntity
import com.pixelvibe.vedioplayer.core.data.db.entity.PlaylistVideoEntity
import com.pixelvibe.vedioplayer.core.data.db.entity.VideoEntity
import com.pixelvibe.vedioplayer.core.data.repository.VideoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class HomeTab { ALL, FOLDERS, FAVORITES, IPTV }

data class HomeState(
    val videos: List<VideoEntity> = emptyList(),
    val filteredVideos: List<VideoEntity> = emptyList(),
    val folders: List<String> = emptyList(),
    val selectedTab: HomeTab = HomeTab.ALL,
    val selectedFolder: String? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val error: UiText? = null,
    val showCreatePlaylistDialog: Boolean = false
)

sealed interface HomeAction {
    data class OnSearchQueryChange(val query: String) : HomeAction
    data class OnTabSelected(val tab: HomeTab) : HomeAction
    data class OnFolderSelected(val folder: String) : HomeAction
    data class OnVideoClick(val videoId: String) : HomeAction
    data object OnRetryClick : HomeAction
    data object OnShowCreatePlaylist : HomeAction
    data object OnDismissCreatePlaylist : HomeAction
    data class OnCreatePlaylist(val name: String) : HomeAction
    data class OnAddToPlaylist(val playlistId: String, val videoId: String) : HomeAction
}

sealed interface HomeEvent {
    data class NavigateToPlayer(val videoId: String) : HomeEvent
    data object PlaylistCreated : HomeEvent
}

class HomeViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val videoRepository: VideoRepository,
    private val playlistDao: PlaylistDao
) : ViewModel() {

    private val _state = MutableStateFlow(
        HomeState(
            selectedTab = savedStateHandle.get<String>("selectedTab")?.let { name ->
                try { HomeTab.valueOf(name) } catch (_: Exception) { HomeTab.ALL }
            } ?: HomeTab.ALL,
            searchQuery = savedStateHandle.get<String>("searchQuery") ?: ""
        )
    )
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<HomeEvent>()
    val events = _events.asSharedFlow()

    private var searchJob: Job? = null
    private var videosJob: Job? = null
    private var foldersJob: Job? = null

    init {
        observeVideos()
        observeFolders()
        persistState()
    }

    private fun persistState() {
        viewModelScope.launch {
            _state.collect { s ->
                savedStateHandle["selectedTab"] = s.selectedTab.name
                savedStateHandle["searchQuery"] = s.searchQuery
            }
        }
    }

    fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.OnSearchQueryChange -> onSearchQueryChange(action.query)
            is HomeAction.OnTabSelected -> onTabSelected(action.tab)
            is HomeAction.OnFolderSelected -> onFolderSelected(action.folder)
            is HomeAction.OnVideoClick -> onVideoClick(action.videoId)
            is HomeAction.OnRetryClick -> onRetryClick()
            is HomeAction.OnShowCreatePlaylist -> onShowCreatePlaylist()
            is HomeAction.OnDismissCreatePlaylist -> onDismissCreatePlaylist()
            is HomeAction.OnCreatePlaylist -> onCreatePlaylist(action.name)
            is HomeAction.OnAddToPlaylist -> onAddToPlaylist(action.playlistId, action.videoId)
        }
    }

    private fun observeVideos() {
        videosJob?.cancel()
        videosJob = viewModelScope.launch {
            videoRepository.getAllVideos().collect { videos ->
                _state.update {
                    it.copy(
                        videos = videos,
                        isLoading = false,
                        error = null
                    )
                }
                applyFilters()
            }
        }
    }

    private fun observeFolders() {
        foldersJob?.cancel()
        foldersJob = viewModelScope.launch {
            videoRepository.getFolders().collect { folders ->
                _state.update { it.copy(folders = folders) }
            }
        }
    }

    private fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            applyFilters()
        }
    }

    private fun onTabSelected(tab: HomeTab) {
        _state.update { it.copy(selectedTab = tab, selectedFolder = null) }
        applyFilters()
    }

    private fun onFolderSelected(folder: String) {
        _state.update {
            it.copy(
                selectedTab = HomeTab.FOLDERS,
                selectedFolder = folder
            )
        }
        applyFilters()
    }

    private fun onVideoClick(videoId: String) {
        viewModelScope.launch {
            _events.emit(HomeEvent.NavigateToPlayer(videoId))
        }
    }

    private fun onRetryClick() {
        _state.update { it.copy(isLoading = true, error = null) }
        observeVideos()
    }

    private fun onShowCreatePlaylist() {
        _state.update { it.copy(showCreatePlaylistDialog = true) }
    }

    private fun onDismissCreatePlaylist() {
        _state.update { it.copy(showCreatePlaylistDialog = false) }
    }

    private fun onCreatePlaylist(name: String) {
        viewModelScope.launch {
            val playlist = PlaylistEntity(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                createdAt = System.currentTimeMillis()
            )
            playlistDao.insertPlaylist(playlist)
            _state.update { it.copy(showCreatePlaylistDialog = false) }
            _events.emit(HomeEvent.PlaylistCreated)
        }
    }

    private fun onAddToPlaylist(playlistId: String, videoId: String) {
        viewModelScope.launch {
            val entry = PlaylistVideoEntity(
                playlistId = playlistId,
                videoId = videoId,
                position = 0
            )
            playlistDao.addVideoToPlaylist(entry)
        }
    }

    private fun applyFilters() {
        val s = _state.value
        val filtered = when (s.selectedTab) {
            HomeTab.ALL -> s.videos
            HomeTab.FOLDERS -> {
                if (s.selectedFolder != null) {
                    s.videos.filter { it.folderName == s.selectedFolder }
                } else s.videos
            }
            HomeTab.FAVORITES -> s.videos.filter { it.isFavorite }
            HomeTab.IPTV -> emptyList()
        }

        val query = s.searchQuery.trim().lowercase()
        val result = if (query.isNotEmpty()) {
            filtered.filter { it.title.lowercase().contains(query) }
        } else filtered

        _state.update { it.copy(filteredVideos = result) }
    }
}
