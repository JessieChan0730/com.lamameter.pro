package com.yourbrand.lumameter.pro.viewmodel

import com.yourbrand.lumameter.pro.domain.exposure.ExposureMode
import com.yourbrand.lumameter.pro.domain.exposure.ExposureCalculator
import com.yourbrand.lumameter.pro.domain.exposure.LuminanceReading
import com.yourbrand.lumameter.pro.domain.exposure.MeteringMode
import com.yourbrand.lumameter.pro.domain.exposure.MeteringPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MeterViewModelTest {

    private val calculator = ExposureCalculator()

    @Test
    fun `first analyzed frame updates live meter state`() {
        val viewModel = MeterViewModel(exposureCalculator = calculator)
        val reading = sampleReading(meteredLuma = 100.0)

        viewModel.onFrameAnalyzed(reading)

        val state = viewModel.uiState.value
        assertEquals(reading, state.liveReading)
        assertEquals(MeterStatus.LIVE, state.meterStatus)
        assertEquals(calculator.lumaToEv100(100.0, null), state.exposureResult.sceneEv100, 0.0001)
        assertNull(state.cameraError)
    }

    @Test
    fun `live metering smooths consecutive readings`() {
        val viewModel = MeterViewModel(exposureCalculator = calculator)
        val firstLuma = 24.0
        val secondLuma = 220.0

        viewModel.onFrameAnalyzed(sampleReading(meteredLuma = firstLuma))
        viewModel.onFrameAnalyzed(sampleReading(meteredLuma = secondLuma))

        val state = viewModel.uiState.value
        val firstBaseEv100 = calculator.lumaToEv100(firstLuma, null)
        val secondBaseEv100 = calculator.lumaToEv100(secondLuma, null)
        val expectedEv100 = firstBaseEv100 * 0.82 + secondBaseEv100 * 0.18

        assertEquals(expectedEv100, state.exposureResult.sceneEv100, 0.0001)
        assertEquals(MeterStatus.LIVE, state.meterStatus)
    }

    @Test
    fun `adding custom aperture selects it and switches to aperture priority`() {
        val viewModel = MeterViewModel()

        viewModel.addApertureOption(1.8)

        val state = viewModel.uiState.value
        assertEquals(ExposureMode.APERTURE_PRIORITY, state.exposureMode)
        assertEquals(1.8, state.selectedAperture, 0.0001)
        assertTrue(state.apertureOptions.contains(1.8))
    }

    @Test
    fun `adding duplicate custom aperture does not create a second entry`() {
        val viewModel = MeterViewModel()

        viewModel.addApertureOption(1.8)
        viewModel.addApertureOption(1.8)

        val state = viewModel.uiState.value
        assertEquals(1, state.apertureOptions.count { kotlin.math.abs(it - 1.8) < 0.0001 })
        assertEquals(1.8, state.selectedAperture, 0.0001)
    }

    @Test
    fun `removing selected custom aperture falls back to nearest remaining value`() {
        val viewModel = MeterViewModel()

        viewModel.addApertureOption(1.8)
        viewModel.removeApertureOption(1.8)

        val state = viewModel.uiState.value
        assertFalse(state.apertureOptions.any { kotlin.math.abs(it - 1.8) < 0.0001 })
        assertEquals(2.0, state.selectedAperture, 0.0001)
    }

    @Test
    fun `removing selected custom shutter falls back to nearest remaining value`() {
        val viewModel = MeterViewModel()

        viewModel.addShutterOption(1.0 / 320.0)
        viewModel.removeShutterOption(1.0 / 320.0)

        val state = viewModel.uiState.value
        assertFalse(state.shutterOptions.any { kotlin.math.abs(it - (1.0 / 320.0)) < 0.0001 })
        assertEquals(1.0 / 250.0, state.selectedShutterSeconds, 0.0001)
    }

    @Test
    fun `default aperture values cannot be removed`() {
        val viewModel = MeterViewModel()

        viewModel.removeApertureOption(2.8)

        val state = viewModel.uiState.value
        assertTrue(state.apertureOptions.contains(2.8))
        assertEquals(2.8, state.selectedAperture, 0.0001)
    }

    @Test
    fun `adding custom shutter clamps to supported range and switches mode`() {
        val viewModel = MeterViewModel()

        viewModel.addShutterOption(40.0)

        val state = viewModel.uiState.value
        assertEquals(ExposureMode.SHUTTER_PRIORITY, state.exposureMode)
        assertEquals(30.0, state.selectedShutterSeconds, 0.0001)
        assertTrue(state.shutterOptions.contains(30.0))
    }

    @Test
    fun `default shutter values cannot be removed`() {
        val viewModel = MeterViewModel()

        viewModel.removeShutterOption(1.0 / 125.0)

        val state = viewModel.uiState.value
        assertTrue(state.shutterOptions.contains(1.0 / 125.0))
        assertEquals(1.0 / 125.0, state.selectedShutterSeconds, 0.0001)
    }

    @Test
    fun `single metering mode only captures after explicit request`() {
        val viewModel = MeterViewModel()
        val firstReading = sampleReading(meteredLuma = 60.0)
        val secondReading = sampleReading(meteredLuma = 180.0)

        viewModel.setLiveMeteringEnabled(false)
        viewModel.onFrameAnalyzed(firstReading)

        assertNull(viewModel.uiState.value.liveReading)

        viewModel.requestManualMetering()
        viewModel.onFrameAnalyzed(secondReading)

        val state = viewModel.uiState.value
        assertFalse(state.isLiveMeteringEnabled)
        assertFalse(state.isManualMeterPending)
        assertEquals(secondReading, state.liveReading)
    }

    @Test
    fun `manual metering request is ignored while live metering stays enabled`() {
        val viewModel = MeterViewModel()

        viewModel.requestManualMetering()

        assertFalse(viewModel.uiState.value.isManualMeterPending)
    }

    @Test
    fun `manual metering request clears camera error when live metering is disabled`() {
        val viewModel = MeterViewModel()

        viewModel.setLiveMeteringEnabled(false)
        viewModel.onCameraError("Permission denied")
        viewModel.requestManualMetering()

        val state = viewModel.uiState.value
        assertTrue(state.isManualMeterPending)
        assertNull(state.cameraError)
    }

    @Test
    fun `setting metering point marks spot reticle as user selected`() {
        val viewModel = MeterViewModel()
        val point = MeteringPoint.normalized(0.2f, 0.8f)

        viewModel.setMeteringPoint(point)

        val state = viewModel.uiState.value
        assertEquals(point, state.meteringPoint)
        assertTrue(state.hasCustomSpotMeteringPoint)
    }

    @Test
    fun `toggle ae lock without a reading leaves state unchanged`() {
        val viewModel = MeterViewModel()

        viewModel.toggleAeLock()

        val state = viewModel.uiState.value
        assertFalse(state.isAeLocked)
        assertEquals(MeterStatus.WAITING, state.meterStatus)
    }

    @Test
    fun `ae lock freezes exposure while newer frames still update preview reading`() {
        val viewModel = MeterViewModel(exposureCalculator = calculator)
        val firstReading = sampleReading(meteredLuma = 70.0)
        val secondReading = sampleReading(meteredLuma = 200.0)

        viewModel.onFrameAnalyzed(firstReading)
        val lockedSceneEv100 = viewModel.uiState.value.exposureResult.sceneEv100

        viewModel.toggleAeLock()
        viewModel.onFrameAnalyzed(secondReading)

        val state = viewModel.uiState.value
        assertTrue(state.isAeLocked)
        assertEquals(MeterStatus.LOCKED, state.meterStatus)
        assertEquals(secondReading, state.liveReading)
        assertEquals(lockedSceneEv100, state.exposureResult.sceneEv100, 0.0001)
    }

    @Test
    fun `camera error clears after the next successful frame analysis`() {
        val viewModel = MeterViewModel()

        viewModel.onCameraError("Camera unavailable")
        viewModel.onFrameAnalyzed(sampleReading(meteredLuma = 110.0))

        assertNull(viewModel.uiState.value.cameraError)
    }

    @Test
    fun `compensation and calibration snap to supported steps`() {
        val viewModel = MeterViewModel(exposureCalculator = calculator)

        viewModel.onFrameAnalyzed(sampleReading(meteredLuma = 100.0))
        viewModel.setCompensation(0.44f)
        viewModel.setCalibrationOffset(0.26f)

        val state = viewModel.uiState.value
        val baseEv100 = calculator.lumaToEv100(100.0, null)
        assertEquals(0.3, state.compensationEv, 0.0001)
        assertEquals(0.3, state.calibrationOffsetEv, 0.0001)
        assertEquals(baseEv100 + 0.3, state.exposureResult.sceneEv100, 0.0001)
        assertEquals(baseEv100, state.exposureResult.workingEv, 0.0001)
    }

    @Test
    fun `compensation and calibration clamp to configured bounds`() {
        val viewModel = MeterViewModel()

        viewModel.setCompensation(9f)
        viewModel.setCalibrationOffset(-9f)

        val state = viewModel.uiState.value
        assertEquals(3.0, state.compensationEv, 0.0001)
        assertEquals(-2.0, state.calibrationOffsetEv, 0.0001)
    }

    @Test
    fun `zoom presets reflect camera capability range`() {
        val viewModel = MeterViewModel()

        viewModel.updateZoomCapability(
            minZoomRatio = 1f,
            maxZoomRatio = 4f,
        )

        val state = viewModel.uiState.value
        assertTrue(state.isZoomSupported)
        assertEquals(1f, state.zoomRatio, 0.0001f)
        assertEquals(listOf(false, true, true, true, false), state.zoomPresets.map { it.enabled })
    }

    @Test
    fun `setting zoom ratio clamps to supported range`() {
        val viewModel = MeterViewModel()
        viewModel.updateZoomCapability(
            minZoomRatio = 1f,
            maxZoomRatio = 1.9f,
        )

        viewModel.setZoomRatio(4f)

        val state = viewModel.uiState.value
        assertEquals(1.9f, state.zoomRatio, 0.0001f)
        assertTrue(state.zoomPresets.none { it.selected })
    }

    @Test
    fun `fixed zoom devices hide zoom controls`() {
        val viewModel = MeterViewModel()

        viewModel.updateZoomCapability(
            minZoomRatio = 1f,
            maxZoomRatio = 1f,
        )

        val state = viewModel.uiState.value
        assertFalse(state.isZoomSupported)
        assertTrue(state.zoomPresets.isEmpty())
        assertEquals(1f, state.zoomRatio, 0.0001f)
    }

    @Test
    fun `minimum zoom ratio is clamped to supported floor`() {
        val viewModel = MeterViewModel()

        viewModel.updateZoomCapability(
            minZoomRatio = 0.01f,
            maxZoomRatio = 0.5f,
        )

        val state = viewModel.uiState.value
        assertEquals(0.1f, state.minZoomRatio, 0.0001f)
        assertEquals(0.5f, state.maxZoomRatio, 0.0001f)
        assertTrue(state.isZoomSupported)
    }

    @Test
    fun `wide zoom ranges expose all preset buttons`() {
        val viewModel = MeterViewModel()

        viewModel.updateZoomCapability(
            minZoomRatio = 0.5f,
            maxZoomRatio = 8f,
        )

        val state = viewModel.uiState.value
        assertTrue(state.isZoomSupported)
        assertEquals(listOf(true, true, true, true, true), state.zoomPresets.map { it.enabled })
    }

    @Test
    fun `narrow zoom ranges keep unsupported preset buttons disabled`() {
        val viewModel = MeterViewModel()

        viewModel.updateZoomCapability(
            minZoomRatio = 1f,
            maxZoomRatio = 2f,
        )

        val state = viewModel.uiState.value
        assertTrue(state.isZoomSupported)
        assertEquals(listOf(false, true, true, false, false), state.zoomPresets.map { it.enabled })
    }

    private fun sampleReading(meteredLuma: Double): LuminanceReading {
        return LuminanceReading(
            meteredLuma = meteredLuma,
            averageLuma = meteredLuma,
            frameWidth = 1920,
            frameHeight = 1080,
            rotationDegrees = 0,
            meteringMode = MeteringMode.SPOT,
            meteringPoint = MeteringPoint.Center,
        )
    }
}
