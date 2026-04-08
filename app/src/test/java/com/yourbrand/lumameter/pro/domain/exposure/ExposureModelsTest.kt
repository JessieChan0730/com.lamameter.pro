package com.yourbrand.lumameter.pro.domain.exposure

import org.junit.Assert.assertEquals
import org.junit.Test

class ExposureModelsTest {

    @Test
    fun `center metering point uses normalized midpoint`() {
        assertEquals(MeteringPoint(0.5f, 0.5f), MeteringPoint.Center)
    }

    @Test
    fun `normalized metering point clamps coordinates into preview range`() {
        val point = MeteringPoint.normalized(x = -0.2f, y = 1.6f)

        assertEquals(0f, point.x, 0.0001f)
        assertEquals(1f, point.y, 0.0001f)
    }

    @Test
    fun `placeholder exposure result provides safe startup defaults`() {
        val result = ExposureResult.placeholder()

        assertEquals(0.0, result.sceneEv100, 0.0001)
        assertEquals(0.0, result.workingEv, 0.0001)
        assertEquals(100, result.iso)
        assertEquals(2.8, result.aperture, 0.0001)
        assertEquals(1.0 / 60.0, result.shutterSeconds, 0.0001)
        assertEquals(ExposureMode.APERTURE_PRIORITY, result.exposureMode)
        assertEquals(0.0, result.compensationEv, 0.0001)
    }
}
