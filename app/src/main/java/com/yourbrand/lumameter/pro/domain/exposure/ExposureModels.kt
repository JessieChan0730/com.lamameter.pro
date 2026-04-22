package com.yourbrand.lumameter.pro.domain.exposure

enum class ExposureMode {
    APERTURE_PRIORITY,
    SHUTTER_PRIORITY,
}

enum class AnalysisTool {
    METER,
    WHITE_BALANCE,
    FOCUS,
}

enum class MeteringMode {
    AVERAGE,
    CENTER_WEIGHTED,
    SPOT,
}

enum class ViewfinderAspectRatio(
    val storageValue: String,
    private val widthUnits: Int,
    private val heightUnits: Int,
) {
    SQUARE("1:1", 1, 1),
    FOUR_THREE("4:3", 4, 3),
    NINE_SIX("9:6", 9, 6),
    SIXTEEN_NINE("16:9", 16, 9),
    ;

    val ratio: Float
        get() = widthUnits.toFloat() / heightUnits.toFloat()

    companion object {
        val Default = FOUR_THREE

        fun fromStorageValue(value: String?): ViewfinderAspectRatio {
            return entries.firstOrNull { it.storageValue == value } ?: Default
        }
    }
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

data class ViewfinderRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    val width: Float
        get() = (right - left).coerceAtLeast(0f)

    val height: Float
        get() = (bottom - top).coerceAtLeast(0f)

    fun contains(point: MeteringPoint): Boolean {
        return point.x in left..right && point.y in top..bottom
    }

    fun toAbsolutePoint(localPoint: MeteringPoint): MeteringPoint {
        return MeteringPoint.normalized(
            x = left + width * localPoint.x,
            y = top + height * localPoint.y,
        )
    }

    fun toLocalPoint(absolutePoint: MeteringPoint): MeteringPoint {
        val safeWidth = width.coerceAtLeast(0.0001f)
        val safeHeight = height.coerceAtLeast(0.0001f)
        return MeteringPoint.normalized(
            x = (absolutePoint.x - left) / safeWidth,
            y = (absolutePoint.y - top) / safeHeight,
        )
    }

    companion object {
        val Full = ViewfinderRect(
            left = 0f,
            top = 0f,
            right = 1f,
            bottom = 1f,
        )

        fun centered(
            containerWidth: Float,
            containerHeight: Float,
            targetAspectRatio: Float,
        ): ViewfinderRect {
            val safeWidth = containerWidth.coerceAtLeast(1f)
            val safeHeight = containerHeight.coerceAtLeast(1f)
            val safeAspectRatio = targetAspectRatio.coerceAtLeast(0.01f)
            val containerAspectRatio = safeWidth / safeHeight

            return if (containerAspectRatio > safeAspectRatio) {
                val normalizedWidth = (safeAspectRatio / containerAspectRatio).coerceIn(0f, 1f)
                val horizontalInset = (1f - normalizedWidth) / 2f
                ViewfinderRect(
                    left = horizontalInset,
                    top = 0f,
                    right = 1f - horizontalInset,
                    bottom = 1f,
                )
            } else {
                val normalizedHeight = (containerAspectRatio / safeAspectRatio).coerceIn(0f, 1f)
                val verticalInset = (1f - normalizedHeight) / 2f
                ViewfinderRect(
                    left = 0f,
                    top = verticalInset,
                    right = 1f,
                    bottom = 1f - verticalInset,
                )
            }
        }
    }
}

data class FrameExposureMetadata(
    val exposureTimeNs: Long,
    val sensitivity: Int,
    val aperture: Float,
    val whiteBalanceGains: WhiteBalanceGains? = null,
)

data class WhiteBalanceGains(
    val red: Float,
    val greenEven: Float,
    val greenOdd: Float,
    val blue: Float,
)

enum class WhiteBalanceCondition(
    val referenceKelvin: Int,
) {
    CANDLE(referenceKelvin = 2200),
    TUNGSTEN(referenceKelvin = 3200),
    FLUORESCENT(referenceKelvin = 4200),
    SUNLIGHT(referenceKelvin = 5200),
    CLOUDY(referenceKelvin = 6000),
    SHADE(referenceKelvin = 7000),
    ;

    companion object {
        fun fromKelvin(kelvin: Int): WhiteBalanceCondition {
            return when {
                kelvin < 2600 -> CANDLE
                kelvin < 3400 -> TUNGSTEN
                kelvin < 4300 -> FLUORESCENT
                kelvin < 5600 -> SUNLIGHT
                kelvin < 6800 -> CLOUDY
                else -> SHADE
            }
        }
    }
}

data class WhiteBalanceReading(
    val kelvin: Int,
    val condition: WhiteBalanceCondition,
)

data class LuminanceReading(
    val meteredLuma: Double,
    val averageLuma: Double,
    val frameWidth: Int,
    val frameHeight: Int,
    val rotationDegrees: Int,
    val meteringMode: MeteringMode,
    val meteringPoint: MeteringPoint,
    val metadata: FrameExposureMetadata? = null,
    val whiteBalanceReading: WhiteBalanceReading? = null,
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
            metadata == other.metadata &&
            whiteBalanceReading == other.whiteBalanceReading &&
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
        result = 31 * result + (metadata?.hashCode() ?: 0)
        result = 31 * result + (whiteBalanceReading?.hashCode() ?: 0)
        result = 31 * result + histogram.contentHashCode()
        return result
    }

    companion object {
        const val HISTOGRAM_BIN_COUNT = 256
    }
}

const val PREVIEW_CONTAINER_ASPECT_RATIO = 4f / 3f

data class CalibrationPreset(
    val id: String,
    val name: String,
    val offsetEv: Double,
    val notes: String = "",
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
