package com.pixelvibe.vedioplayer.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration

@Stable
class WindowSize(
    val isCompact: Boolean,
    val isTablet: Boolean
)

val LocalWindowSize = compositionLocalOf { WindowSize(isCompact = true, isTablet = false) }

@Composable
fun rememberWindowSize(): WindowSize {
    val config = LocalConfiguration.current
    val widthDp = config.screenWidthDp
    val isTablet = widthDp >= 600
    return WindowSize(isCompact = widthDp < 600, isTablet = isTablet)
}
