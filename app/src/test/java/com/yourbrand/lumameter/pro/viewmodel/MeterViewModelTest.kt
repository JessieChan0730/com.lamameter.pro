package com.yourbrand.lumameter.pro.viewmodel

import com.yourbrand.lumameter.pro.domain.exposure.AnalysisTool
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
        assertEquals(-5.0, state.calibrationOffsetEv, 0.0001)
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

    @Test
    fun `adding calibration preset saves name and current offset`() {
        val viewModel = MeterViewModel()

        viewModel.setCalibrationOffset(-1f)
        viewModel.addCalibrationPreset("Canon R10")

        val state = viewModel.uiState.value
        assertEquals(1, state.calibrationPresets.size)
        assertEquals("Canon R10", state.calibrationPresets[0].name)
        assertEquals(-1.0, state.calibrationPresets[0].offsetEv, 0.0001)
        assertEquals(state.calibrationPresets[0].id, state.activeCalibrationPresetId)
    }

    @Test
    fun `selecting preset applies its offset`() {
        val viewModel = MeterViewModel(exposureCalculator = calculator)

        viewModel.setCalibrationOffset(-2f)
        viewModel.addCalibrationPreset("Canon R10")
        viewModel.setCalibrationOffset(0f)

        val presetId = viewModel.uiState.value.calibrationPresets[0].id
        viewModel.selectCalibrationPreset(presetId)

        val state = viewModel.uiState.value
        assertEquals(-2.0, state.calibrationOffsetEv, 0.0001)
        assertEquals(presetId, state.activeCalibrationPresetId)
    }

    @Test
    fun `selecting active preset again clears selection and resets offset`() {
        val viewModel = MeterViewModel()

        viewModel.setCalibrationOffset(-2f)
        viewModel.addCalibrationPreset("Canon R10")

        val presetId = viewModel.uiState.value.calibrationPresets[0].id
        assertEquals(presetId, viewModel.uiState.value.activeCalibrationPresetId)

        viewModel.selectCalibrationPreset(presetId)

        val state = viewModel.uiState.value
        assertNull(state.activeCalibrationPresetId)
        assertEquals(0.0, state.calibrationOffsetEv, 0.0001)
    }

    @Test
    fun `manual slider change clears active preset`() {
        val viewModel = MeterViewModel()

        viewModel.setCalibrationOffset(-1f)
        viewModel.addCalibrationPreset("Canon R10")
        val presetId = viewModel.uiState.value.calibrationPresets[0].id
        assertEquals(presetId, viewModel.uiState.value.activeCalibrationPresetId)

        viewModel.setCalibrationOffset(0.5f)

        assertNull(viewModel.uiState.value.activeCalibrationPresetId)
    }

    @Test
    fun `deleting active preset clears selection`() {
        val viewModel = MeterViewModel()

        viewModel.setCalibrationOffset(-1f)
        viewModel.addCalibrationPreset("Canon R10")
        val presetId = viewModel.uiState.value.calibrationPresets[0].id

        viewModel.deleteCalibrationPreset(presetId)

        val state = viewModel.uiState.value
        assertTrue(state.calibrationPresets.isEmpty())
        assertNull(state.activeCalibrationPresetId)
        assertEquals(0.0, state.calibrationOffsetEv, 0.0001)
    }

    @Test
    fun `updating active preset rewrites offset`() {
        val viewModel = MeterViewModel()
        viewModel.setCalibrationOffset(-1f)
        viewModel.addCalibrationPreset("Canon R10", "daylight")
        val id = viewModel.uiState.value.calibrationPresets[0].id

        viewModel.updateCalibrationPreset(id, "Canon R10 II", "cloudy", 0.4)

        val state = viewModel.uiState.value
        assertEquals(1, state.calibrationPresets.size)
        assertEquals("Canon R10 II", state.calibrationPresets[0].name)
        assertEquals("cloudy", state.calibrationPresets[0].notes)
        assertEquals(0.4, state.calibrationPresets[0].offsetEv, 0.0001)
        assertEquals(0.4, state.calibrationOffsetEv, 0.0001)
    }

    @Test
    fun `updating non-active preset does not change live offset`() {
        val viewModel = MeterViewModel()
        viewModel.addCalibrationPreset("A")
        viewModel.addCalibrationPreset("B")
        val firstId = viewModel.uiState.value.calibrationPresets[0].id
        val activeId = viewModel.uiState.value.activeCalibrationPresetId
        assertTrue(activeId != firstId)

        viewModel.updateCalibrationPreset(firstId, "A+", "", 2.0)

        val state = viewModel.uiState.value
        assertEquals(2.0, state.calibrationPresets.first { it.id == firstId }.offsetEv, 0.0001)
        assertEquals(0.0, state.calibrationOffsetEv, 0.0001)
        assertEquals(activeId, state.activeCalibrationPresetId)
    }

    @Test
    fun `preview offset does not persist or clear active preset`() {
        val persisted = mutableListOf<PersistedMeterSettings>()
        val viewModel = MeterViewModel(onSettingsChanged = { persisted.add(it) })
        viewModel.setCalibrationOffset(0.5f)
        viewModel.addCalibrationPreset("Canon")
        val activeId = viewModel.uiState.value.activeCalibrationPresetId
        val snapshotsBefore = persisted.size

        viewModel.previewCalibrationOffset(-2.0f)

        val state = viewModel.uiState.value
        assertEquals(-2.0, state.calibrationOffsetEv, 0.0001)
        assertEquals(activeId, state.activeCalibrationPresetId)
        assertEquals(snapshotsBefore, persisted.size)
    }

    @Test
    fun `import replace swaps presets wholesale`() {
        val viewModel = MeterViewModel()
        viewModel.addCalibrationPreset("Old")

        val incoming = listOf(
            com.yourbrand.lumameter.pro.domain.exposure.CalibrationPreset(
                id = "x", name = "New", offsetEv = 0.7, notes = "",
            ),
        )
        viewModel.importCalibrationPresets(incoming, activeId = "x", strategy = CalibrationImportStrategy.REPLACE)

        val state = viewModel.uiState.value
        assertEquals(listOf("New"), state.calibrationPresets.map { it.name })
        assertEquals("x", state.activeCalibrationPresetId)
        assertEquals(0.7, state.calibrationOffsetEv, 0.0001)
    }

    @Test
    fun `import replace without active id clears selection and resets offset`() {
        val viewModel = MeterViewModel()
        viewModel.setCalibrationOffset(-1f)
        viewModel.addCalibrationPreset("Current")
        viewModel.setCalibrationOffset(1.5f)

        val incoming = listOf(
            com.yourbrand.lumameter.pro.domain.exposure.CalibrationPreset(
                id = "x", name = "Imported", offsetEv = 0.7, notes = "",
            ),
        )
        viewModel.importCalibrationPresets(incoming, activeId = null, strategy = CalibrationImportStrategy.REPLACE)

        val state = viewModel.uiState.value
        assertEquals(listOf("Imported"), state.calibrationPresets.map { it.name })
        assertNull(state.activeCalibrationPresetId)
        assertEquals(0.0, state.calibrationOffsetEv, 0.0001)
    }

    @Test
    fun `import merge skips duplicate names`() {
        val viewModel = MeterViewModel()
        viewModel.addCalibrationPreset("Canon R10")

        val incoming = listOf(
            com.yourbrand.lumameter.pro.domain.exposure.CalibrationPreset(
                id = "x", name = "canon r10", offsetEv = 0.3, notes = "",
            ),
            com.yourbrand.lumameter.pro.domain.exposure.CalibrationPreset(
                id = "y", name = "Sony A7", offsetEv = -0.1, notes = "",
            ),
        )
        viewModel.importCalibrationPresets(incoming, activeId = null, strategy = CalibrationImportStrategy.MERGE_KEEP_EXISTING)

        val names = viewModel.uiState.value.calibrationPresets.map { it.name }
        assertEquals(listOf("Canon R10", "Sony A7"), names)
    }

    @Test
    fun `import merge preserves current selection and offset`() {
        val viewModel = MeterViewModel()
        viewModel.setCalibrationOffset(-1f)
        viewModel.addCalibrationPreset("Current")
        val currentId = viewModel.uiState.value.activeCalibrationPresetId

        val incoming = listOf(
            com.yourbrand.lumameter.pro.domain.exposure.CalibrationPreset(
                id = "x", name = "Imported", offsetEv = 0.7, notes = "",
            ),
        )
        viewModel.importCalibrationPresets(incoming, activeId = "x", strategy = CalibrationImportStrategy.MERGE_KEEP_EXISTING)

        val state = viewModel.uiState.value
        assertEquals(listOf("Current", "Imported"), state.calibrationPresets.map { it.name })
        assertEquals(currentId, state.activeCalibrationPresetId)
        assertEquals(-1.0, state.calibrationOffsetEv, 0.0001)
    }

    @Test
    fun `calibration offset persists through initial settings`() {
        val viewModel = MeterViewModel(
            initialSettings = PersistedMeterSettings(
                calibrationOffsetEv = -3.5,
            ),
        )

        assertEquals(-3.5, viewModel.uiState.value.calibrationOffsetEv, 0.0001)
    }

    @Test
    fun `nd filter defaults to none`() {
        val viewModel = MeterViewModel()

        assertEquals(1, viewModel.uiState.value.selectedNdFilter)
    }

    @Test
    fun `nd filter ND8 reduces scene ev by 3 stops`() {
        val viewModel = MeterViewModel(exposureCalculator = calculator)

        viewModel.onFrameAnalyzed(sampleReading(meteredLuma = 100.0))
        val baseEv = viewModel.uiState.value.exposureResult.sceneEv100

        viewModel.setNdFilter(8)

        val ndEv = viewModel.uiState.value.exposureResult.sceneEv100
        assertEquals(baseEv - 3.0, ndEv, 0.0001)
    }

    @Test
    fun `nd filter persists through initial settings`() {
        val viewModel = MeterViewModel(
            initialSettings = PersistedMeterSettings(
                selectedNdFilter = 64,
            ),
        )

        assertEquals(64, viewModel.uiState.value.selectedNdFilter)
    }

    @Test
    fun `nd filter combined with calibration offset`() {
        val viewModel = MeterViewModel(exposureCalculator = calculator)

        viewModel.onFrameAnalyzed(sampleReading(meteredLuma = 100.0))
        val baseEv = viewModel.uiState.value.exposureResult.sceneEv100

        viewModel.setCalibrationOffset(1f)
        viewModel.setNdFilter(4)

        val state = viewModel.uiState.value
        // calibration adds +1.0, ND4 subtracts -2.0, net = -1.0
        assertEquals(baseEv - 1.0, state.exposureResult.sceneEv100, 0.0001)
    }

    @Test
    fun `nd filter notifies settings changed`() {
        var lastSettings: PersistedMeterSettings? = null
        val viewModel = MeterViewModel(
            onSettingsChanged = { lastSettings = it },
        )

        viewModel.setNdFilter(16)

        assertEquals(16, lastSettings?.selectedNdFilter)
    }

    @Test
    fun `analysis tool persists through initial settings and updates`() {
        var lastSettings: PersistedMeterSettings? = null
        val viewModel = MeterViewModel(
            initialSettings = PersistedMeterSettings(
                analysisTool = AnalysisTool.WHITE_BALANCE,
            ),
            onSettingsChanged = { lastSettings = it },
        )

        assertEquals(AnalysisTool.WHITE_BALANCE, viewModel.uiState.value.analysisTool)

        viewModel.setAnalysisTool(AnalysisTool.METER)

        assertEquals(AnalysisTool.METER, viewModel.uiState.value.analysisTool)
        assertEquals(AnalysisTool.METER, lastSettings?.analysisTool)
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
