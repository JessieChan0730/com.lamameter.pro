package com.yourbrand.lumameter.pro.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

data class MeterPalettePreview(
    val background: Color,
    val surface: Color,
    val accent: Color,
)

data class MeterColorTokens(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val outlineVariant: Color,
    val scrim: Color,
)

data class MeterThemePalette(
    val light: MeterColorTokens,
    val dark: MeterColorTokens,
) {
    fun colorScheme(darkTheme: Boolean): ColorScheme {
        return if (darkTheme) {
            dark.toDarkColorScheme()
        } else {
            light.toLightColorScheme()
        }
    }

    fun preview(darkTheme: Boolean): MeterPalettePreview {
        val tokens = if (darkTheme) dark else light
        return MeterPalettePreview(
            background = tokens.background,
            surface = tokens.surface,
            accent = tokens.primary,
        )
    }
}

private fun MeterColorTokens.toDarkColorScheme(): ColorScheme {
    return darkColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = tertiary,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        outline = outline,
        outlineVariant = outlineVariant,
        scrim = scrim,
    )
}

private fun MeterColorTokens.toLightColorScheme(): ColorScheme {
    return lightColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = tertiary,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        outline = outline,
        outlineVariant = outlineVariant,
        scrim = scrim,
    )
}

val ClassicAmberPalette = MeterThemePalette(
    light = MeterColorTokens(
        primary = Color(0xFFE0B43B),
        onPrimary = Color(0xFF433100),
        primaryContainer = Color(0xFFF8E9B5),
        onPrimaryContainer = Color(0xFF433100),
        secondary = Color(0xFF6E675C),
        secondaryContainer = Color(0xFFE8E1D4),
        onSecondaryContainer = Color(0xFF433F36),
        tertiary = Color(0xFFD9AA2E),
        background = Color(0xFFF3F1EC),
        onBackground = Color(0xFF1C1B17),
        surface = Color(0xFFFFFEFB),
        onSurface = Color(0xFF1C1B17),
        surfaceVariant = Color(0xFFF0ECE5),
        onSurfaceVariant = Color(0xFF4D473E),
        outline = Color(0xFFBBB2A5),
        outlineVariant = Color(0xFFD8D0C4),
        scrim = Color(0xFFD7D1C6),
    ),
    dark = MeterColorTokens(
        primary = Color(0xFFF0C247),
        onPrimary = Color(0xFF201800),
        primaryContainer = Color(0xFF4A3A0A),
        onPrimaryContainer = Color(0xFFFDE8A6),
        secondary = Color(0xFFCDC5B7),
        secondaryContainer = Color(0xFF3A352D),
        onSecondaryContainer = Color(0xFFF0E9DB),
        tertiary = Color(0xFFF6CB58),
        background = Color(0xFF0E0E10),
        onBackground = Color(0xFFF3EFE7),
        surface = Color(0xFF18181B),
        onSurface = Color(0xFFF3EFE7),
        surfaceVariant = Color(0xFF232327),
        onSurfaceVariant = Color(0xFFD1CCC2),
        outline = Color(0xFF8F887B),
        outlineVariant = Color(0xFF4A453C),
        scrim = Color(0xFF09090A),
    ),
)

val CyanTechPalette = MeterThemePalette(
    light = MeterColorTokens(
        primary = Color(0xFF147E75),
        onPrimary = Color(0xFFF4FFFD),
        primaryContainer = Color(0xFFB8F0EA),
        onPrimaryContainer = Color(0xFF062C28),
        secondary = Color(0xFF566A72),
        secondaryContainer = Color(0xFFD9E5E8),
        onSecondaryContainer = Color(0xFF142025),
        tertiary = Color(0xFF0E9F94),
        background = Color(0xFFF3F7F7),
        onBackground = Color(0xFF141A1B),
        surface = Color(0xFFFCFEFE),
        onSurface = Color(0xFF141A1B),
        surfaceVariant = Color(0xFFE5F0F0),
        onSurfaceVariant = Color(0xFF4A5B62),
        outline = Color(0xFF8A9CA4),
        outlineVariant = Color(0xFFC5D1D6),
        scrim = Color(0xFFD4DDDF),
    ),
    dark = MeterColorTokens(
        primary = Color(0xFF2EC4B6),
        onPrimary = Color(0xFF071411),
        primaryContainer = Color(0xFF123D3A),
        onPrimaryContainer = Color(0xFFC5FFF7),
        secondary = Color(0xFFA8B6BE),
        secondaryContainer = Color(0xFF2A353C),
        onSecondaryContainer = Color(0xFFE2EDF2),
        tertiary = Color(0xFF69E7DB),
        background = Color(0xFF121417),
        onBackground = Color(0xFFF2F7F8),
        surface = Color(0xFF1A1F24),
        onSurface = Color(0xFFF2F7F8),
        surfaceVariant = Color(0xFF242C33),
        onSurfaceVariant = Color(0xFFC3CED3),
        outline = Color(0xFF73818A),
        outlineVariant = Color(0xFF364048),
        scrim = Color(0xFF080A0B),
    ),
)

val NavyOrangePalette = MeterThemePalette(
    light = MeterColorTokens(
        primary = Color(0xFFB65300),
        onPrimary = Color(0xFFFFF8F3),
        primaryContainer = Color(0xFFFFD9BC),
        onPrimaryContainer = Color(0xFF391700),
        secondary = Color(0xFF54657D),
        secondaryContainer = Color(0xFFDBE5F7),
        onSecondaryContainer = Color(0xFF15202E),
        tertiary = Color(0xFFE07B23),
        background = Color(0xFFF5F7FC),
        onBackground = Color(0xFF111A2B),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF111A2B),
        surfaceVariant = Color(0xFFE8EEF9),
        onSurfaceVariant = Color(0xFF506075),
        outline = Color(0xFF8997AA),
        outlineVariant = Color(0xFFC9D3E1),
        scrim = Color(0xFFD6DCE8),
    ),
    dark = MeterColorTokens(
        primary = Color(0xFFFF7A00),
        onPrimary = Color(0xFF381A00),
        primaryContainer = Color(0xFF5B2B00),
        onPrimaryContainer = Color(0xFFFFE3C2),
        secondary = Color(0xFFB7C5E0),
        secondaryContainer = Color(0xFF263455),
        onSecondaryContainer = Color(0xFFECF1FA),
        tertiary = Color(0xFFFFB14A),
        background = Color(0xFF0B132B),
        onBackground = Color(0xFFF0F4FA),
        surface = Color(0xFF111B38),
        onSurface = Color(0xFFF0F4FA),
        surfaceVariant = Color(0xFF1A2747),
        onSurfaceVariant = Color(0xFFCFD8E8),
        outline = Color(0xFF76839D),
        outlineVariant = Color(0xFF36425E),
        scrim = Color(0xFF070B18),
    ),
)

val IndustrialGreenPalette = MeterThemePalette(
    light = MeterColorTokens(
        primary = Color(0xFF007A4A),
        onPrimary = Color(0xFFF4FFF8),
        primaryContainer = Color(0xFFB1F7D2),
        onPrimaryContainer = Color(0xFF002111),
        secondary = Color(0xFF5C6A62),
        secondaryContainer = Color(0xFFDDE7E1),
        onSecondaryContainer = Color(0xFF16211B),
        tertiary = Color(0xFF009E61),
        background = Color(0xFFF4F7F4),
        onBackground = Color(0xFF0D1510),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF0D1510),
        surfaceVariant = Color(0xFFE3ECE6),
        onSurfaceVariant = Color(0xFF4B5A52),
        outline = Color(0xFF86958D),
        outlineVariant = Color(0xFFC7D1CB),
        scrim = Color(0xFFD8E0DB),
    ),
    dark = MeterColorTokens(
        primary = Color(0xFF00FF9C),
        onPrimary = Color(0xFF002817),
        primaryContainer = Color(0xFF00592F),
        onPrimaryContainer = Color(0xFFB9FFD9),
        secondary = Color(0xFFA4B0A9),
        secondaryContainer = Color(0xFF2A312D),
        onSecondaryContainer = Color(0xFFE4EBE6),
        tertiary = Color(0xFF58FFBF),
        background = Color(0xFF000000),
        onBackground = Color(0xFFF1F7F3),
        surface = Color(0xFF0C0F0D),
        onSurface = Color(0xFFF1F7F3),
        surfaceVariant = Color(0xFF151A18),
        onSurfaceVariant = Color(0xFFC7D0CB),
        outline = Color(0xFF76817A),
        outlineVariant = Color(0xFF313834),
        scrim = Color(0xFF000000),
    ),
)

val SoftVioletPalette = MeterThemePalette(
    light = MeterColorTokens(
        primary = Color(0xFF6C4BD1),
        onPrimary = Color(0xFFFAF7FF),
        primaryContainer = Color(0xFFE1D8FF),
        onPrimaryContainer = Color(0xFF24124B),
        secondary = Color(0xFF5E6473),
        secondaryContainer = Color(0xFFE0E4F0),
        onSecondaryContainer = Color(0xFF191F2A),
        tertiary = Color(0xFF7D5CE6),
        background = Color(0xFFF7F6FB),
        onBackground = Color(0xFF181A20),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF181A20),
        surfaceVariant = Color(0xFFEBE9F7),
        onSurfaceVariant = Color(0xFF565C6A),
        outline = Color(0xFF8F95A3),
        outlineVariant = Color(0xFFCDD1DD),
        scrim = Color(0xFFDADCEA),
    ),
    dark = MeterColorTokens(
        primary = Color(0xFFA78BFA),
        onPrimary = Color(0xFF1F133F),
        primaryContainer = Color(0xFF3D2D6B),
        onPrimaryContainer = Color(0xFFE9DEFF),
        secondary = Color(0xFFC3C9D8),
        secondaryContainer = Color(0xFF343949),
        onSecondaryContainer = Color(0xFFF0F3FB),
        tertiary = Color(0xFFC99BFF),
        background = Color(0xFF181A20),
        onBackground = Color(0xFFF3F4F8),
        surface = Color(0xFF20232B),
        onSurface = Color(0xFFF3F4F8),
        surfaceVariant = Color(0xFF2B303A),
        onSurfaceVariant = Color(0xFFD1D6E2),
        outline = Color(0xFF7E8493),
        outlineVariant = Color(0xFF424857),
        scrim = Color(0xFF0E1014),
    ),
)

val PaperLightPalette = MeterThemePalette(
    light = MeterColorTokens(
        primary = Color(0xFFD78833),
        onPrimary = Color(0xFF422200),
        primaryContainer = Color(0xFFFFE7CC),
        onPrimaryContainer = Color(0xFF4B2500),
        secondary = Color(0xFF75685B),
        secondaryContainer = Color(0xFFF1E6DA),
        onSecondaryContainer = Color(0xFF342B22),
        tertiary = Color(0xFFE5A54D),
        background = Color(0xFFFBF8F2),
        onBackground = Color(0xFF1C1813),
        surface = Color(0xFFFFFDFC),
        onSurface = Color(0xFF1C1813),
        surfaceVariant = Color(0xFFF7F2EA),
        onSurfaceVariant = Color(0xFF5F5549),
        outline = Color(0xFFC8B9A9),
        outlineVariant = Color(0xFFE4D9CC),
        scrim = Color(0xFFE7DED3),
    ),
    dark = MeterColorTokens(
        primary = Color(0xFFFFC57A),
        onPrimary = Color(0xFF4B2600),
        primaryContainer = Color(0xFF6A3A06),
        onPrimaryContainer = Color(0xFFFFE8CD),
        secondary = Color(0xFFD4C3B4),
        secondaryContainer = Color(0xFF41362D),
        onSecondaryContainer = Color(0xFFF5E8DB),
        tertiary = Color(0xFFFFD08D),
        background = Color(0xFF1C1916),
        onBackground = Color(0xFFF6F0E7),
        surface = Color(0xFF25211D),
        onSurface = Color(0xFFF6F0E7),
        surfaceVariant = Color(0xFF302A25),
        onSurfaceVariant = Color(0xFFD9CDBF),
        outline = Color(0xFF95887C),
        outlineVariant = Color(0xFF4A423A),
        scrim = Color(0xFF100E0C),
    ),
)
