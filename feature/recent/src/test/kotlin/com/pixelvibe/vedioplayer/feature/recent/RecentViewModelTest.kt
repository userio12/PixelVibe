package com.pixelvibe.vedioplayer.feature.recent

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.pixelvibe.vedioplayer.core.data.db.dao.HistoryDao
import com.pixelvibe.vedioplayer.core.data.db.entity.HistoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private class FakeHistoryDao(
    private val history: MutableStateFlow<List<HistoryEntity>> = MutableStateFlow(emptyList())
) : HistoryDao {
    override fun getAllHistory() = history
    override suspend fun insert(entry: HistoryEntity) { history.value = history.value + entry }
    override suspend fun deleteById(id: String) { history.value = history.value.filter { it.id != id } }
    override suspend fun deleteAll() { history.value = emptyList() }
    override suspend fun getByVideoId(videoId: String) = history.value.find { it.videoId == videoId }
}

@OptIn(ExperimentalCoroutinesApi::class)
class RecentViewModelTest {

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
    fun `initial state loads history and becomes not loading`() {
        val dao = FakeHistoryDao()
        val vm = RecentViewModel(SavedStateHandle(), dao)
        assertThat(vm.state.value.isLoading).isFalse()
        assertThat(vm.state.value.history).isEqualTo(emptyList())
    }

    @Test
    fun `history click emits player event`() = runTest(testDispatcher) {
        val dao = FakeHistoryDao()
        val vm = RecentViewModel(SavedStateHandle(), dao)
        vm.events.test {
            vm.onAction(RecentAction.OnHistoryClick("video-1"))
            val event = awaitItem()
            assertThat(event).isEqualTo(RecentEvent.NavigateToPlayer("video-1"))
        }
    }
}
