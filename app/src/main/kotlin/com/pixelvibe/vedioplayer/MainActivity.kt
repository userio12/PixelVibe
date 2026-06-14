package com.pixelvibe.vedioplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.hasRoute
import com.pixelvibe.vedioplayer.core.common.route.Route
import com.pixelvibe.vedioplayer.core.data.security.AppLockManager
import com.pixelvibe.vedioplayer.core.data.security.ThemePreferences
import com.pixelvibe.vedioplayer.core.player.pip.PipHandler
import com.pixelvibe.vedioplayer.core.ui.theme.PixelVibeTheme
import com.pixelvibe.vedioplayer.navigation.PixelVibeNavGraph
import com.pixelvibe.vedioplayer.security.AppLockGate
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.compose.koinInject
import org.koin.java.KoinJavaComponent.get as koinGet

private val isPlayerActive = MutableStateFlow(false)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themePrefs = koinInject<ThemePreferences>()
            val amoled by themePrefs.isAmoledTheme.collectAsStateWithLifecycle(false)
            PixelVibeTheme(amoledTheme = amoled) {
                val appLockManager = koinInject<AppLockManager>()
                AppLockGate(
                    appLockManager = appLockManager,
                    onUnlocked = { }
                ) {
                    MainScreen()
                }
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (isPlayerActive.value) {
            val pipHandler = koinGet<PipHandler>(PipHandler::class.java)
            pipHandler.enterPipMode(this)
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val onPlayer = currentDestination?.hasRoute<Route.Player>() ?: false
    isPlayerActive.value = onPlayer

    val bottomNavItems = listOf(
        BottomNavItem("Home", Icons.Filled.Home, Route.Home),
        BottomNavItem("Recent", Icons.Filled.History, Route.Recent),
        BottomNavItem("Network", Icons.Filled.Folder, Route.Network),
        BottomNavItem("Settings", Icons.Filled.Settings, Route.Settings)
    )

    Scaffold(
        bottomBar = {
            if (!onPlayer) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy?.any { dest ->
                                when (item.route) {
                                    is Route.Home -> dest.hasRoute<Route.Home>()
                                    is Route.Recent -> dest.hasRoute<Route.Recent>()
                                    is Route.Network -> dest.hasRoute<Route.Network>()
                                    is Route.Settings -> dest.hasRoute<Route.Settings>()
                                    else -> false
                                }
                            } == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        PixelVibeNavGraph(
            navController = navController,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

private data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: Route
)
