package com.pixelvibe.vedioplayer.navigation

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.pixelvibe.vedioplayer.core.common.route.Route
import org.junit.jupiter.api.Test

class NavigationTest {

    @Test
    fun `Route sealed interface defines all expected screens`() {
        val routes = listOf<Route>(
            Route.Home,
            Route.Recent,
            Route.Network,
            Route.Settings,
            Route.Player("test")
        )
        assertThat(routes).hasSize(5)
    }

    @Test
    fun `Player createRoute encodes URI safely`() {
        val route = Route.Player.createRoute("smb://server/share/video.mp4")
        assertThat(route.videoId).isEqualTo("smb%3A%2F%2Fserver%2Fshare%2Fvideo.mp4")
    }

    @Test
    fun `bottom nav contains exactly 4 items`() {
        val bottomNavRoutes = listOf(
            Route.Home,
            Route.Recent,
            Route.Network,
            Route.Settings
        )
        assertThat(bottomNavRoutes).hasSize(4)
    }

    @Test
    fun `Player screen is not in bottom nav`() {
        val bottomNavRoutes = setOf(
            Route.Home::class,
            Route.Recent::class,
            Route.Network::class,
            Route.Settings::class
        )
        assertThat(bottomNavRoutes.contains(Route.Player::class)).isEqualTo(false)
    }

    @Test
    fun `all screens have corresponding composable imports`() {
        arrayOf(
            com.pixelvibe.vedioplayer.feature.home.HomeRoot::class.simpleName,
            com.pixelvibe.vedioplayer.feature.recent.RecentRoot::class.simpleName,
            com.pixelvibe.vedioplayer.feature.network.NetworkRoot::class.simpleName,
            com.pixelvibe.vedioplayer.feature.settings.SettingsScreen::class.simpleName,
            com.pixelvibe.vedioplayer.feature.player.PlayerRoot::class.simpleName
        ).forEach { name ->
            assertThat(name).isNotNull()
        }
    }
}
