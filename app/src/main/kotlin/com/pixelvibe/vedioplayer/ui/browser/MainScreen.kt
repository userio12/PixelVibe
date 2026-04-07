package com.pixelvibe.vedioplayer.ui.browser

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.pixelvibe.vedioplayer.R
import java.io.File

/**
 * Main screen with 4-tab bottom navigation:
 * Home (Folder browser), Recents, Playlists, Network.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    rootDirectory: File,
    onVideoClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val tabs = listOf(
        TabInfo(stringResource(R.string.tab_home), Icons.Default.Folder),
        TabInfo(stringResource(R.string.tab_recents), Icons.Default.History),
        TabInfo(stringResource(R.string.tab_playlists), Icons.Default.List),
        TabInfo(stringResource(R.string.tab_network), Icons.Default.Wifi)
    )

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = tabs[selectedTab].label,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> FolderListScreen(
                    rootDirectory = rootDirectory,
                    onVideoClick = { file -> onVideoClick(file.absolutePath) }
                )
                1 -> RecentlyPlayedScreen(
                    items = emptyList(),
                    onVideoClick = onVideoClick,
                    onClearHistory = {}
                )
                2 -> PlaylistScreen(
                    playlists = emptyList(),
                    onCreatePlaylist = {},
                    onDeletePlaylist = {},
                    onPlaylistClick = {},
                    onPlayPlaylist = {}
                )
                3 -> NetworkStreamingScreen(
                    connections = emptyList(),
                    onAddConnection = {},
                    onDeleteConnection = {},
                    onConnectionClick = {}
                )
            }
        }
    }
}

private data class TabInfo(val label: String, val icon: ImageVector)
