package com.pixelvibe.vedioplayer.core.common.route

import android.net.Uri
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {
    @Serializable
    data object Home : Route

    @Serializable
    data object Recent : Route

    @Serializable
    data object Network : Route

    @Serializable
    data object Settings : Route

    @Serializable
    data class Player(val videoId: String) : Route {
        companion object {
            fun createRoute(rawVideoId: String): Player {
                return Player(Uri.encode(rawVideoId))
            }
        }
    }
}
