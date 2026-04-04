package com.yourbrand.lumameter.pro.domain.exposure

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExposureCalculatorTest {

    private val calculator = ExposureCalculator()

    @Test
    fun `luma maps to higher ev when scene gets brighter`() {
        val darkEv = calculator.lumaToEv100(24.0)
        val brightEv = calculator.lumaToEv100(220.0)

        assertTrue(brightEv > darkEv)
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
}
