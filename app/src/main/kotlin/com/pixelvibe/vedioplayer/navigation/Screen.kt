package com.pixelvibe.vedioplayer.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Recent : Screen("recent")
    data object Network : Screen("network")
    data object Settings : Screen("settings")
    data object Player : Screen("player/{videoId}") {
        fun createRoute(videoId: String) = "player/$videoId"
    }
}
