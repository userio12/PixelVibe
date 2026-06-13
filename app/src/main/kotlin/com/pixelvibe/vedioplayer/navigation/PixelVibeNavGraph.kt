package com.pixelvibe.vedioplayer.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
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
        startDestination = Screen.Home.route,
        modifier = modifier,
        enterTransition = { fadeIn(animationSpec = tween(300)) },
        exitTransition = { fadeOut(animationSpec = tween(300)) }
    ) {
        composable(Screen.Home.route) {
            HomeRoot(
                onVideoClick = { videoId ->
                    navController.navigate(Screen.Player.createRoute(videoId))
                }
            )
        }
        composable(Screen.Recent.route) {
            RecentRoot(
                onVideoClick = { videoId ->
                    navController.navigate(Screen.Player.createRoute(videoId))
                }
            )
        }
        composable(Screen.Network.route) {
            NetworkRoot(
                onVideoClick = { uri ->
                    navController.navigate(Screen.Player.createRoute(uri))
                }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
        composable(
            route = "player/{videoId}",
            arguments = listOf(navArgument("videoId") { type = NavType.StringType }),
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
            val videoId = backStackEntry.arguments?.getString("videoId") ?: return@composable
            PlayerRoot(
                videoId = videoId,
                onBackPress = { navController.popBackStack() }
            )
        }
    }
}
