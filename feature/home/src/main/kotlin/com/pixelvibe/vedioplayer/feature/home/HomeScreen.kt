package com.pixelvibe.vedioplayer.feature.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pixelvibe.vedioplayer.core.ui.component.ObserveAsEvents
import com.pixelvibe.vedioplayer.core.ui.component.EmptyView
import com.pixelvibe.vedioplayer.core.ui.component.ErrorView
import com.pixelvibe.vedioplayer.core.ui.component.LoadingIndicator
import com.pixelvibe.vedioplayer.feature.home.component.CategoryTabRow
import com.pixelvibe.vedioplayer.feature.home.component.FolderTab
import com.pixelvibe.vedioplayer.feature.home.component.VideoGrid
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeRoot(
    onVideoClick: (String) -> Unit = {},
    viewModel: HomeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val context = LocalContext.current
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is HomeEvent.NavigateToPlayer -> onVideoClick(event.videoId)
            is HomeEvent.PlaylistCreated ->
                Toast.makeText(context, "Playlist created", Toast.LENGTH_SHORT).show()
        }
    }

    HomeScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeState,
    onAction: (HomeAction) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { onAction(HomeAction.OnSearchQueryChange(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            placeholder = { Text("Search videos...") },
            singleLine = true
        )

        CategoryTabRow(
            selectedTab = state.selectedTab,
            onTabSelected = { onAction(HomeAction.OnTabSelected(it)) }
        )

        if (state.selectedTab == HomeTab.FOLDERS && state.folders.isNotEmpty()) {
            FolderTab(
                folders = state.folders,
                selectedFolder = state.selectedFolder,
                onFolderSelected = { onAction(HomeAction.OnFolderSelected(it)) }
            )
        }

        when {
            state.isLoading -> LoadingIndicator(message = "Scanning videos...")
            state.error != null -> ErrorView(
                message = state.error!!,
                onRetry = { onAction(HomeAction.OnRetryClick) }
            )
            state.filteredVideos.isEmpty() -> {
                EmptyView(
                    message = if (state.searchQuery.isNotEmpty()) "No videos match your search"
                    else when (state.selectedTab) {
                        HomeTab.FAVORITES -> "No favorite videos yet"
                        HomeTab.IPTV -> "IPTV support coming in a future update"
                        else -> "No videos found"
                    }
                )
            }
            else -> VideoGrid(
                videos = state.filteredVideos,
                onVideoClick = { onAction(HomeAction.OnVideoClick(it)) }
            )
        }
    }

    if (state.showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = { onAction(HomeAction.OnDismissCreatePlaylist) },
            onCreate = { name -> onAction(HomeAction.OnCreatePlaylist(name)) }
        )
    }
}

@Composable
private fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var playlistName by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Playlist") },
        text = {
            OutlinedTextField(
                value = playlistName,
                onValueChange = { playlistName = it },
                placeholder = { Text("Playlist name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(playlistName) },
                enabled = playlistName.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
