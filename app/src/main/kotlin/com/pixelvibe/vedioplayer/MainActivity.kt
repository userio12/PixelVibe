package com.pixelvibe.vedioplayer

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.Surface
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
import com.pixelvibe.vedioplayer.database.PixelVibeDatabase
import com.pixelvibe.vedioplayer.ui.browser.FolderListScreen
import com.pixelvibe.vedioplayer.ui.browser.NetworkStreamingScreen
import com.pixelvibe.vedioplayer.ui.browser.PlaylistScreen
import com.pixelvibe.vedioplayer.ui.browser.RecentlyPlayedScreen
import com.pixelvibe.vedioplayer.ui.theme.PixelVibeTheme
import org.koin.android.ext.android.get
import java.io.File

class MainActivity : ComponentActivity() {

    private val database: PixelVibeDatabase by lazy { get() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PixelVibeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        recentlyPlayedDao = database.recentlyPlayedDao(),
                        playlistDao = database.playlistDao(),
                        networkDao = database.networkConnectionDao(),
                        onVideoClick = { filePath ->
                            launchPlayer(filePath)
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.data?.let { uri ->
            launchPlayer(uri.toString())
        }
    }

    private fun launchPlayer(filePath: String) {
        val intent = Intent(this, PlayerActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = android.net.Uri.parse(filePath)
        }
        startActivity(intent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    recentlyPlayedDao: com.pixelvibe.vedioplayer.database.dao.RecentlyPlayedDao,
    playlistDao: com.pixelvibe.vedioplayer.database.dao.PlaylistDao,
    networkDao: com.pixelvibe.vedioplayer.database.dao.NetworkConnectionDao,
    onVideoClick: (String) -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val defaultVideoDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        ?: Environment.getExternalStorageDirectory()

    val tabs = listOf(
        TabItem(stringResource(R.string.tab_home), Icons.Default.Folder),
        TabItem(stringResource(R.string.tab_recents), Icons.Default.History),
        TabItem(stringResource(R.string.tab_playlists), Icons.Default.List),
        TabItem(stringResource(R.string.tab_network), Icons.Default.Wifi),
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
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
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                tabs = tabs
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> HomeTabContent(rootDirectory = defaultVideoDir, onVideoClick = onVideoClick)
                1 -> RecentsTabContent(dao = recentlyPlayedDao, onVideoClick = onVideoClick)
                2 -> PlaylistsTabContent(dao = playlistDao, onVideoClick = onVideoClick)
                3 -> NetworkTabContent(dao = networkDao, onVideoClick = onVideoClick)
            }
        }
    }
}

@Composable
private fun HomeTabContent(rootDirectory: File, onVideoClick: (String) -> Unit) {
    FolderListScreen(
        rootDirectory = rootDirectory,
        onVideoClick = { file -> onVideoClick(file.absolutePath) }
    )
}

@Composable
private fun RecentsTabContent(
    dao: com.pixelvibe.vedioplayer.database.dao.RecentlyPlayedDao,
    onVideoClick: (String) -> Unit
) {
    val recentItems by androidx.compose.runtime.livedata.observeAsState<List<com.pixelvibe.vedioplayer.database.entities.RecentlyPlayedEntity>>()
    // In production, use collectAsState() from Flow
    // For now, placeholder - the Flow-based collection would be done via ViewModel
    RecentlyPlayedScreen(
        items = emptyList(),
        onVideoClick = onVideoClick,
        onClearHistory = { /* TODO */ }
    )
}

@Composable
private fun PlaylistsTabContent(
    dao: com.pixelvibe.vedioplayer.database.dao.PlaylistDao,
    onVideoClick: (String) -> Unit
) {
    PlaylistScreen(
        playlists = emptyList(), // TODO: collect from Flow
        onCreatePlaylist = { /* TODO */ },
        onDeletePlaylist = { /* TODO */ },
        onPlaylistClick = { /* TODO */ },
        onPlayPlaylist = { /* TODO */ }
    )
}

@Composable
private fun NetworkTabContent(
    dao: com.pixelvibe.vedioplayer.database.dao.NetworkConnectionDao,
    onVideoClick: (String) -> Unit
) {
    NetworkStreamingScreen(
        connections = emptyList(), // TODO: collect from Flow
        onAddConnection = { /* TODO */ },
        onDeleteConnection = { /* TODO */ },
        onConnectionClick = { /* TODO */ }
    )
}

@Composable
fun BottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    tabs: List<TabItem>
) {
    NavigationBar {
        tabs.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = selectedTab == index,
                onClick = { onTabSelected(index) }
            )
        }
    }
}

data class TabItem(val label: String, val icon: ImageVector)
