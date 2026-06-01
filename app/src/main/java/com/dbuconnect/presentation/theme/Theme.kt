package com.dbuconnect.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = BackgroundWhite,
    primaryContainer = PrimaryGreenContainer,
    onPrimaryContainer = PrimaryGreenDark,
    secondary = PrimaryGreen,
    onSecondary = BackgroundWhite,
    secondaryContainer = PrimaryGreenContainer,
    onSecondaryContainer = PrimaryGreenDark,
    tertiary = BrandPurple,
    onTertiary = BackgroundWhite,
    error = StatusError,
    onError = BackgroundWhite,
    errorContainer = AccentPinkContainer,
    onErrorContainer = AccentPink,
    background = BackgroundPrimary,
    onBackground = TextPrimary,
    surface = BackgroundWhite,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceMuted,
    onSurfaceVariant = TextSecondary,
    outline = BorderDefault,
    outlineVariant = BorderDefault,
    inverseSurface = TextPrimary,
    inverseOnSurface = BackgroundWhite,
    surfaceTint = PrimaryGreen
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreenLight,
    onPrimary = DarkBackground,
    primaryContainer = PrimaryGreenDark,
    onPrimaryContainer = PrimaryGreenContainer,
    secondary = PrimaryGreenLight,
    onSecondary = DarkBackground,
    secondaryContainer = PrimaryGreenDark,
    onSecondaryContainer = PrimaryGreenContainer,
    tertiary = BrandPurple,
    onTertiary = BackgroundWhite,
    error = AccentPinkLight,
    onError = DarkBackground,
    errorContainer = AccentPink,
    onErrorContainer = AccentPinkContainer,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkCard,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkBorder,
    inverseSurface = DarkTextPrimary,
    inverseOnSurface = DarkBackground,
    surfaceTint = PrimaryGreenLight
)

@Composable
fun DBUConnectTheme(
    darkTheme: Boolean = ThemeManager.userDarkModeOverride ?: isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    isAppInDarkTheme = darkTheme
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DBUTypography,
        shapes = DBUShapes,
        content = content
    )
}
