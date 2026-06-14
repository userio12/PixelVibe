package com.pixelvibe.vedioplayer.navigation

import android.net.Uri
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.pixelvibe.vedioplayer.core.common.route.Route
import com.pixelvibe.vedioplayer.feature.home.HomeRoot
import com.pixelvibe.vedioplayer.feature.network.NetworkRoot
import com.pixelvibe.vedioplayer.feature.player.PlayerRoot
import com.pixelvibe.vedioplayer.feature.recent.RecentRoot
import com.pixelvibe.vedioplayer.feature.settings.SettingsScreen

@Composable
fun PixelVibeNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Route.Home,
        modifier = modifier,
        enterTransition = { fadeIn(animationSpec = tween(300)) },
        exitTransition = { fadeOut(animationSpec = tween(300)) }
    ) {
        composable<Route.Home> {
            HomeRoot(
                onVideoClick = { videoId ->
                    navController.navigate(Route.Player.createRoute(videoId))
                }
            )
        }
        composable<Route.Recent> {
            RecentRoot(
                onVideoClick = { videoId ->
                    navController.navigate(Route.Player.createRoute(videoId))
                }
            )
        }
        composable<Route.Network> {
            NetworkRoot(
                onVideoClick = { uri ->
                    navController.navigate(Route.Player.createRoute(uri))
                }
            )
        }
        composable<Route.Settings> {
            SettingsScreen()
        }
        composable<Route.Player>(
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            }
        ) { backStackEntry ->
            val route: Route.Player = backStackEntry.toRoute()
            PlayerRoot(
                videoId = Uri.decode(route.videoId),
                onBackPress = { navController.popBackStack() }
            )
        }
    }
}
