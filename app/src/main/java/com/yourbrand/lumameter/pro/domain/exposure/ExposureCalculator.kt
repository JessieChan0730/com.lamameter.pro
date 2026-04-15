package com.yourbrand.lumameter.pro.domain.exposure

import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt

class ExposureCalculator(
    private val minEv100: Double = -6.0,
    private val maxEv100: Double = 16.0,
    private val minShutterSeconds: Double = 1.0 / 8000.0,
    private val maxShutterSeconds: Double = 30.0,
    private val minAperture: Double = 1.0,
    private val maxAperture: Double = 32.0,
    private val middleGrayLinearLuma: Double = 0.18,
    private val fallbackMiddleGrayEv100: Double = 8.0,
) {

    fun lumaToEv100(
        luma: Double,
        captureMetadata: CameraCaptureMetadata? = null,
    ): Double {
        estimateEv100FromCaptureMetadata(
            luma = luma,
            captureMetadata = captureMetadata,
        )?.let { return it }

        val normalizedLuma = (luma / 255.0).coerceIn(0.0, 1.0)
        if (normalizedLuma <= 0.0) {
            return minEv100
        }

        val relativeEvFromMiddleGray = lumaToRelativeEv(luma)
        return (fallbackMiddleGrayEv100 + relativeEvFromMiddleGray)
            .coerceIn(minEv100, maxEv100)
    }

    fun calculateFromEv100(
        sceneEv100: Double,
        iso: Int,
        mode: ExposureMode,
        apertureValue: Double,
        shutterSeconds: Double,
        compensationEv: Double,
    ): ExposureResult {
        val workingEv = sceneEv100 + log2(iso / 100.0) - compensationEv

        return when (mode) {
            ExposureMode.APERTURE_PRIORITY -> {
                val aperture = apertureValue.coerceIn(minAperture, maxAperture)
                val shutter = (aperture.pow(2.0) / 2.0.pow(workingEv))
                    .coerceIn(minShutterSeconds, maxShutterSeconds)
                ExposureResult(
                    sceneEv100 = sceneEv100,
                    workingEv = workingEv,
                    iso = iso,
                    aperture = aperture,
                    shutterSeconds = shutter,
                    exposureMode = mode,
                    compensationEv = compensationEv,
                )
            }

            ExposureMode.SHUTTER_PRIORITY -> {
                val shutter = shutterSeconds.coerceIn(minShutterSeconds, maxShutterSeconds)
                val aperture = sqrt(shutter * 2.0.pow(workingEv))
                    .coerceIn(minAperture, maxAperture)
                ExposureResult(
                    sceneEv100 = sceneEv100,
                    workingEv = workingEv,
                    iso = iso,
                    aperture = aperture,
                    shutterSeconds = shutter,
                    exposureMode = mode,
                    compensationEv = compensationEv,
                )
            }
        }
    }

    private fun log2(value: Double): Double {
        return ln(value) / ln(2.0)
    }

    private fun estimateEv100FromCaptureMetadata(
        luma: Double,
        captureMetadata: CameraCaptureMetadata?,
    ): Double? {
        val metadata = captureMetadata?.takeIf { it.isComplete } ?: return null
        val aperture = metadata.aperture ?: return null
        val exposureTimeNs = metadata.exposureTimeNs ?: return null
        val sensitivityIso = metadata.sensitivityIso ?: return null

        val exposureTimeSeconds = exposureTimeNs / 1_000_000_000.0
        if (exposureTimeSeconds <= 0.0) {
            return null
        }

        val frameEv100 = log2((aperture * aperture) / exposureTimeSeconds) - log2(sensitivityIso / 100.0)
        return (frameEv100 + lumaToRelativeEv(luma))
            .coerceIn(minEv100, maxEv100)
    }

    private fun lumaToRelativeEv(luma: Double): Double {
        val normalizedLuma = (luma / 255.0).coerceIn(0.0, 1.0)
        val linearLuma = srgbToLinear(normalizedLuma)
            .coerceAtLeast(Double.MIN_VALUE)
        return log2(linearLuma / middleGrayLinearLuma)
    }

    private fun srgbToLinear(value: Double): Double {
        return if (value <= 0.04045) {
            value / 12.92
        } else {
            ((value + 0.055) / 1.055).pow(2.4)
        }
    }
}
