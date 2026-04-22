package com.yourbrand.lumameter.pro.ui.meter

import com.yourbrand.lumameter.pro.domain.exposure.AnalysisTool
import com.yourbrand.lumameter.pro.domain.exposure.MeteringMode
import com.yourbrand.lumameter.pro.domain.exposure.MeteringPoint
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
    fun `focus hint bands resolve to the expected scene icons`() {
        assertEquals(FocusSceneHint.INSECT, resolveFocusSceneHint(0.1f))
        assertEquals(FocusSceneHint.INSECT, resolveFocusSceneHint(0.46f))
        assertEquals(FocusSceneHint.DINING, resolveFocusSceneHint(0.47f))
        assertEquals(FocusSceneHint.DINING, resolveFocusSceneHint(0.54f))
        assertNull(resolveFocusSceneHint(0.8f))
        assertEquals(FocusSceneHint.PERSON, resolveFocusSceneHint(1.0f))
        assertEquals(FocusSceneHint.PAIR, resolveFocusSceneHint(1.4f))
        assertEquals(FocusSceneHint.GROUP, resolveFocusSceneHint(2.1f))
        assertEquals(FocusSceneHint.MOUNTAIN, resolveFocusSceneHint(5.1f))
        assertEquals(FocusSceneHint.MOUNTAIN, resolveFocusSceneHint(null))
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
}
