package com.yourbrand.lumameter.pro.domain.exposure

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExposureModelsTest {

    @Test
    fun `center metering point uses normalized midpoint`() {
        assertEquals(MeteringPoint(0.5f, 0.5f), MeteringPoint.Center)
    }

    @Test
    fun `normalized metering point clamps coordinates into preview range`() {
        val point = MeteringPoint.normalized(x = -0.2f, y = 1.6f)

        assertEquals(0f, point.x, 0.0001f)
        assertEquals(1f, point.y, 0.0001f)
    }

    @Test
    fun `stored viewfinder ratio migrates legacy nine by six and falls back when unknown`() {
        assertEquals(ViewfinderAspectRatio.FOUR_THREE, ViewfinderAspectRatio.fromStorageValue("7:5"))
        assertEquals(ViewfinderAspectRatio.FOUR_THREE, ViewfinderAspectRatio.fromStorageValue("9:6"))
        assertEquals(ViewfinderAspectRatio.THREE_FOUR, ViewfinderAspectRatio.fromStorageValue("2:3"))
        assertEquals(ViewfinderAspectRatio.THREE_FOUR, ViewfinderAspectRatio.fromStorageValue("3:4"))
    }

    @Test
    fun `four by three and sixteen by nine viewfinders exclude golden spiral guides`() {
        assertEquals(
            listOf(ReferenceGridType.THIRDS, ReferenceGridType.DIAGONAL),
            ViewfinderAspectRatio.FOUR_THREE.supportedReferenceGridTypes(),
        )
        assertEquals(
            listOf(ReferenceGridType.THIRDS, ReferenceGridType.DIAGONAL),
            ViewfinderAspectRatio.SIXTEEN_NINE.supportedReferenceGridTypes(),
        )
    }

    @Test
    fun `portrait and square viewfinders keep golden spiral guides available`() {
        assertTrue(
            ReferenceGridType.GOLDEN_SPIRAL in
                ViewfinderAspectRatio.THREE_FOUR.supportedReferenceGridTypes(),
        )
        assertTrue(
            ReferenceGridType.GOLDEN_SPIRAL in
                ViewfinderAspectRatio.SQUARE.supportedReferenceGridTypes(),
        )
    }

    @Test
    fun `unsupported golden spiral guides fall back to thirds for wide viewfinders`() {
        assertEquals(
            ReferenceGridType.THIRDS,
            ReferenceGridType.normalizeForViewfinder(
                referenceGridType = ReferenceGridType.GOLDEN_SPIRAL,
                viewfinderAspectRatio = ViewfinderAspectRatio.FOUR_THREE,
            ),
        )
        assertEquals(
            ReferenceGridType.THIRDS,
            ReferenceGridType.normalizeForViewfinder(
                referenceGridType = ReferenceGridType.GOLDEN_SPIRAL,
                viewfinderAspectRatio = ViewfinderAspectRatio.SIXTEEN_NINE,
            ),
        )
    }

    @Test
    fun `supported reference guides remain unchanged when normalized`() {
        assertEquals(
            ReferenceGridType.DIAGONAL,
            ReferenceGridType.normalizeForViewfinder(
                referenceGridType = ReferenceGridType.DIAGONAL,
                viewfinderAspectRatio = ViewfinderAspectRatio.FOUR_THREE,
            ),
        )
        assertEquals(
            ReferenceGridType.GOLDEN_SPIRAL,
            ReferenceGridType.normalizeForViewfinder(
                referenceGridType = ReferenceGridType.GOLDEN_SPIRAL,
                viewfinderAspectRatio = ViewfinderAspectRatio.THREE_FOUR,
            ),
        )
        assertFalse(
            ReferenceGridType.GOLDEN_SPIRAL.isSupportedFor(ViewfinderAspectRatio.SIXTEEN_NINE),
        )
    }

    @Test
    fun `square viewfinder is centered inside the preview container`() {
        val rect = ViewfinderRect.centered(
            containerWidth = 4f,
            containerHeight = 3f,
            targetAspectRatio = ViewfinderAspectRatio.SQUARE.ratio,
        )

        assertEquals(0.125f, rect.left, 0.0001f)
        assertEquals(0f, rect.top, 0.0001f)
        assertEquals(0.875f, rect.right, 0.0001f)
        assertEquals(1f, rect.bottom, 0.0001f)
    }

    @Test
    fun `portrait viewfinder is centered inside the preview container`() {
        val rect = ViewfinderRect.centered(
            containerWidth = 4f,
            containerHeight = 3f,
            targetAspectRatio = ViewfinderAspectRatio.THREE_FOUR.ratio,
        )

        assertEquals(0.21875f, rect.left, 0.0001f)
        assertEquals(0f, rect.top, 0.0001f)
        assertEquals(0.78125f, rect.right, 0.0001f)
        assertEquals(1f, rect.bottom, 0.0001f)
    }

    @Test
    fun `viewfinder rect converts between local and absolute metering points`() {
        val rect = ViewfinderRect(
            left = 0.125f,
            top = 0f,
            right = 0.875f,
            bottom = 1f,
        )
        val localPoint = MeteringPoint.normalized(1f, 0.5f)

        val absolutePoint = rect.toAbsolutePoint(localPoint)

        assertEquals(0.875f, absolutePoint.x, 0.0001f)
        assertEquals(0.5f, absolutePoint.y, 0.0001f)
        assertEquals(localPoint, rect.toLocalPoint(absolutePoint))
    }

    @Test
    fun `placeholder exposure result provides safe startup defaults`() {
        val result = ExposureResult.placeholder()

        assertEquals(0.0, result.sceneEv100, 0.0001)
        assertEquals(0.0, result.workingEv, 0.0001)
        assertEquals(100, result.iso)
        assertEquals(2.8, result.aperture, 0.0001)
        assertEquals(1.0 / 60.0, result.shutterSeconds, 0.0001)
        assertEquals(ExposureMode.APERTURE_PRIORITY, result.exposureMode)
        assertEquals(0.0, result.compensationEv, 0.0001)
    }
}
