package com.yourbrand.lumameter.pro.domain.exposure

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WhiteBalanceEstimatorTest {

    private val estimator = WhiteBalanceEstimator()

    @Test
    fun `awb gains near daylight map to sunlight`() {
        val reading = estimator.estimateFromGains(
            WhiteBalanceGains(
                red = 2.0f,
                greenEven = 1.0f,
                greenOdd = 1.0f,
                blue = 2.15f,
            )
        )

        assertNotNull(reading)
        assertEquals(WhiteBalanceCondition.SUNLIGHT, reading?.condition)
        assertTrue((reading?.kelvin ?: 0) in 5000..5400)
    }

    @Test
    fun `warm awb gains map to tungsten`() {
        val reading = estimator.estimateFromGains(
            WhiteBalanceGains(
                red = 2.0f,
                greenEven = 1.0f,
                greenOdd = 1.0f,
                blue = 3.4f,
            )
        )

        assertNotNull(reading)
        assertEquals(WhiteBalanceCondition.TUNGSTEN, reading?.condition)
        assertTrue((reading?.kelvin ?: 0) in 2800..3200)
    }

    @Test
    fun `cool awb gains map to shade`() {
        val reading = estimator.estimateFromGains(
            WhiteBalanceGains(
                red = 3.1f,
                greenEven = 1.0f,
                greenOdd = 1.0f,
                blue = 2.0f,
            )
        )

        assertNotNull(reading)
        assertEquals(WhiteBalanceCondition.SHADE, reading?.condition)
        assertTrue((reading?.kelvin ?: 0) >= 9000)
    }

    @Test
    fun `kelvin classification keeps direct sun inside sunlight range`() {
        assertEquals(WhiteBalanceCondition.SUNLIGHT, WhiteBalanceCondition.fromKelvin(5200))
        assertEquals(WhiteBalanceCondition.CLOUDY, WhiteBalanceCondition.fromKelvin(6200))
        assertEquals(WhiteBalanceCondition.CANDLE, WhiteBalanceCondition.fromKelvin(2200))
    }

    @Test
    fun `white balance conditions expose stable reference kelvin values`() {
        assertEquals(2200, WhiteBalanceCondition.CANDLE.referenceKelvin)
        assertEquals(3200, WhiteBalanceCondition.TUNGSTEN.referenceKelvin)
        assertEquals(4200, WhiteBalanceCondition.FLUORESCENT.referenceKelvin)
        assertEquals(5200, WhiteBalanceCondition.SUNLIGHT.referenceKelvin)
        assertEquals(6000, WhiteBalanceCondition.CLOUDY.referenceKelvin)
        assertEquals(7000, WhiteBalanceCondition.SHADE.referenceKelvin)
    }
}
