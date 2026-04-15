package com.yourbrand.lumameter.pro.domain.exposure

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

class ExposureCalculatorTest {

    private val calculator = ExposureCalculator()

    @Test
    fun `luma below zero clamps to minimum ev without metadata`() {
        val result = calculator.lumaToEv100(-12.0, null)

        assertEquals(-6.0, result, 0.0001)
    }

    @Test
    fun `luma above sensor range clamps to maximum ev without metadata`() {
        val result = calculator.lumaToEv100(400.0, null)

        assertEquals(16.0, result, 0.0001)
    }

    @Test
    fun `luma maps to higher ev when scene gets brighter without metadata`() {
        val darkEv = calculator.lumaToEv100(24.0, null)
        val brightEv = calculator.lumaToEv100(220.0, null)

        assertTrue(brightEv > darkEv)
    }

    @Test
    fun `mid-gray scene at ev10 camera settings returns approximately ev10`() {
        // Camera auto-exposed to: f/2.8, 1/250s, ISO 100
        // Mid-gray (18%) scene should read luma ~118 after gamma encoding
        // 0.18^(1/2.2) ≈ 0.4562 → 0.4562*255 ≈ 116
        val metadata = FrameExposureMetadata(
            exposureTimeNs = 4_000_000L, // 1/250s
            sensitivity = 100,
            aperture = 2.8f,
        )
        // cameraEv = log2(2.8^2 / 0.004) = log2(1960) ≈ 10.94
        // isoCorrection = log2(100/100) = 0
        // linearLuma = (116/255)^2.2 ≈ 0.178
        // brightnessCorrection = log2(0.178/0.18) ≈ -0.016
        // sceneEv100 ≈ 10.94 - 0 + (-0.016) ≈ 10.92
        val result = calculator.lumaToEv100(116.0, metadata)

        assertEquals(10.9, result, 0.3)
    }

    @Test
    fun `bright scene with high luma shifts ev upward from camera baseline`() {
        val metadata = FrameExposureMetadata(
            exposureTimeNs = 4_000_000L, // 1/250s
            sensitivity = 100,
            aperture = 2.8f,
        )
        val midGrayEv = calculator.lumaToEv100(116.0, metadata)
        val brightEv = calculator.lumaToEv100(230.0, metadata)

        assertTrue(brightEv > midGrayEv)
    }

    @Test
    fun `dark scene with low luma shifts ev downward from camera baseline`() {
        val metadata = FrameExposureMetadata(
            exposureTimeNs = 4_000_000L, // 1/250s
            sensitivity = 100,
            aperture = 2.8f,
        )
        val midGrayEv = calculator.lumaToEv100(116.0, metadata)
        val darkEv = calculator.lumaToEv100(30.0, metadata)

        assertTrue(darkEv < midGrayEv)
    }

    @Test
    fun `higher iso reduces scene ev100`() {
        val metadataIso100 = FrameExposureMetadata(
            exposureTimeNs = 10_000_000L, // 1/100s
            sensitivity = 100,
            aperture = 4.0f,
        )
        val metadataIso400 = FrameExposureMetadata(
            exposureTimeNs = 10_000_000L,
            sensitivity = 400,
            aperture = 4.0f,
        )

        val ev100 = calculator.lumaToEv100(116.0, metadataIso100)
        val ev400 = calculator.lumaToEv100(116.0, metadataIso400)

        // ISO 400 means the camera needed less light, so scene EV should be lower
        assertEquals(ev100 - 2.0, ev400, 0.01)
    }

    @Test
    fun `metadata with zero exposure time falls back to legacy formula`() {
        val metadata = FrameExposureMetadata(
            exposureTimeNs = 0L,
            sensitivity = 100,
            aperture = 2.8f,
        )
        val withMeta = calculator.lumaToEv100(128.0, metadata)
        val withoutMeta = calculator.lumaToEv100(128.0, null)

        assertEquals(withoutMeta, withMeta, 0.0001)
    }

    @Test
    fun `aperture priority solves shutter from ev`() {
        val result = calculator.calculateFromEv100(
            sceneEv100 = 10.0,
            iso = 100,
            mode = ExposureMode.APERTURE_PRIORITY,
            apertureValue = 4.0,
            shutterSeconds = 1.0 / 125.0,
            compensationEv = 0.0,
        )

        assertEquals(4.0, result.aperture, 0.0001)
        assertEquals(1.0 / 64.0, result.shutterSeconds, 0.0001)
    }

    @Test
    fun `aperture priority includes iso and compensation in working ev`() {
        val result = calculator.calculateFromEv100(
            sceneEv100 = 10.0,
            iso = 400,
            mode = ExposureMode.APERTURE_PRIORITY,
            apertureValue = 2.0,
            shutterSeconds = 1.0 / 125.0,
            compensationEv = 0.6,
        )

        val expectedWorkingEv = 10.0 + 2.0 - 0.6
        val expectedShutter = 2.0.pow(2.0) / 2.0.pow(expectedWorkingEv)

        assertEquals(expectedWorkingEv, result.workingEv, 0.0001)
        assertEquals(expectedShutter, result.shutterSeconds, 0.0001)
    }

    @Test
    fun `aperture priority clamps aperture and shutter to supported range`() {
        val result = calculator.calculateFromEv100(
            sceneEv100 = -10.0,
            iso = 100,
            mode = ExposureMode.APERTURE_PRIORITY,
            apertureValue = 64.0,
            shutterSeconds = 1.0 / 125.0,
            compensationEv = 0.0,
        )

        assertEquals(32.0, result.aperture, 0.0001)
        assertEquals(30.0, result.shutterSeconds, 0.0001)
    }

    @Test
    fun `shutter priority solves aperture from ev`() {
        val result = calculator.calculateFromEv100(
            sceneEv100 = 9.0,
            iso = 100,
            mode = ExposureMode.SHUTTER_PRIORITY,
            apertureValue = 2.8,
            shutterSeconds = 1.0 / 125.0,
            compensationEv = 0.0,
        )

        assertEquals(2.02, result.aperture, 0.05)
        assertEquals(1.0 / 125.0, result.shutterSeconds, 0.0001)
    }

    @Test
    fun `shutter priority clamps shutter and aperture to supported range`() {
        val result = calculator.calculateFromEv100(
            sceneEv100 = -12.0,
            iso = 100,
            mode = ExposureMode.SHUTTER_PRIORITY,
            apertureValue = 2.8,
            shutterSeconds = 1.0 / 32000.0,
            compensationEv = 0.0,
        )

        assertEquals(1.0, result.aperture, 0.0001)
        assertEquals(1.0 / 8000.0, result.shutterSeconds, 0.0001)
    }
}
