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
import kotlin.math.roundToInt

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
}

data class MeterUiState(
    val exposureMode: ExposureMode = ExposureMode.APERTURE_PRIORITY,
    val meteringMode: MeteringMode = MeteringMode.SPOT,
    val meteringPoint: MeteringPoint = MeteringPoint.Center,
    val isAeLocked: Boolean = false,
    val selectedIso: Int = 100,
    val selectedAperture: Double = 2.8,
    val selectedShutterSeconds: Double = 1.0 / 125.0,
    val compensationEv: Double = 0.0,
    val calibrationOffsetEv: Double = 0.0,
    val liveReading: LuminanceReading? = null,
    val exposureResult: ExposureResult = ExposureResult.placeholder(),
    val meterStatus: MeterStatus = MeterStatus.WAITING,
    val cameraError: String? = null,
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
        val rawBaseEv100 = exposureCalculator.lumaToEv100(reading.meteredLuma)

        _uiState.update { current ->
            val nextState = current.copy(
                liveReading = reading,
                cameraError = null,
            )

            if (current.isAeLocked) {
                buildState(nextState, lockedBaseEv100 ?: smoothedBaseEv100)
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
        _uiState.update { current -> current.copy(meteringPoint = point) }
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

    private fun snapToStep(
        value: Double,
        step: Double,
        min: Double,
        max: Double,
    ): Double {
        val snapped = (value / step).roundToInt() * step
        return snapped.coerceIn(min, max)
    }
}
