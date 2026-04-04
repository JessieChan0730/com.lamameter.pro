package com.yourbrand.lumameter.pro.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = MeterGreenDark,
    onPrimary = MeterBackgroundDark,
    primaryContainer = MeterGreenContainerDark,
    onPrimaryContainer = MeterGreenDark,
    secondary = MeterTealDark,
    tertiary = MeterAmberDark,
    background = MeterBackgroundDark,
    surface = MeterSurfaceDark,
    surfaceVariant = MeterSurfaceVariantDark,
    outline = MeterOutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = MeterGreenLight,
    onPrimary = Color.White,
    primaryContainer = MeterGreenContainerLight,
    onPrimaryContainer = MeterGreenContainerDark,
    secondary = MeterTealLight,
    tertiary = MeterAmberLight,
    background = MeterBackgroundLight,
    surface = MeterSurfaceLight,
    surfaceVariant = MeterSurfaceVariantLight,
    outline = MeterOutlineLight
)

@Composable
fun LumaMeterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor -> if (darkTheme) DarkColorScheme else LightColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
