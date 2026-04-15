package com.yourbrand.lumameter.pro.domain.exposure

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

class ExposureCalculatorTest {

    private val calculator = ExposureCalculator()
    private val middleGrayLuma = 118.0

    @Test
    fun `luma below zero clamps to minimum ev`() {
        val result = calculator.lumaToEv100(-12.0)

        assertEquals(-6.0, result, 0.0001)
    }

    @Test
    fun `luma above sensor range clamps to sensor white point`() {
        val result = calculator.lumaToEv100(400.0)

        assertEquals(calculator.lumaToEv100(255.0), result, 0.0001)
    }

    @Test
    fun `luma maps to higher ev when scene gets brighter`() {
        val darkEv = calculator.lumaToEv100(24.0)
        val brightEv = calculator.lumaToEv100(220.0)

        assertTrue(brightEv > darkEv)
    }

    @Test
    fun `capture metadata drives ev estimate around camera exposure`() {
        val result = calculator.lumaToEv100(
            luma = middleGrayLuma,
            captureMetadata = CameraCaptureMetadata(
                aperture = 1.8,
                exposureTimeNs = 12_500_000L,
                sensitivityIso = 100,
            ),
        )

        assertEquals(8.0, result, 0.2)
    }

    @Test
    fun `brighter than middle gray region increases ev when capture metadata is available`() {
        val darkerRegionEv = calculator.lumaToEv100(
            luma = 80.0,
            captureMetadata = CameraCaptureMetadata(
                aperture = 1.8,
                exposureTimeNs = 12_500_000L,
                sensitivityIso = 100,
            ),
        )
        val brighterRegionEv = calculator.lumaToEv100(
            luma = 180.0,
            captureMetadata = CameraCaptureMetadata(
                aperture = 1.8,
                exposureTimeNs = 12_500_000L,
                sensitivityIso = 100,
            ),
        )

        assertTrue(brighterRegionEv > darkerRegionEv)
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
