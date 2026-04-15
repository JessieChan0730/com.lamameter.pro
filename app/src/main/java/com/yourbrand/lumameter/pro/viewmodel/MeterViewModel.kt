package com.yourbrand.lumameter.pro.viewmodel

import androidx.lifecycle.ViewModel
import com.yourbrand.lumameter.pro.domain.exposure.ExposureCalculator
import com.yourbrand.lumameter.pro.domain.exposure.ExposureMode
import com.yourbrand.lumameter.pro.domain.exposure.ExposureResult
import com.yourbrand.lumameter.pro.domain.exposure.LuminanceReading
import com.yourbrand.lumameter.pro.domain.exposure.MeteringMode
import com.yourbrand.lumameter.pro.domain.exposure.MeteringPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.abs
import kotlin.math.roundToInt

private const val DEFAULT_ZOOM_RATIO = 1f
private const val MIN_ZOOM_RATIO = 0.1f
private const val ZOOM_CAPABILITY_EPSILON = 0.01f
private const val ZOOM_PRESET_SELECTION_EPSILON = 0.05f
private val ZOOM_PRESET_RATIOS = listOf(0.5f, 1f, 2f, 4f, 8f)

object MeterDefaults {
    val isoValues = listOf(50, 100, 200, 400, 800, 1600, 3200, 6400)
    val apertureValues = listOf(1.4, 2.0, 2.8, 4.0, 5.6, 8.0, 11.0, 16.0)
    val shutterValues = listOf(
        1.0 / 4000.0,
        1.0 / 2000.0,
        1.0 / 1000.0,
        1.0 / 500.0,
        1.0 / 250.0,
        1.0 / 125.0,
        1.0 / 60.0,
        1.0 / 30.0,
        1.0 / 15.0,
        1.0 / 8.0,
        1.0 / 4.0,
        1.0 / 2.0,
        1.0,
        2.0,
        4.0,
        8.0,
    )
}

enum class MeterStatus {
    WAITING,
    LIVE,
    LOCKED,
    MANUAL,
}

data class ZoomPresetUiModel(
    val ratio: Float,
    val enabled: Boolean,
    val selected: Boolean,
)

data class MeterUiState(
    val exposureMode: ExposureMode = ExposureMode.APERTURE_PRIORITY,
    val meteringMode: MeteringMode = MeteringMode.SPOT,
    val meteringPoint: MeteringPoint = MeteringPoint.Center,
    val hasCustomSpotMeteringPoint: Boolean = false,
    val isAeLocked: Boolean = false,
    val isLiveMeteringEnabled: Boolean = true,
    val isManualMeterPending: Boolean = false,
    val isoOptions: List<Int> = MeterDefaults.isoValues,
    val apertureOptions: List<Double> = MeterDefaults.apertureValues,
    val shutterOptions: List<Double> = MeterDefaults.shutterValues,
    val selectedIso: Int = 100,
    val selectedAperture: Double = 2.8,
    val selectedShutterSeconds: Double = 1.0 / 125.0,
    val compensationEv: Double = 0.0,
    val calibrationOffsetEv: Double = 0.0,
    val liveReading: LuminanceReading? = null,
    val exposureResult: ExposureResult = ExposureResult.placeholder(),
    val meterStatus: MeterStatus = MeterStatus.WAITING,
    val cameraError: String? = null,
    val zoomRatio: Float = DEFAULT_ZOOM_RATIO,
    val minZoomRatio: Float = DEFAULT_ZOOM_RATIO,
    val maxZoomRatio: Float = DEFAULT_ZOOM_RATIO,
    val isZoomSupported: Boolean = false,
    val zoomPresets: List<ZoomPresetUiModel> = emptyList(),
)

class MeterViewModel(
    private val exposureCalculator: ExposureCalculator = ExposureCalculator(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        MeterUiState(
            selectedIso = MeterDefaults.isoValues[1],
            selectedAperture = MeterDefaults.apertureValues[2],
            selectedShutterSeconds = MeterDefaults.shutterValues[5],
        )
    )
    val uiState: StateFlow<MeterUiState> = _uiState.asStateFlow()

    private var smoothedBaseEv100: Double? = null
    private var lockedBaseEv100: Double? = null

    fun onFrameAnalyzed(reading: LuminanceReading) {
        val currentState = _uiState.value
        if (!currentState.isLiveMeteringEnabled && !currentState.isManualMeterPending) {
            return
        }

        val rawBaseEv100 = exposureCalculator.lumaToEv100(reading.meteredLuma, reading.metadata)

        _uiState.update { current ->
            val nextState = current.copy(
                liveReading = reading,
                cameraError = null,
                isManualMeterPending = false,
            )

            if (current.isAeLocked) {
                buildState(nextState, lockedBaseEv100 ?: smoothedBaseEv100)
            } else if (!current.isLiveMeteringEnabled) {
                smoothedBaseEv100 = rawBaseEv100
                buildState(nextState, rawBaseEv100)
            } else {
                val nextBaseEv100 = smoothedBaseEv100
                    ?.let { previous -> previous * 0.82 + rawBaseEv100 * 0.18 }
                    ?: rawBaseEv100
                smoothedBaseEv100 = nextBaseEv100
                buildState(nextState, nextBaseEv100)
            }
        }
    }

    fun setExposureMode(mode: ExposureMode) {
        _uiState.update { current ->
            buildState(current.copy(exposureMode = mode), activeBaseEv100())
        }
    }

    fun setMeteringMode(mode: MeteringMode) {
        _uiState.update { current ->
            buildState(current.copy(meteringMode = mode), activeBaseEv100())
        }
    }

    fun setMeteringPoint(point: MeteringPoint) {
        _uiState.update { current ->
            current.copy(
                meteringPoint = point,
                hasCustomSpotMeteringPoint = true,
            )
        }
    }

    fun updateZoomCapability(
        minZoomRatio: Float,
        maxZoomRatio: Float,
    ) {
        _uiState.update { current ->
            buildZoomState(
                baseState = current,
                minZoomRatio = minZoomRatio,
                maxZoomRatio = maxZoomRatio,
                zoomRatio = current.zoomRatio,
            )
        }
    }

    fun setZoomRatio(zoomRatio: Float) {
        _uiState.update { current ->
            buildZoomState(
                baseState = current,
                zoomRatio = zoomRatio,
            )
        }
    }

    fun setLiveMeteringEnabled(enabled: Boolean) {
        if (!enabled) {
            lockedBaseEv100 = null
        }
        _uiState.update { current ->
            buildState(
                current.copy(
                    isLiveMeteringEnabled = enabled,
                    isManualMeterPending = false,
                    isAeLocked = if (enabled) current.isAeLocked else false,
                ),
                activeBaseEv100(),
            )
        }
    }

    fun requestManualMetering() {
        _uiState.update { current ->
            if (current.isLiveMeteringEnabled) {
                return@update current
            }
            current.copy(
                isManualMeterPending = true,
                cameraError = null,
            )
        }
    }

    fun setIso(iso: Int) {
        _uiState.update { current ->
            buildState(current.copy(selectedIso = iso), activeBaseEv100())
        }
    }

    fun setAperture(aperture: Double) {
        _uiState.update { current ->
            buildState(current.copy(selectedAperture = aperture), activeBaseEv100())
        }
    }

    fun setShutterSeconds(shutterSeconds: Double) {
        _uiState.update { current ->
            buildState(current.copy(selectedShutterSeconds = shutterSeconds), activeBaseEv100())
        }
    }

    fun addApertureOption(aperture: Double) {
        val sanitizedAperture = aperture.coerceIn(1.0, 32.0)
        _uiState.update { current ->
            if (current.apertureOptions.any { valuesEqual(it, sanitizedAperture) }) {
                return@update current
            }

            buildState(
                current.copy(
                    exposureMode = ExposureMode.APERTURE_PRIORITY,
                    apertureOptions = (current.apertureOptions + sanitizedAperture).sorted(),
                    selectedAperture = sanitizedAperture,
                ),
                activeBaseEv100(),
            )
        }
    }

    fun removeApertureOption(aperture: Double) {
        _uiState.update { current ->
            if (MeterDefaults.apertureValues.any { valuesEqual(it, aperture) }) {
                return@update current
            }

            val nextOptions = current.apertureOptions.filterNot { valuesEqual(it, aperture) }
            if (nextOptions.isEmpty()) {
                return@update current
            }

            val nextSelection = if (valuesEqual(current.selectedAperture, aperture)) {
                findNearestValue(current.selectedAperture, nextOptions)
            } else {
                current.selectedAperture
            }

            buildState(
                current.copy(
                    apertureOptions = nextOptions,
                    selectedAperture = nextSelection,
                ),
                activeBaseEv100(),
            )
        }
    }

    fun addShutterOption(shutterSeconds: Double) {
        val sanitizedShutter = shutterSeconds.coerceIn(1.0 / 8000.0, 30.0)
        _uiState.update { current ->
            if (current.shutterOptions.any { valuesEqual(it, sanitizedShutter) }) {
                return@update current
            }

            buildState(
                current.copy(
                    exposureMode = ExposureMode.SHUTTER_PRIORITY,
                    shutterOptions = (current.shutterOptions + sanitizedShutter).sorted(),
                    selectedShutterSeconds = sanitizedShutter,
                ),
                activeBaseEv100(),
            )
        }
    }

    fun removeShutterOption(shutterSeconds: Double) {
        _uiState.update { current ->
            if (MeterDefaults.shutterValues.any { valuesEqual(it, shutterSeconds) }) {
                return@update current
            }

            val nextOptions = current.shutterOptions.filterNot { valuesEqual(it, shutterSeconds) }
            if (nextOptions.isEmpty()) {
                return@update current
            }

            val nextSelection = if (valuesEqual(current.selectedShutterSeconds, shutterSeconds)) {
                findNearestValue(current.selectedShutterSeconds, nextOptions)
            } else {
                current.selectedShutterSeconds
            }

            buildState(
                current.copy(
                    shutterOptions = nextOptions,
                    selectedShutterSeconds = nextSelection,
                ),
                activeBaseEv100(),
            )
        }
    }

    fun setCompensation(value: Float) {
        val snappedValue = snapToStep(value.toDouble(), step = 0.3, min = -3.0, max = 3.0)
        _uiState.update { current ->
            buildState(current.copy(compensationEv = snappedValue), activeBaseEv100())
        }
    }

    fun setCalibrationOffset(value: Float) {
        val snappedValue = snapToStep(value.toDouble(), step = 0.1, min = -2.0, max = 2.0)
        _uiState.update { current ->
            buildState(current.copy(calibrationOffsetEv = snappedValue), activeBaseEv100())
        }
    }

    fun toggleAeLock() {
        _uiState.update { current ->
            if (current.isAeLocked) {
                lockedBaseEv100 = null
                buildState(current.copy(isAeLocked = false), smoothedBaseEv100)
            } else {
                val currentBaseEv100 = smoothedBaseEv100 ?: return@update current
                lockedBaseEv100 = currentBaseEv100
                buildState(current.copy(isAeLocked = true), currentBaseEv100)
            }
        }
    }

    fun onCameraError(message: String) {
        _uiState.update { current ->
            current.copy(
                cameraError = message,
            )
        }
    }

    private fun buildState(
        baseState: MeterUiState,
        baseEv100: Double?,
    ): MeterUiState {
        val resolvedBaseEv100 = if (baseState.isAeLocked) {
            lockedBaseEv100 ?: baseEv100
        } else {
            baseEv100
        }

        val sceneEv100 = (resolvedBaseEv100 ?: 0.0) + baseState.calibrationOffsetEv
        val exposureResult = exposureCalculator.calculateFromEv100(
            sceneEv100 = sceneEv100,
            iso = baseState.selectedIso,
            mode = baseState.exposureMode,
            apertureValue = baseState.selectedAperture,
            shutterSeconds = baseState.selectedShutterSeconds,
            compensationEv = baseState.compensationEv,
        )

        val meterStatus = when {
            baseState.liveReading == null -> MeterStatus.WAITING
            baseState.isAeLocked -> MeterStatus.LOCKED
            !baseState.isLiveMeteringEnabled -> MeterStatus.MANUAL
            else -> MeterStatus.LIVE
        }

        return baseState.copy(
            exposureResult = exposureResult,
            meterStatus = meterStatus,
        )
    }

    private fun activeBaseEv100(): Double? {
        return lockedBaseEv100 ?: smoothedBaseEv100
    }

    private fun buildZoomState(
        baseState: MeterUiState,
        minZoomRatio: Float = baseState.minZoomRatio,
        maxZoomRatio: Float = baseState.maxZoomRatio,
        zoomRatio: Float = baseState.zoomRatio,
    ): MeterUiState {
        val normalizedMinZoom = minZoomRatio.coerceAtLeast(MIN_ZOOM_RATIO)
        val normalizedMaxZoom = maxZoomRatio.coerceAtLeast(normalizedMinZoom)
        val isZoomSupported = normalizedMaxZoom - normalizedMinZoom > ZOOM_CAPABILITY_EPSILON
        val resolvedZoomRatio = if (isZoomSupported) {
            zoomRatio.coerceIn(normalizedMinZoom, normalizedMaxZoom)
        } else {
            DEFAULT_ZOOM_RATIO
        }
        val zoomPresets = buildZoomPresets(
            currentZoomRatio = resolvedZoomRatio,
            minZoomRatio = normalizedMinZoom,
            maxZoomRatio = normalizedMaxZoom,
            isZoomSupported = isZoomSupported,
        )

        return baseState.copy(
            zoomRatio = resolvedZoomRatio,
            minZoomRatio = normalizedMinZoom,
            maxZoomRatio = normalizedMaxZoom,
            isZoomSupported = isZoomSupported,
            zoomPresets = zoomPresets,
        )
    }

    private fun buildZoomPresets(
        currentZoomRatio: Float,
        minZoomRatio: Float,
        maxZoomRatio: Float,
        isZoomSupported: Boolean,
    ): List<ZoomPresetUiModel> {
        if (!isZoomSupported) {
            return emptyList()
        }

        return ZOOM_PRESET_RATIOS.map { presetRatio ->
            val enabled = presetRatio in (minZoomRatio - ZOOM_CAPABILITY_EPSILON)..(maxZoomRatio + ZOOM_CAPABILITY_EPSILON)
            ZoomPresetUiModel(
                ratio = presetRatio,
                enabled = enabled,
                selected = enabled && abs(currentZoomRatio - presetRatio) < ZOOM_PRESET_SELECTION_EPSILON,
            )
        }
    }

    private fun snapToStep(
        value: Double,
        step: Double,
        min: Double,
        max: Double,
    ): Double {
        val snapped = (value / step).roundToInt() * step
        return snapped.coerceIn(min, max)
    }

    private fun findNearestValue(
        target: Double,
        options: List<Double>,
    ): Double {
        return options.minByOrNull { abs(it - target) } ?: target
    }

    private fun valuesEqual(
        first: Double,
        second: Double,
    ): Boolean {
        return abs(first - second) < 0.0001
    }

}
