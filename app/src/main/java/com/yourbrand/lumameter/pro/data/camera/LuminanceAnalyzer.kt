package com.yourbrand.lumameter.pro.data.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.yourbrand.lumameter.pro.domain.exposure.LuminanceReading
import com.yourbrand.lumameter.pro.domain.exposure.MeteringMode
import com.yourbrand.lumameter.pro.domain.exposure.MeteringPoint
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

class LuminanceAnalyzer(
    private val meteringModeProvider: () -> MeteringMode,
    private val meteringPointProvider: () -> MeteringPoint,
    private val onReadingAvailable: (LuminanceReading) -> Unit,
) : ImageAnalysis.Analyzer {

    override fun analyze(image: ImageProxy) {
        try {
            val yPlane = image.planes.firstOrNull() ?: return
            val buffer = yPlane.buffer
            val width = image.width
            val height = image.height
            val rowStride = yPlane.rowStride
            val pixelStride = yPlane.pixelStride

            val stepX = max(1, width / 48)
            val stepY = max(1, height / 48)

            val meteringMode = meteringModeProvider()
            val mappedPoint = mapPreviewPointToBuffer(
                previewPoint = meteringPointProvider(),
                rotationDegrees = image.imageInfo.rotationDegrees,
            )

            var sampleCount = 0
            var averageSum = 0.0
            var centerWeightedSum = 0.0
            var centerWeightTotal = 0.0
            var spotSum = 0.0
            var spotCount = 0

            val centerX = width / 2.0
            val centerY = height / 2.0
            val maxDistance = hypot(centerX, centerY)
            val spotCenterX = mappedPoint.x * width
            val spotCenterY = mappedPoint.y * height
            val spotRadius = min(width, height) * 0.12

            for (y in 0 until height step stepY) {
                for (x in 0 until width step stepX) {
                    val byteIndex = y * rowStride + x * pixelStride
                    val luma = buffer.get(byteIndex).toInt() and 0xFF
                    val lumaDouble = luma.toDouble()

                    averageSum += lumaDouble
                    sampleCount += 1

                    val distanceToCenter = hypot(x - centerX, y - centerY)
                    val centerWeight = 1.0 + (1.0 - (distanceToCenter / maxDistance).coerceIn(0.0, 1.0)) * 2.5
                    centerWeightedSum += lumaDouble * centerWeight
                    centerWeightTotal += centerWeight

                    val distanceToSpot = hypot(x - spotCenterX, y - spotCenterY)
                    if (distanceToSpot <= spotRadius) {
                        spotSum += lumaDouble
                        spotCount += 1
                    }
                }
            }

            val averageLuma = averageSum / sampleCount.coerceAtLeast(1)
            val centerWeightedLuma = centerWeightedSum / centerWeightTotal.coerceAtLeast(1.0)
            val spotLuma = if (spotCount > 0) spotSum / spotCount else averageLuma

            val meteredLuma = when (meteringMode) {
                MeteringMode.AVERAGE -> averageLuma
                MeteringMode.CENTER_WEIGHTED -> centerWeightedLuma
                MeteringMode.SPOT -> spotLuma
            }

            onReadingAvailable(
                LuminanceReading(
                    meteredLuma = meteredLuma,
                    averageLuma = averageLuma,
                    frameWidth = width,
                    frameHeight = height,
                    rotationDegrees = image.imageInfo.rotationDegrees,
                    meteringMode = meteringMode,
                    meteringPoint = mappedPoint,
                )
            )
        } finally {
            image.close()
        }
    }

    private fun mapPreviewPointToBuffer(
        previewPoint: MeteringPoint,
        rotationDegrees: Int,
    ): MeteringPoint {
        return when ((rotationDegrees % 360 + 360) % 360) {
            90 -> MeteringPoint.normalized(previewPoint.y, 1f - previewPoint.x)
            180 -> MeteringPoint.normalized(1f - previewPoint.x, 1f - previewPoint.y)
            270 -> MeteringPoint.normalized(1f - previewPoint.y, previewPoint.x)
            else -> previewPoint
        }
    }
}
