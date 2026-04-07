package com.pixelvibe.vedioplayer.presentation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable

/**
 * Screen interface extending NavKey for Navigation3.
 * Each screen provides its own @Composable Content().
 */
interface Screen : NavKey {
    val title: String

    @Composable
    fun Content(navigateTo: (Screen) -> Unit, onBack: () -> Unit)
}

// ── Screen Definitions ──

@Serializable
data object HomeScreen : Screen {
    override val title: String = "Home"

    @Composable
    override fun Content(navigateTo: (Screen) -> Unit, onBack: () -> Unit) {
        // Placeholder - will be replaced by FolderListScreen in Phase 3
        androidx.compose.material3.Text("Home Screen")
    }
}

@Serializable
data object RecentsScreen : Screen {
    override val title: String = "Recents"

    @Composable
    override fun Content(navigateTo: (Screen) -> Unit, onBack: () -> Unit) {
        // Placeholder - will be replaced by RecentlyPlayedScreen in Phase 3
        androidx.compose.material3.Text("Recents Screen")
    }
}

@Serializable
data object PlaylistsScreen : Screen {
    override val title: String = "Playlists"

    @Composable
    override fun Content(navigateTo: (Screen) -> Unit, onBack: () -> Unit) {
        // Placeholder - will be replaced by PlaylistScreen in Phase 3
        androidx.compose.material3.Text("Playlists Screen")
    }
}

@Serializable
data object NetworkScreen : Screen {
    override val title: String = "Network"

    @Composable
    override fun Content(navigateTo: (Screen) -> Unit, onBack: () -> Unit) {
        // Placeholder - will be replaced by NetworkStreamingScreen in Phase 3
        androidx.compose.material3.Text("Network Screen")
    }
}

@Serializable
data class PlayerScreen(
    val filePath: String? = null
) : Screen {
    override val title: String = "Player"

    @Composable
    override fun Content(navigateTo: (Screen) -> Unit, onBack: () -> Unit) {
        // Player screen handled by PlayerActivity
        androidx.compose.material3.Text("Player Screen: $filePath")
    }
}

@Serializable
data class SettingsScreen(
    val initialDestination: String = ""
) : Screen {
    override val title: String = "Settings"

    @Composable
    override fun Content(navigateTo: (Screen) -> Unit, onBack: () -> Unit) {
        // Placeholder - will be replaced by PreferencesScreen in Phase 4
        androidx.compose.material3.Text("Settings Screen")
    }
}
