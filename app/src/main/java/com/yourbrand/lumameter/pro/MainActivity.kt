package com.yourbrand.lumameter.pro

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.yourbrand.lumameter.pro.domain.exposure.AnalysisTool
import com.yourbrand.lumameter.pro.domain.exposure.CalibrationPreset
import com.yourbrand.lumameter.pro.domain.exposure.ExposureMode
import com.yourbrand.lumameter.pro.domain.exposure.MeteringMode
import com.yourbrand.lumameter.pro.domain.exposure.ReferenceGridType
import com.yourbrand.lumameter.pro.domain.exposure.ViewfinderAspectRatio
import com.yourbrand.lumameter.pro.ui.locale.AppLanguage
import com.yourbrand.lumameter.pro.ui.meter.MeterRoute
import com.yourbrand.lumameter.pro.ui.theme.AppColorTheme
import com.yourbrand.lumameter.pro.ui.theme.AppThemeMode
import com.yourbrand.lumameter.pro.ui.theme.LumaMeterTheme
import com.yourbrand.lumameter.pro.viewmodel.MeterDefaults
import com.yourbrand.lumameter.pro.viewmodel.PersistedMeterSettings
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val preferences = newBase.getSharedPreferences(THEME_PREFS_NAME, MODE_PRIVATE)
        val language = AppLanguage.fromStorageValue(
            preferences.getString(KEY_LANGUAGE, AppLanguage.SYSTEM.storageValue),
        )
        super.attachBaseContext(newBase.localized(language))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val preferences = remember {
                getSharedPreferences(THEME_PREFS_NAME, MODE_PRIVATE)
            }
            var themeMode by rememberSaveable {
                mutableStateOf(
                    AppThemeMode.fromStorageValue(
                        preferences.getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.storageValue),
                    )
                )
            }
            var colorTheme by rememberSaveable {
                mutableStateOf(
                    AppColorTheme.fromStorageValue(
                        preferences.getString(
                            KEY_COLOR_THEME,
                            AppColorTheme.CLASSIC_AMBER.storageValue,
                        ),
                    )
                )
            }
            var language by rememberSaveable {
                mutableStateOf(
                    AppLanguage.fromStorageValue(
                        preferences.getString(KEY_LANGUAGE, AppLanguage.SYSTEM.storageValue),
                    )
                )
            }
            var liveMeteringEnabled by rememberSaveable {
                mutableStateOf(
                    preferences.getBoolean(KEY_LIVE_METERING_ENABLED, true),
                )
            }
            var guideGridEnabled by rememberSaveable {
                mutableStateOf(
                    preferences.getBoolean(KEY_GUIDE_GRID_ENABLED, false),
                )
            }
            var referenceGridType by rememberSaveable {
                mutableStateOf(
                    ReferenceGridType.fromStorageValue(
                        preferences.getString(
                            KEY_REFERENCE_GRID_TYPE,
                            ReferenceGridType.Default.storageValue,
                        ),
                    )
                )
            }
            var histogramEnabled by rememberSaveable {
                mutableStateOf(
                    preferences.getBoolean(KEY_HISTOGRAM_ENABLED, false),
                )
            }
            var levelIndicatorEnabled by rememberSaveable {
                mutableStateOf(
                    preferences.getBoolean(KEY_LEVEL_INDICATOR_ENABLED, false),
                )
            }
            var viewfinderAspectRatio by rememberSaveable {
                mutableStateOf(
                    ViewfinderAspectRatio.fromStorageValue(
                        preferences.getString(
                            KEY_VIEWFINDER_ASPECT_RATIO,
                            ViewfinderAspectRatio.Default.storageValue,
                        ),
                    )
                )
            }

            LaunchedEffect(themeMode) {
                preferences.edit()
                    .putString(KEY_THEME_MODE, themeMode.storageValue)
                    .apply()
            }
            LaunchedEffect(colorTheme) {
                preferences.edit()
                    .putString(KEY_COLOR_THEME, colorTheme.storageValue)
                    .apply()
            }
            LaunchedEffect(liveMeteringEnabled) {
                preferences.edit()
                    .putBoolean(KEY_LIVE_METERING_ENABLED, liveMeteringEnabled)
                    .apply()
            }
            LaunchedEffect(guideGridEnabled) {
                preferences.edit()
                    .putBoolean(KEY_GUIDE_GRID_ENABLED, guideGridEnabled)
                    .apply()
            }
            LaunchedEffect(referenceGridType) {
                preferences.edit()
                    .putString(KEY_REFERENCE_GRID_TYPE, referenceGridType.storageValue)
                    .apply()
            }
            LaunchedEffect(histogramEnabled) {
                preferences.edit()
                    .putBoolean(KEY_HISTOGRAM_ENABLED, histogramEnabled)
                    .apply()
            }
            LaunchedEffect(levelIndicatorEnabled) {
                preferences.edit()
                    .putBoolean(KEY_LEVEL_INDICATOR_ENABLED, levelIndicatorEnabled)
                    .apply()
            }
            LaunchedEffect(viewfinderAspectRatio) {
                preferences.edit()
                    .putString(KEY_VIEWFINDER_ASPECT_RATIO, viewfinderAspectRatio.storageValue)
                    .apply()
            }

            val initialSettings = remember {
                PersistedMeterSettings(
                    analysisTool = AnalysisTool.entries.firstOrNull {
                        it.name == preferences.getString(KEY_ANALYSIS_TOOL, null)
                    } ?: PersistedMeterSettings().analysisTool,
                    meteringMode = MeteringMode.entries.firstOrNull {
                        it.name == preferences.getString(KEY_METERING_MODE, null)
                    } ?: PersistedMeterSettings().meteringMode,
                    selectedIso = preferences.getInt(
                        KEY_SELECTED_ISO,
                        MeterDefaults.isoValues[1],
                    ),
                    exposureMode = ExposureMode.entries.firstOrNull {
                        it.name == preferences.getString(KEY_EXPOSURE_MODE, null)
                    } ?: PersistedMeterSettings().exposureMode,
                    selectedAperture = preferences.getFloat(
                        KEY_SELECTED_APERTURE,
                        MeterDefaults.apertureValues[2].toFloat(),
                    ).toDouble(),
                    selectedShutterSeconds = preferences.getFloat(
                        KEY_SELECTED_SHUTTER,
                        MeterDefaults.shutterValues[5].toFloat(),
                    ).toDouble(),
                    compensationEv = preferences.getFloat(
                        KEY_COMPENSATION_EV,
                        0f,
                    ).toDouble(),
                    isAeLocked = preferences.getBoolean(KEY_AE_LOCKED, false),
                    customApertures = parseDoubleList(
                        preferences.getString(KEY_CUSTOM_APERTURES, null),
                    ),
                    customShutters = parseDoubleList(
                        preferences.getString(KEY_CUSTOM_SHUTTERS, null),
                    ),
                    calibrationOffsetEv = preferences.getFloat(
                        KEY_CALIBRATION_OFFSET,
                        0f,
                    ).toDouble(),
                    calibrationPresets = parseCalibrationPresets(
                        preferences.getString(KEY_CALIBRATION_PRESETS, null),
                    ),
                    activeCalibrationPresetId = preferences.getString(
                        KEY_ACTIVE_CALIBRATION_PRESET,
                        null,
                    ),
                    selectedNdFilter = preferences.getInt(
                        KEY_SELECTED_ND_FILTER,
                        1,
                    ),
                )
            }

            LumaMeterTheme(
                themeMode = themeMode,
                colorTheme = colorTheme,
            ) {
                MeterRoute(
                    themeMode = themeMode,
                    onThemeModeChanged = { themeMode = it },
                    colorTheme = colorTheme,
                    onColorThemeChanged = { colorTheme = it },
                    language = language,
                    onLanguageChanged = { selectedLanguage ->
                        if (selectedLanguage != language) {
                            language = selectedLanguage
                            preferences.edit()
                                .putString(KEY_LANGUAGE, selectedLanguage.storageValue)
                                .commit()
                            if (!isFinishing && !isDestroyed) {
                                recreate()
                            }
                        }
                    },
                    liveMeteringEnabled = liveMeteringEnabled,
                    onLiveMeteringEnabledChanged = { liveMeteringEnabled = it },
                    guideGridEnabled = guideGridEnabled,
                    onGuideGridEnabledChanged = { guideGridEnabled = it },
                    referenceGridType = referenceGridType,
                    onReferenceGridTypeChanged = { referenceGridType = it },
                    histogramEnabled = histogramEnabled,
                    onHistogramEnabledChanged = { histogramEnabled = it },
                    levelIndicatorEnabled = levelIndicatorEnabled,
                    onLevelIndicatorEnabledChanged = { levelIndicatorEnabled = it },
                    viewfinderAspectRatio = viewfinderAspectRatio,
                    onViewfinderAspectRatioChanged = { viewfinderAspectRatio = it },
                    initialSettings = initialSettings,
                    onSettingsChanged = { settings ->
                        preferences.edit()
                            .putString(KEY_ANALYSIS_TOOL, settings.analysisTool.name)
                            .putString(KEY_METERING_MODE, settings.meteringMode.name)
                            .putInt(KEY_SELECTED_ISO, settings.selectedIso)
                            .putString(KEY_EXPOSURE_MODE, settings.exposureMode.name)
                            .putFloat(KEY_SELECTED_APERTURE, settings.selectedAperture.toFloat())
                            .putFloat(KEY_SELECTED_SHUTTER, settings.selectedShutterSeconds.toFloat())
                            .putFloat(KEY_COMPENSATION_EV, settings.compensationEv.toFloat())
                            .putBoolean(KEY_AE_LOCKED, settings.isAeLocked)
                            .putString(KEY_CUSTOM_APERTURES, serializeDoubleList(settings.customApertures))
                            .putString(KEY_CUSTOM_SHUTTERS, serializeDoubleList(settings.customShutters))
                            .putFloat(KEY_CALIBRATION_OFFSET, settings.calibrationOffsetEv.toFloat())
                            .putString(KEY_CALIBRATION_PRESETS, serializeCalibrationPresets(settings.calibrationPresets))
                            .putString(KEY_ACTIVE_CALIBRATION_PRESET, settings.activeCalibrationPresetId)
                            .putInt(KEY_SELECTED_ND_FILTER, settings.selectedNdFilter)
                            .apply()
                    },
                )
            }
        }
    }

    private companion object {
        const val THEME_PREFS_NAME = "luma_meter_prefs"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_COLOR_THEME = "color_theme"
        const val KEY_LANGUAGE = "language"
        const val KEY_LIVE_METERING_ENABLED = "live_metering_enabled"
        const val KEY_GUIDE_GRID_ENABLED = "guide_grid_enabled"
        const val KEY_REFERENCE_GRID_TYPE = "reference_grid_type"
        const val KEY_HISTOGRAM_ENABLED = "histogram_enabled"
        const val KEY_LEVEL_INDICATOR_ENABLED = "level_indicator_enabled"
        const val KEY_VIEWFINDER_ASPECT_RATIO = "viewfinder_aspect_ratio"
        const val KEY_ANALYSIS_TOOL = "analysis_tool"
        const val KEY_CUSTOM_APERTURES = "custom_apertures"
        const val KEY_CUSTOM_SHUTTERS = "custom_shutters"
        const val KEY_METERING_MODE = "metering_mode"
        const val KEY_SELECTED_ISO = "selected_iso"
        const val KEY_EXPOSURE_MODE = "exposure_mode"
        const val KEY_SELECTED_APERTURE = "selected_aperture"
        const val KEY_SELECTED_SHUTTER = "selected_shutter"
        const val KEY_COMPENSATION_EV = "compensation_ev"
        const val KEY_AE_LOCKED = "ae_locked"
        const val KEY_CALIBRATION_OFFSET = "calibration_offset"
        const val KEY_CALIBRATION_PRESETS = "calibration_presets"
        const val KEY_ACTIVE_CALIBRATION_PRESET = "active_calibration_preset"
        const val KEY_SELECTED_ND_FILTER = "selected_nd_filter"

        fun parseDoubleList(raw: String?): List<Double> {
            if (raw.isNullOrBlank()) return emptyList()
            return raw.split(",").mapNotNull { it.trim().toDoubleOrNull() }
        }

        fun serializeDoubleList(values: List<Double>): String {
            return values.joinToString(",")
        }

        private const val PRESET_RECORD_SEPARATOR = ";;"
        private const val PRESET_FIELD_SEPARATOR = "|"

        fun parseCalibrationPresets(raw: String?): List<CalibrationPreset> {
            if (raw.isNullOrBlank()) return emptyList()
            return raw.split(PRESET_RECORD_SEPARATOR).mapNotNull { record ->
                val parts = record.split(PRESET_FIELD_SEPARATOR, limit = 4)
                if (parts.size >= 3) {
                    val offset = parts[2].toDoubleOrNull() ?: return@mapNotNull null
                    CalibrationPreset(
                        id = parts[0],
                        name = parts[1],
                        offsetEv = offset,
                        notes = parts.getOrElse(3) { "" },
                    )
                } else {
                    null
                }
            }
        }

        fun serializeCalibrationPresets(presets: List<CalibrationPreset>): String {
            return presets.joinToString(PRESET_RECORD_SEPARATOR) { preset ->
                "${preset.id}${PRESET_FIELD_SEPARATOR}${preset.name}${PRESET_FIELD_SEPARATOR}${preset.offsetEv}${PRESET_FIELD_SEPARATOR}${preset.notes}"
            }
        }
    }
}

private fun Context.localized(language: AppLanguage): Context {
    val locale = language.locale ?: return this
    Locale.setDefault(locale)
    val configuration = Configuration(resources.configuration)
    configuration.setLocale(locale)
    return createConfigurationContext(configuration)
}
