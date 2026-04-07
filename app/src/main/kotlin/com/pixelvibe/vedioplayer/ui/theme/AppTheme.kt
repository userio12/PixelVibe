package com.pixelvibe.vedioplayer.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Immutable
data class ThemeState(
    val selectedTheme: AppTheme = AppTheme.Dynamic,
    val isDarkTheme: Boolean = true,
    val isAmoledMode: Boolean = false
)

@Composable
fun rememberThemeState(
    theme: AppTheme = AppTheme.Dynamic,
    darkTheme: Boolean = isSystemInDarkTheme(),
    amoled: Boolean = false
): ThemeState {
    return remember(theme, darkTheme, amoled) {
        ThemeState(theme, darkTheme, amoled)
    }
}

/**
 * Converts [AppThemeColors] to Material3 [ColorScheme].
 */
fun AppThemeColors.toColorScheme() = lightColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    primaryContainer = primaryContainer,
    onPrimaryContainer = onPrimaryContainer,
    secondary = secondary,
    onSecondary = onSecondary,
    secondaryContainer = secondaryContainer,
    onSecondaryContainer = onSecondaryContainer,
    tertiary = tertiary,
    onTertiary = onTertiary,
    tertiaryContainer = tertiaryContainer,
    onTertiaryContainer = onTertiaryContainer,
    error = error,
    onError = onError,
    errorContainer = errorContainer,
    onErrorContainer = onErrorContainer,
    background = background,
    onBackground = onBackground,
    surface = surface,
    onSurface = onSurface,
    surfaceVariant = surfaceVariant,
    onSurfaceVariant = onSurfaceVariant,
    outline = outline,
    outlineVariant = outlineVariant,
    scrim = scrim,
    inverseSurface = inverseSurface,
    inverseOnSurface = inverseOnSurface,
    inversePrimary = inversePrimary,
    surfaceTint = surfaceTint,
)

/**
 * Resolves the color scheme based on theme, dark mode, Material You, and AMOLED settings.
 */
@Composable
fun resolveColorScheme(
    theme: AppTheme,
    darkTheme: Boolean,
    amoled: Boolean
): androidx.compose.material3.ColorScheme {
    val context = LocalContext.current

    // Material You dynamic color on Android 12+
    if (theme is AppTheme.Dynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val dynamicScheme = if (darkTheme) {
            dynamicDarkColorScheme(context)
        } else {
            dynamicLightColorScheme(context)
        }

        return if (amoled && darkTheme) {
            dynamicScheme.copy(
                background = Color.Black,
                surface = Color.Black,
                surfaceVariant = Color.Black,
            )
        } else {
            dynamicScheme
        }
    }

    val colors = theme.getColors(isDark = darkTheme, isAmoled = amoled)

    val baseScheme = if (darkTheme) {
        darkColorScheme(
            primary = colors.primary,
            onPrimary = colors.onPrimary,
            primaryContainer = colors.primaryContainer,
            onPrimaryContainer = colors.onPrimaryContainer,
            secondary = colors.secondary,
            onSecondary = colors.onSecondary,
            secondaryContainer = colors.secondaryContainer,
            onSecondaryContainer = colors.onSecondaryContainer,
            tertiary = colors.tertiary,
            onTertiary = colors.onTertiary,
            tertiaryContainer = colors.tertiaryContainer,
            onTertiaryContainer = colors.onTertiaryContainer,
            error = colors.error,
            onError = colors.onError,
            errorContainer = colors.errorContainer,
            onErrorContainer = colors.onErrorContainer,
            background = if (amoled) Color.Black else colors.background,
            onBackground = colors.onBackground,
            surface = if (amoled) Color.Black else colors.surface,
            onSurface = colors.onSurface,
            surfaceVariant = if (amoled) Color.Black else colors.surfaceVariant,
            onSurfaceVariant = colors.onSurfaceVariant,
            outline = colors.outline,
            outlineVariant = colors.outlineVariant,
            scrim = colors.scrim,
            inverseSurface = colors.inverseSurface,
            inverseOnSurface = colors.inverseOnSurface,
            inversePrimary = colors.inversePrimary,
            surfaceTint = colors.surfaceTint,
        )
    } else {
        lightColorScheme(
            primary = colors.primary,
            onPrimary = colors.onPrimary,
            primaryContainer = colors.primaryContainer,
            onPrimaryContainer = colors.onPrimaryContainer,
            secondary = colors.secondary,
            onSecondary = colors.onSecondary,
            secondaryContainer = colors.secondaryContainer,
            onSecondaryContainer = colors.onSecondaryContainer,
            tertiary = colors.tertiary,
            onTertiary = colors.onTertiary,
            tertiaryContainer = colors.tertiaryContainer,
            onTertiaryContainer = colors.onTertiaryContainer,
            error = colors.error,
            onError = colors.onError,
            errorContainer = colors.errorContainer,
            onErrorContainer = colors.onErrorContainer,
            background = colors.background,
            onBackground = colors.onBackground,
            surface = colors.surface,
            onSurface = colors.onSurface,
            surfaceVariant = colors.surfaceVariant,
            onSurfaceVariant = colors.onSurfaceVariant,
            outline = colors.outline,
            outlineVariant = colors.outlineVariant,
            scrim = colors.scrim,
            inverseSurface = colors.inverseSurface,
            inverseOnSurface = colors.inverseOnSurface,
            inversePrimary = colors.inversePrimary,
            surfaceTint = colors.surfaceTint,
        )
    }

    return baseScheme
}
