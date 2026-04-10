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
    val histogram: IntArray = IntArray(HISTOGRAM_BIN_COUNT),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LuminanceReading) return false
        return meteredLuma == other.meteredLuma &&
            averageLuma == other.averageLuma &&
            frameWidth == other.frameWidth &&
            frameHeight == other.frameHeight &&
            rotationDegrees == other.rotationDegrees &&
            meteringMode == other.meteringMode &&
            meteringPoint == other.meteringPoint &&
            histogram.contentEquals(other.histogram)
    }

    override fun hashCode(): Int {
        var result = meteredLuma.hashCode()
        result = 31 * result + averageLuma.hashCode()
        result = 31 * result + frameWidth
        result = 31 * result + frameHeight
        result = 31 * result + rotationDegrees
        result = 31 * result + meteringMode.hashCode()
        result = 31 * result + meteringPoint.hashCode()
        result = 31 * result + histogram.contentHashCode()
        return result
    }

    companion object {
        const val HISTOGRAM_BIN_COUNT = 256
    }
}

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
