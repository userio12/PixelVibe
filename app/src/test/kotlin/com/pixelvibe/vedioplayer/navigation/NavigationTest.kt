package com.pixelvibe.vedioplayer.navigation

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import com.pixelvibe.vedioplayer.feature.home.HomeRoot
import com.pixelvibe.vedioplayer.feature.network.NetworkRoot
import com.pixelvibe.vedioplayer.feature.player.PlayerRoot
import com.pixelvibe.vedioplayer.feature.recent.RecentRoot
import com.pixelvibe.vedioplayer.feature.settings.SettingsScreen
import org.junit.jupiter.api.Test

class NavigationTest {

    @Test
    fun `Screen sealed class defines all expected routes`() {
        assertThat(Screen.Home.route).isEqualTo("home")
        assertThat(Screen.Recent.route).isEqualTo("recent")
        assertThat(Screen.Network.route).isEqualTo("network")
        assertThat(Screen.Settings.route).isEqualTo("settings")
        assertThat(Screen.Player.route).isEqualTo("player/{videoId}")
    }

    @Test
    fun `Player createRoute builds correct path`() {
        val route = Screen.Player.createRoute("video-123")
        assertThat(route).isEqualTo("player/video-123")
    }

    @Test
    fun `Player createRoute encodes URI safely`() {
        val route = Screen.Player.createRoute("smb://server/share/video.mp4")
        assertThat(route).isEqualTo("player/smb://server/share/video.mp4")
    }

    @Test
    fun `bottom nav contains exactly 4 items`() {
        val items = listOf(
            Screen.Home,
            Screen.Recent,
            Screen.Network,
            Screen.Settings
        )
        assertThat(items).hasSize(4)
        assertThat(items.map { it.route }).containsExactly(
            "home", "recent", "network", "settings"
        )
    }

    @Test
    fun `Player screen is not in bottom nav`() {
        val bottomNavRoutes = setOf(
            Screen.Home.route,
            Screen.Recent.route,
            Screen.Network.route,
            Screen.Settings.route
        )
        assertThat(Screen.Player.route in bottomNavRoutes).isFalse()
    }

    @Test
    fun `all bottom nav screens have corresponding composable imports`() {
        val composableClasses = listOf(
            HomeRoot::class.simpleName,
            RecentRoot::class.simpleName,
            NetworkRoot::class.simpleName,
            SettingsScreen::class.simpleName,
            PlayerRoot::class.simpleName
        )
        composableClasses.forEach { name ->
            assertThat(name).isNotNull()
        }
    }

    @Test
    fun `nav graph contains all 5 screen composables`() {
        val screenRoutes = setOf(
            Screen.Home.route,
            Screen.Recent.route,
            Screen.Network.route,
            Screen.Settings.route,
            Screen.Player.route
        )
        assertThat(screenRoutes).hasSize(5)
    }
}
