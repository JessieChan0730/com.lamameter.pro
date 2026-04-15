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
) {

    fun lumaToEv100(luma: Double, metadata: FrameExposureMetadata?): Double {
        if (metadata != null && metadata.exposureTimeNs > 0 && metadata.sensitivity > 0 && metadata.aperture > 0f) {
            val exposureTimeSec = metadata.exposureTimeNs / 1_000_000_000.0
            val cameraEv = log2(metadata.aperture.toDouble().pow(2.0) / exposureTimeSec)
            val isoCorrection = log2(metadata.sensitivity / 100.0)
            val normalizedLuma = (luma / 255.0).coerceIn(0.001, 1.0)
            val linearLuma = normalizedLuma.pow(2.2)
            val brightnessCorrection = log2(linearLuma / 0.18)
            return (cameraEv - isoCorrection + brightnessCorrection).coerceIn(minEv100, maxEv100)
        }
        val normalizedLuma = (luma / 255.0).coerceIn(0.0, 1.0)
        val perceptualBrightness = sqrt(normalizedLuma)
        return (minEv100 + (maxEv100 - minEv100) * perceptualBrightness)
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
}
