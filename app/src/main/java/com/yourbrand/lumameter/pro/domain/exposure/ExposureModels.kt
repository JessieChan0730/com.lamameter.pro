package com.yourbrand.lumameter.pro.domain.exposure

enum class ExposureMode {
    APERTURE_PRIORITY,
    SHUTTER_PRIORITY,
}

enum class MeteringMode {
    AVERAGE,
    CENTER_WEIGHTED,
    SPOT,
}

data class MeteringPoint(
    val x: Float = 0.5f,
    val y: Float = 0.5f,
) {
    companion object {
        val Center = MeteringPoint()

        fun normalized(x: Float, y: Float): MeteringPoint {
            return MeteringPoint(
                x = x.coerceIn(0f, 1f),
                y = y.coerceIn(0f, 1f),
            )
        }
    }
}

data class LuminanceReading(
    val meteredLuma: Double,
    val averageLuma: Double,
    val frameWidth: Int,
    val frameHeight: Int,
    val rotationDegrees: Int,
    val meteringMode: MeteringMode,
    val meteringPoint: MeteringPoint,
)

data class ExposureResult(
    val sceneEv100: Double,
    val workingEv: Double,
    val iso: Int,
    val aperture: Double,
    val shutterSeconds: Double,
    val exposureMode: ExposureMode,
    val compensationEv: Double,
) {
    companion object {
        fun placeholder(): ExposureResult {
            return ExposureResult(
                sceneEv100 = 0.0,
                workingEv = 0.0,
                iso = 100,
                aperture = 2.8,
                shutterSeconds = 1.0 / 60.0,
                exposureMode = ExposureMode.APERTURE_PRIORITY,
                compensationEv = 0.0,
            )
        }
    }
}
