package com.yourbrand.lumameter.pro.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = MeterAccentDark,
    onPrimary = MeterBackgroundDark,
    primaryContainer = MeterAccentContainerDark,
    onPrimaryContainer = MeterOnAccentContainerDark,
    secondary = MeterSupportDark,
    tertiary = MeterAccentLockedDark,
    background = MeterBackgroundDark,
    onBackground = Color.White,
    surface = MeterSurfaceDark,
    onSurface = Color.White,
    surfaceVariant = MeterSurfaceVariantDark,
    onSurfaceVariant = Color.White,
    outline = MeterOutlineDark,
    scrim = MeterScrimDark,
)

private val LightColorScheme = lightColorScheme(
    primary = MeterAccentLight,
    onPrimary = MeterOnAccentContainerLight,
    primaryContainer = MeterAccentContainerLight,
    onPrimaryContainer = MeterOnAccentContainerLight,
    secondary = MeterSupportLight,
    tertiary = MeterAccentLockedLight,
    background = MeterBackgroundLight,
    surface = MeterSurfaceLight,
    surfaceVariant = MeterSurfaceVariantLight,
    outline = MeterOutlineLight,
    scrim = MeterScrimLight,
)

@Composable
fun LumaMeterTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = colorScheme.background.copy(alpha = 0.92f).toArgb()

            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides colorScheme.onSurface,
        ) {
            content()
        }
    }
}
