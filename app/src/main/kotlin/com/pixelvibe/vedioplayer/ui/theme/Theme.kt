package com.pixelvibe.vedioplayer.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val LocalPlayerColors = staticCompositionLocalOf<AppThemeColors> {
    DefaultDark
}

@Composable
fun PixelVibeTheme(
    theme: AppTheme = AppTheme.Dynamic,
    darkTheme: Boolean = isSystemInDarkTheme(),
    amoledMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val colorScheme: ColorScheme = if (theme is AppTheme.Dynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val dynamicScheme = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        if (amoledMode && darkTheme) {
            dynamicScheme.copy(
                background = Color.Black,
                surface = Color.Black,
                surfaceVariant = Color.Black,
            )
        } else {
            dynamicScheme
        }
    } else {
        resolveColorScheme(theme = theme, darkTheme = darkTheme, amoled = amoledMode)
    }

    val themeColors = if (theme is AppTheme.Dynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // Map dynamic colors back to AppThemeColors for LocalPlayerColors
        AppThemeColors(
            primary = colorScheme.primary,
            onPrimary = colorScheme.onPrimary,
            primaryContainer = colorScheme.primaryContainer,
            onPrimaryContainer = colorScheme.onPrimaryContainer,
            secondary = colorScheme.secondary,
            onSecondary = colorScheme.onSecondary,
            secondaryContainer = colorScheme.secondaryContainer,
            onSecondaryContainer = colorScheme.onSecondaryContainer,
            tertiary = colorScheme.tertiary,
            onTertiary = colorScheme.onTertiary,
            tertiaryContainer = colorScheme.tertiaryContainer,
            onTertiaryContainer = colorScheme.onTertiaryContainer,
            error = colorScheme.error,
            onError = colorScheme.onError,
            errorContainer = colorScheme.errorContainer,
            onErrorContainer = colorScheme.onErrorContainer,
            background = colorScheme.background,
            onBackground = colorScheme.onBackground,
            surface = colorScheme.surface,
            onSurface = colorScheme.onSurface,
            surfaceVariant = colorScheme.surfaceVariant,
            onSurfaceVariant = colorScheme.onSurfaceVariant,
            outline = colorScheme.outline,
            outlineVariant = colorScheme.outlineVariant,
            scrim = colorScheme.scrim,
            inverseSurface = colorScheme.inverseSurface,
            inverseOnSurface = colorScheme.inverseOnSurface,
            inversePrimary = colorScheme.inversePrimary,
            surfaceTint = colorScheme.surfaceTint,
        )
    } else {
        theme.getColors(isDark = darkTheme, isAmoled = amoledMode)
    }

    CompositionLocalProvider(LocalPlayerColors provides themeColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = PixelVibeTypography,
            content = content
        )
    }
}
