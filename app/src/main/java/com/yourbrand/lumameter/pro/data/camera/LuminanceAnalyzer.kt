package com.yourbrand.lumameter.pro.data.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.yourbrand.lumameter.pro.domain.exposure.LuminanceReading
import com.yourbrand.lumameter.pro.domain.exposure.MeteringMode
import com.yourbrand.lumameter.pro.domain.exposure.MeteringPoint
import com.yourbrand.lumameter.pro.domain.exposure.PREVIEW_CONTAINER_ASPECT_RATIO
import com.yourbrand.lumameter.pro.domain.exposure.ViewfinderAspectRatio
import com.yourbrand.lumameter.pro.domain.exposure.ViewfinderRect
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

class LuminanceAnalyzer(
    private val meteringModeProvider: () -> MeteringMode,
    private val meteringPointProvider: () -> MeteringPoint,
    private val viewfinderAspectRatioProvider: () -> ViewfinderAspectRatio,
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

            val meteringMode = meteringModeProvider()
            val previewViewfinderRect = ViewfinderRect.centered(
                containerWidth = PREVIEW_CONTAINER_ASPECT_RATIO,
                containerHeight = 1f,
                targetAspectRatio = viewfinderAspectRatioProvider().ratio,
            )
            val mappedViewfinderRect = mapPreviewRectToBuffer(
                previewRect = previewViewfinderRect,
                rotationDegrees = image.imageInfo.rotationDegrees,
            )
            val mappedPoint = mapPreviewPointToBuffer(
                previewPoint = previewViewfinderRect.toAbsolutePoint(meteringPointProvider()),
                rotationDegrees = image.imageInfo.rotationDegrees,
            )
            val cropLeft = floor(mappedViewfinderRect.left * width).toInt()
                .coerceIn(0, width.saturatingExclusiveUpperBound())
            val cropTop = floor(mappedViewfinderRect.top * height).toInt()
                .coerceIn(0, height.saturatingExclusiveUpperBound())
            val cropRightExclusive = ceil(mappedViewfinderRect.right * width).toInt()
                .coerceIn(cropLeft + 1, width)
            val cropBottomExclusive = ceil(mappedViewfinderRect.bottom * height).toInt()
                .coerceIn(cropTop + 1, height)
            val cropWidth = (cropRightExclusive - cropLeft).coerceAtLeast(1)
            val cropHeight = (cropBottomExclusive - cropTop).coerceAtLeast(1)
            val stepX = max(1, cropWidth / 48)
            val stepY = max(1, cropHeight / 48)
            val histogram = IntArray(LuminanceReading.HISTOGRAM_BIN_COUNT)
            var sampleCount = 0
            var averageSum = 0.0
            var centerWeightedSum = 0.0
            var centerWeightTotal = 0.0
            var spotSum = 0.0
            var spotCount = 0

            val centerX = cropLeft + cropWidth / 2.0
            val centerY = cropTop + cropHeight / 2.0
            val maxDistance = hypot(cropWidth / 2.0, cropHeight / 2.0).coerceAtLeast(1.0)
            val spotCenterX = mappedPoint.x * width
            val spotCenterY = mappedPoint.y * height
            val spotRadius = min(cropWidth, cropHeight) * 0.12

            for (y in cropTop until cropBottomExclusive step stepY) {
                for (x in cropLeft until cropRightExclusive step stepX) {
                    val byteIndex = y * rowStride + x * pixelStride
                    val luma = buffer.get(byteIndex).toInt() and 0xFF
                    val lumaDouble = luma.toDouble()

                    histogram[luma]++
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
                    histogram = histogram,
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

    private fun mapPreviewRectToBuffer(
        previewRect: ViewfinderRect,
        rotationDegrees: Int,
    ): ViewfinderRect {
        val mappedPoints = listOf(
            mapPreviewPointToBuffer(
                previewPoint = MeteringPoint(previewRect.left, previewRect.top),
                rotationDegrees = rotationDegrees,
            ),
            mapPreviewPointToBuffer(
                previewPoint = MeteringPoint(previewRect.right, previewRect.top),
                rotationDegrees = rotationDegrees,
            ),
            mapPreviewPointToBuffer(
                previewPoint = MeteringPoint(previewRect.left, previewRect.bottom),
                rotationDegrees = rotationDegrees,
            ),
            mapPreviewPointToBuffer(
                previewPoint = MeteringPoint(previewRect.right, previewRect.bottom),
                rotationDegrees = rotationDegrees,
            ),
        )

        return ViewfinderRect(
            left = mappedPoints.minOf { it.x },
            top = mappedPoints.minOf { it.y },
            right = mappedPoints.maxOf { it.x },
            bottom = mappedPoints.maxOf { it.y },
        )
    }
}

private fun Int.saturatingExclusiveUpperBound(): Int {
    return (this - 1).coerceAtLeast(0)
}
