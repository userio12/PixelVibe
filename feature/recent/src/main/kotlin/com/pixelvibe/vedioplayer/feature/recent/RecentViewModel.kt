package com.pixelvibe.vedioplayer.feature.recent

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelvibe.vedioplayer.core.common.util.UiText
import com.pixelvibe.vedioplayer.core.data.db.dao.HistoryDao
import com.pixelvibe.vedioplayer.core.data.db.entity.HistoryEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecentState(
    val history: List<HistoryEntity> = emptyList(),
    val isLoading: Boolean = true,
    val error: UiText? = null
)

sealed interface RecentAction {
    data class OnHistoryClick(val videoId: String) : RecentAction
    data class OnDeleteEntry(val entryId: String) : RecentAction
    data object OnClearAll : RecentAction
    data object OnRetryClick : RecentAction
}

sealed interface RecentEvent {
    data class NavigateToPlayer(val videoId: String) : RecentEvent
}

class RecentViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val historyDao: HistoryDao
) : ViewModel() {

    private val _state = MutableStateFlow(
        RecentState(isLoading = savedStateHandle.get<Boolean>("isLoading") ?: true)
    )
    val state: StateFlow<RecentState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<RecentEvent>()
    val events = _events.asSharedFlow()

    private var historyJob: Job? = null

    init {
        observeHistory()
        persistState()
    }

    private fun persistState() {
        viewModelScope.launch {
            _state.collect { s ->
                savedStateHandle["isLoading"] = s.isLoading
            }
        }
    }

    fun onAction(action: RecentAction) {
        when (action) {
            is RecentAction.OnHistoryClick -> onHistoryClick(action.videoId)
            is RecentAction.OnDeleteEntry -> onDeleteEntry(action.entryId)
            is RecentAction.OnClearAll -> onClearAll()
            is RecentAction.OnRetryClick -> onRetryClick()
        }
    }

    private fun observeHistory() {
        historyJob?.cancel()
        historyJob = viewModelScope.launch {
            historyDao.getAllHistory().collect { history ->
                _state.update {
                    it.copy(
                        history = history,
                        isLoading = false,
                        error = null
                    )
                }
            }
        }
    }

    private fun onHistoryClick(videoId: String) {
        viewModelScope.launch {
            _events.emit(RecentEvent.NavigateToPlayer(videoId))
        }
    }

    private fun onDeleteEntry(entryId: String) {
        viewModelScope.launch {
            historyDao.deleteById(entryId)
        }
    }

    private fun onClearAll() {
        viewModelScope.launch {
            historyDao.deleteAll()
        }
    }

    private fun onRetryClick() {
        _state.update { it.copy(isLoading = true, error = null) }
        observeHistory()
    }
}
