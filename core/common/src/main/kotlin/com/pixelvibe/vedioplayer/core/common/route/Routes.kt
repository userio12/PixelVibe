package com.pixelvibe.vedioplayer.core.common.route

sealed interface Route {
    data object Home : Route
    data object Recent : Route
    data object Network : Route
    data object Settings : Route
    data class Player(val videoId: String) : Route
}
