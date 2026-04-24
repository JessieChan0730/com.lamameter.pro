package com.yourbrand.lumameter.pro.ui.meter

import com.yourbrand.lumameter.pro.domain.exposure.AnalysisTool
import com.yourbrand.lumameter.pro.domain.exposure.MeteringMode
import com.yourbrand.lumameter.pro.domain.exposure.MeteringPoint
import com.yourbrand.lumameter.pro.domain.exposure.ReferenceGridType
import com.yourbrand.lumameter.pro.domain.exposure.ViewfinderAspectRatio
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MeterUiLogicTest {

    @Test
    fun `spot reticle stays hidden until a point is selected`() {
        assertNull(
            resolveDisplayedReticlePoint(
                meteringMode = MeteringMode.SPOT,
                meteringPoint = MeteringPoint.normalized(0.2f, 0.8f),
                hasCustomSpotMeteringPoint = false,
            )
        )
        assertEquals(
            MeteringPoint.normalized(0.2f, 0.8f),
            resolveDisplayedReticlePoint(
                meteringMode = MeteringMode.SPOT,
                meteringPoint = MeteringPoint.normalized(0.2f, 0.8f),
                hasCustomSpotMeteringPoint = true,
            )
        )
    }

    @Test
    fun `center weighted reticle stays fixed in the center and average hides it`() {
        assertEquals(
            MeteringPoint.Center,
            resolveDisplayedReticlePoint(
                meteringMode = MeteringMode.CENTER_WEIGHTED,
                meteringPoint = MeteringPoint.normalized(0.9f, 0.1f),
                hasCustomSpotMeteringPoint = true,
            )
        )
        assertNull(
            resolveDisplayedReticlePoint(
                meteringMode = MeteringMode.AVERAGE,
                meteringPoint = MeteringPoint.Center,
                hasCustomSpotMeteringPoint = true,
            )
        )
    }

    @Test
    fun `thirds guide geometry keeps evenly spaced vertical and horizontal lines`() {
        val geometry = buildReferenceGuideGeometry(ReferenceGridType.THIRDS)
        val verticalFractions = geometry.lines
            .filter { it.start.x == it.end.x }
            .map { it.start.x }
            .sorted()
        val horizontalFractions = geometry.lines
            .filter { it.start.y == it.end.y }
            .map { it.start.y }
            .sorted()

        assertEquals(4, geometry.lines.size)
        assertTrue(geometry.spiralPoints.isEmpty())
        assertEquals(1f / 3f, verticalFractions[0], 0.0001f)
        assertEquals(2f / 3f, verticalFractions[1], 0.0001f)
        assertEquals(1f / 3f, horizontalFractions[0], 0.0001f)
        assertEquals(2f / 3f, horizontalFractions[1], 0.0001f)
    }

    @Test
    fun `golden spiral guide geometry adds a focus cross over the spiral`() {
        val geometry = buildReferenceGuideGeometry(ReferenceGridType.GOLDEN_SPIRAL)
        val expectedFocusPoint = resolveGoldenSpiralFocusPoint(
            horizontalOffset = calculateHorizontalCenterOffset(
                points = sampleGoldenSpiralPoints(),
                additionalOffset = -0.07f,
            )
        )
        val visibleHorizontalBounds = visibleHorizontalBounds(geometry.spiralPoints)
        val lastPoint = geometry.spiralPoints.last()
        val verticalGuides = geometry.lines.filter { it.start.x == it.end.x }
        val horizontalGuides = geometry.lines.filter { it.start.y == it.end.y }

        assertEquals(2, geometry.lines.size)
        assertTrue(geometry.spiralPoints.size > 100)
        assertEquals(
            visibleHorizontalBounds.first,
            1f - visibleHorizontalBounds.second,
            0.02f,
        )
        assertEquals(1, verticalGuides.size)
        assertEquals(1, horizontalGuides.size)
        assertTrue(verticalGuides.first().start.x in 0f..1f)
        assertTrue(verticalGuides.first().end.x in 0f..1f)
        assertEquals(expectedFocusPoint.y, horizontalGuides.first().start.y, 0.0001f)
        assertEquals(expectedFocusPoint.y, horizontalGuides.first().end.y, 0.0001f)
        assertEquals(0f, verticalGuides.first().start.y, 0.0001f)
        assertEquals(1f, verticalGuides.first().end.y, 0.0001f)
        assertTrue(horizontalGuides.first().start.x <= 0f)
        assertTrue(horizontalGuides.first().end.x >= 1f)
        assertEquals(expectedFocusPoint.y, lastPoint.y, 0.02f)
        assertTrue(lastPoint.x < verticalGuides.first().start.x)
        assertTrue(geometry.spiralPoints.any { it.x < 0f || it.x > 1f })
    }

    @Test
    fun `golden spiral guide geometry switches to horizontal mode on sixteen by nine viewfinders`() {
        val expectedVerticalFocusPoint = resolveGoldenSpiralFocusPoint(
            horizontalOffset = calculateHorizontalCenterOffset(
                points = sampleGoldenSpiralPoints(),
                additionalOffset = -0.07f,
            )
        )
        val expectedHorizontalFocusPoint = MeteringPoint(
            x = expectedVerticalFocusPoint.y,
            y = expectedVerticalFocusPoint.x,
        )

        val geometry = buildReferenceGuideGeometry(
            type = ReferenceGridType.GOLDEN_SPIRAL,
            viewfinderAspectRatio = ViewfinderAspectRatio.SIXTEEN_NINE,
        )
        val visibleHorizontalBounds = visibleHorizontalBounds(geometry.spiralPoints)
        val lastPoint = geometry.spiralPoints.last()
        val verticalGuides = geometry.lines.filter { it.start.x == it.end.x }
        val horizontalGuides = geometry.lines.filter { it.start.y == it.end.y }

        assertEquals(2, geometry.lines.size)
        assertTrue(geometry.spiralPoints.size > 100)
        assertEquals(
            visibleHorizontalBounds.first,
            1f - visibleHorizontalBounds.second,
            0.02f,
        )
        assertEquals(1, verticalGuides.size)
        assertEquals(1, horizontalGuides.size)
        assertTrue(verticalGuides.first().start.x in 0f..1f)
        assertTrue(verticalGuides.first().end.x in 0f..1f)
        assertEquals(expectedHorizontalFocusPoint.y, horizontalGuides.first().start.y, 0.0001f)
        assertEquals(expectedHorizontalFocusPoint.y, horizontalGuides.first().end.y, 0.0001f)
        assertTrue(horizontalGuides.first().start.x <= 0f)
        assertTrue(horizontalGuides.first().end.x >= 1f)
        assertTrue(lastPoint.x in 0f..1.2f)
        assertTrue(geometry.spiralPoints.any { it.y < 0f || it.y > 1f })
    }

    @Test
    fun `golden spiral guide geometry stays vertical on portrait viewfinders`() {
        val expectedFocusPoint = resolveGoldenSpiralFocusPoint(
            horizontalOffset = calculateHorizontalCenterOffset(
                points = sampleGoldenSpiralPoints(),
                additionalOffset = -0.07f,
            )
        )
        val geometry = buildReferenceGuideGeometry(
            type = ReferenceGridType.GOLDEN_SPIRAL,
            viewfinderAspectRatio = ViewfinderAspectRatio.THREE_FOUR,
        )
        val visibleHorizontalBounds = visibleHorizontalBounds(geometry.spiralPoints)
        val lastPoint = geometry.spiralPoints.last()
        val verticalGuides = geometry.lines.filter { it.start.x == it.end.x }
        val horizontalGuides = geometry.lines.filter { it.start.y == it.end.y }

        assertEquals(2, geometry.lines.size)
        assertTrue(geometry.spiralPoints.size > 100)
        assertEquals(
            visibleHorizontalBounds.first,
            1f - visibleHorizontalBounds.second,
            0.02f,
        )
        assertEquals(1, verticalGuides.size)
        assertEquals(1, horizontalGuides.size)
        assertTrue(verticalGuides.first().start.x in 0f..1f)
        assertTrue(verticalGuides.first().end.x in 0f..1f)
        assertEquals(expectedFocusPoint.y, horizontalGuides.first().start.y, 0.0001f)
        assertEquals(expectedFocusPoint.y, horizontalGuides.first().end.y, 0.0001f)
        assertEquals(expectedFocusPoint.y, lastPoint.y, 0.02f)
        assertTrue(horizontalGuides.first().start.x <= 0f)
        assertTrue(horizontalGuides.first().end.x >= 1f)
        assertTrue(lastPoint.x < verticalGuides.first().start.x)
        assertTrue(geometry.spiralPoints.any { it.x < 0f || it.x > 1f })
    }

    @Test
    fun `diagonal guide geometry draws both diagonals across the frame`() {
        val geometry = buildReferenceGuideGeometry(ReferenceGridType.DIAGONAL)

        assertEquals(2, geometry.lines.size)
        assertTrue(geometry.spiralPoints.isEmpty())
        assertEquals(MeteringPoint(x = 0f, y = 0f), geometry.lines[0].start)
        assertEquals(MeteringPoint(x = 1f, y = 1f), geometry.lines[0].end)
        assertEquals(MeteringPoint(x = 1f, y = 0f), geometry.lines[1].start)
        assertEquals(MeteringPoint(x = 0f, y = 1f), geometry.lines[1].end)
    }

    @Test
    fun `preview hint only advertises tap interaction when it matches the mode`() {
        assertEquals(
            PreviewTapHint.TAP_TO_METER,
            resolvePreviewTapHint(
                analysisTool = AnalysisTool.METER,
                meteringMode = MeteringMode.SPOT,
                isLiveMeteringEnabled = true,
                hasCustomSpotMeteringPoint = false,
            )
        )
        assertEquals(
            PreviewTapHint.TAP_TO_REPOSITION_METER,
            resolvePreviewTapHint(
                analysisTool = AnalysisTool.METER,
                meteringMode = MeteringMode.SPOT,
                isLiveMeteringEnabled = true,
                hasCustomSpotMeteringPoint = true,
            )
        )
        assertEquals(
            PreviewTapHint.TAP_TO_METER_ONCE,
            resolvePreviewTapHint(
                analysisTool = AnalysisTool.METER,
                meteringMode = MeteringMode.CENTER_WEIGHTED,
                isLiveMeteringEnabled = false,
                hasCustomSpotMeteringPoint = false,
            )
        )
        assertNull(
            resolvePreviewTapHint(
                analysisTool = AnalysisTool.METER,
                meteringMode = MeteringMode.AVERAGE,
                isLiveMeteringEnabled = true,
                hasCustomSpotMeteringPoint = false,
            )
        )
        assertEquals(
            PreviewTapHint.TAP_TO_SAMPLE_WHITE_BALANCE,
            resolvePreviewTapHint(
                analysisTool = AnalysisTool.WHITE_BALANCE,
                meteringMode = MeteringMode.AVERAGE,
                isLiveMeteringEnabled = false,
                hasCustomSpotMeteringPoint = false,
            )
        )
    }

    @Test
    fun `shutter formatter renders one second as 1s instead of a fraction`() {
        assertEquals("1s", formatShutter(1.0))
        assertEquals("1s", formatShutter(0.999))
        assertEquals("1/2", formatShutter(0.5))
        assertEquals("1s", formatShutter(0.75))
        assertEquals("1/29", formatShutter(0.035))
    }

    @Test
    fun `shutter formatter always uses fraction form below 1s`() {
        assertEquals("1/10", formatShutter(0.1))
        assertEquals("1/100", formatShutter(0.01))
        assertEquals("1/125", formatShutter(1.0 / 125.0))
        assertEquals("1/250", formatShutter(1.0 / 250.0))
        assertEquals("1/4000", formatShutter(1.0 / 4000.0))
    }

    @Test
    fun `shutter formatter uses decimal seconds at or above 1s`() {
        assertEquals("1s", formatShutter(1.0))
        assertEquals("1.1s", formatShutter(1.1))
        assertEquals("2s", formatShutter(2.0))
        assertEquals("4s", formatShutter(4.0))
        assertEquals("30s", formatShutter(30.0))
    }

    @Test
    fun `pinned summary appears only after live status row has scrolled away`() {
        assertFalse(
            shouldShowPinnedSummary(
                hasCameraPermission = false,
                viewportTopPx = 100f,
                statusRowBottomPx = 90f,
            )
        )
        assertFalse(
            shouldShowPinnedSummary(
                hasCameraPermission = true,
                viewportTopPx = 100f,
                statusRowBottomPx = 101.5f,
            )
        )
        assertTrue(
            shouldShowPinnedSummary(
                hasCameraPermission = true,
                viewportTopPx = 100f,
                statusRowBottomPx = 100.5f,
            )
        )
    }

    private fun visibleHorizontalBounds(
        points: List<MeteringPoint>,
    ): Pair<Float, Float> {
        val visiblePoints = points.filter { point ->
            point.x in 0f..1f && point.y in 0f..1f
        }
        return visiblePoints.minOf { it.x } to visiblePoints.maxOf { it.x }
    }
}
