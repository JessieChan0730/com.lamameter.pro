package com.yourbrand.lumameter.pro

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
import com.yourbrand.lumameter.pro.domain.exposure.ViewfinderAspectRatio
import com.yourbrand.lumameter.pro.ui.meter.MeterRoute
import com.yourbrand.lumameter.pro.ui.theme.AppThemeMode
import com.yourbrand.lumameter.pro.ui.theme.LumaMeterTheme

class MainActivity : ComponentActivity() {
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
            var histogramEnabled by rememberSaveable {
                mutableStateOf(
                    preferences.getBoolean(KEY_HISTOGRAM_ENABLED, false),
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
            LaunchedEffect(histogramEnabled) {
                preferences.edit()
                    .putBoolean(KEY_HISTOGRAM_ENABLED, histogramEnabled)
                    .apply()
            }
            LaunchedEffect(viewfinderAspectRatio) {
                preferences.edit()
                    .putString(KEY_VIEWFINDER_ASPECT_RATIO, viewfinderAspectRatio.storageValue)
                    .apply()
            }

            LumaMeterTheme(themeMode = themeMode) {
                MeterRoute(
                    themeMode = themeMode,
                    onThemeModeChanged = { themeMode = it },
                    liveMeteringEnabled = liveMeteringEnabled,
                    onLiveMeteringEnabledChanged = { liveMeteringEnabled = it },
                    guideGridEnabled = guideGridEnabled,
                    onGuideGridEnabledChanged = { guideGridEnabled = it },
                    histogramEnabled = histogramEnabled,
                    onHistogramEnabledChanged = { histogramEnabled = it },
                    viewfinderAspectRatio = viewfinderAspectRatio,
                    onViewfinderAspectRatioChanged = { viewfinderAspectRatio = it },
                )
            }
        }
    }

    private companion object {
        const val THEME_PREFS_NAME = "luma_meter_prefs"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_LIVE_METERING_ENABLED = "live_metering_enabled"
        const val KEY_GUIDE_GRID_ENABLED = "guide_grid_enabled"
        const val KEY_HISTOGRAM_ENABLED = "histogram_enabled"
        const val KEY_VIEWFINDER_ASPECT_RATIO = "viewfinder_aspect_ratio"
    }
}
