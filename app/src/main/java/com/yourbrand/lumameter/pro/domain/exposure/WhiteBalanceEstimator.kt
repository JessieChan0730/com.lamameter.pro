package com.yourbrand.lumameter.pro.domain.exposure

import kotlin.math.pow
import kotlin.math.roundToInt

class WhiteBalanceEstimator {

    fun estimate(
        metadata: FrameExposureMetadata?,
        red: Double?,
        green: Double?,
        blue: Double?,
    ): WhiteBalanceReading? {
        return metadata?.whiteBalanceGains?.let(::estimateFromGains)
            ?: estimateFromRgb(
                red = red,
                green = green,
                blue = blue,
            )
    }

    fun estimateFromGains(gains: WhiteBalanceGains): WhiteBalanceReading? {
        val redGain = gains.red.toDouble()
        val blueGain = gains.blue.toDouble()
        if (redGain <= 0.0 || blueGain <= 0.0) {
            return null
        }

        val kelvin = DAYLIGHT_REFERENCE_KELVIN * (redGain / blueGain).pow(GAIN_RATIO_EXPONENT)
        return buildReading(kelvin)
    }

    fun estimateFromRgb(
        red: Double?,
        green: Double?,
        blue: Double?,
    ): WhiteBalanceReading? {
        val safeRed = red?.coerceIn(0.0, 1.0) ?: return null
        val safeGreen = green?.coerceIn(0.0, 1.0) ?: return null
        val safeBlue = blue?.coerceIn(0.0, 1.0) ?: return null
        if (safeRed <= 0.0 && safeGreen <= 0.0 && safeBlue <= 0.0) {
            return null
        }

        val linearRed = srgbToLinear(safeRed)
        val linearGreen = srgbToLinear(safeGreen)
        val linearBlue = srgbToLinear(safeBlue)

        val x = linearRed * 0.4124 + linearGreen * 0.3576 + linearBlue * 0.1805
        val y = linearRed * 0.2126 + linearGreen * 0.7152 + linearBlue * 0.0722
        val z = linearRed * 0.0193 + linearGreen * 0.1192 + linearBlue * 0.9505
        val xyzSum = x + y + z
        if (xyzSum <= 0.0) {
            return null
        }

        val chromaticityX = x / xyzSum
        val chromaticityY = y / xyzSum
        val denominator = 0.1858 - chromaticityY
        if (denominator == 0.0) {
            return null
        }

        val n = (chromaticityX - 0.3320) / denominator
        val kelvin = 449.0 * n * n * n + 3525.0 * n * n + 6823.3 * n + 5520.33
        return buildReading(kelvin)
    }

    private fun buildReading(kelvin: Double): WhiteBalanceReading? {
        if (!kelvin.isFinite()) {
            return null
        }

        val clampedKelvin = kelvin
            .roundToInt()
            .coerceIn(MIN_SUPPORTED_KELVIN, MAX_SUPPORTED_KELVIN)
        return WhiteBalanceReading(
            kelvin = clampedKelvin,
            condition = WhiteBalanceCondition.fromKelvin(clampedKelvin),
        )
    }

    private fun srgbToLinear(component: Double): Double {
        return if (component <= 0.04045) {
            component / 12.92
        } else {
            ((component + 0.055) / 1.055).pow(2.4)
        }
    }

    private companion object {
        const val DAYLIGHT_REFERENCE_KELVIN = 5600.0
        const val GAIN_RATIO_EXPONENT = 1.18
        const val MIN_SUPPORTED_KELVIN = 1800
        const val MAX_SUPPORTED_KELVIN = 10000
    }
}
