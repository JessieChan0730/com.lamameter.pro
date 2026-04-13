package com.yourbrand.lumameter.pro.ui.meter

import com.yourbrand.lumameter.pro.domain.exposure.MeteringMode
import com.yourbrand.lumameter.pro.domain.exposure.MeteringPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
                meteringMode = MeteringMode.SPOT,
                isLiveMeteringEnabled = true,
                hasCustomSpotMeteringPoint = false,
            )
        )
        assertEquals(
            PreviewTapHint.TAP_TO_REPOSITION_METER,
            resolvePreviewTapHint(
                meteringMode = MeteringMode.SPOT,
                isLiveMeteringEnabled = true,
                hasCustomSpotMeteringPoint = true,
            )
        )
        assertEquals(
            PreviewTapHint.TAP_TO_METER_ONCE,
            resolvePreviewTapHint(
                meteringMode = MeteringMode.CENTER_WEIGHTED,
                isLiveMeteringEnabled = false,
                hasCustomSpotMeteringPoint = false,
            )
        )
        assertNull(
            resolvePreviewTapHint(
                meteringMode = MeteringMode.AVERAGE,
                isLiveMeteringEnabled = true,
                hasCustomSpotMeteringPoint = false,
            )
        )
    }

    @Test
    fun `shutter formatter renders one second as 1s instead of a fraction`() {
        assertEquals("1s", formatShutter(1.0))
        assertEquals("1s", formatShutter(0.999))
        assertEquals("1/2", formatShutter(0.5))
        assertEquals("0.8s", formatShutter(0.75))
        assertEquals("1/29", formatShutter(0.035))
    }
}
