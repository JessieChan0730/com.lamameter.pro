package com.yourbrand.lumameter.pro.ui.theme

import androidx.compose.material3.ColorScheme

enum class AppColorTheme(
    val storageValue: String,
    private val palette: MeterThemePalette,
) {
    CLASSIC_AMBER(
        storageValue = "classic_amber",
        palette = ClassicAmberPalette,
    ),
    CYAN_TECH(
        storageValue = "cyan_tech",
        palette = CyanTechPalette,
    ),
    NAVY_ORANGE(
        storageValue = "navy_orange",
        palette = NavyOrangePalette,
    ),
    INDUSTRIAL_GREEN(
        storageValue = "industrial_green",
        palette = IndustrialGreenPalette,
    ),
    SOFT_VIOLET(
        storageValue = "soft_violet",
        palette = SoftVioletPalette,
    ),
    PAPER_LIGHT(
        storageValue = "paper_light",
        palette = PaperLightPalette,
    ),
    ;

    fun colorScheme(darkTheme: Boolean): ColorScheme {
        return palette.colorScheme(darkTheme)
    }

    fun preview(darkTheme: Boolean): MeterPalettePreview {
        return palette.preview(darkTheme)
    }

    companion object {
        fun fromStorageValue(value: String?): AppColorTheme {
            return entries.firstOrNull { it.storageValue == value } ?: CLASSIC_AMBER
        }
    }
}
