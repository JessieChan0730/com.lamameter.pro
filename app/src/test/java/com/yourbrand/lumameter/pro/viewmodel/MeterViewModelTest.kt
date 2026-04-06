package com.yourbrand.lumameter.pro.viewmodel

import com.yourbrand.lumameter.pro.domain.exposure.ExposureMode
import com.yourbrand.lumameter.pro.domain.exposure.LuminanceReading
import com.yourbrand.lumameter.pro.domain.exposure.MeteringMode
import com.yourbrand.lumameter.pro.domain.exposure.MeteringPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MeterViewModelTest {

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
