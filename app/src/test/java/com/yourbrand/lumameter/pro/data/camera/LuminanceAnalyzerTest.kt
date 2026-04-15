package com.yourbrand.lumameter.pro.data.camera

import android.graphics.Rect
import android.media.Image
import androidx.camera.core.ImageInfo
import androidx.camera.core.ImageProxy
import androidx.camera.core.impl.TagBundle
import androidx.camera.core.impl.utils.ExifData
import com.yourbrand.lumameter.pro.domain.exposure.CameraCaptureMetadata
import com.yourbrand.lumameter.pro.domain.exposure.LuminanceReading
import com.yourbrand.lumameter.pro.domain.exposure.MeteringMode
import com.yourbrand.lumameter.pro.domain.exposure.MeteringPoint
import com.yourbrand.lumameter.pro.domain.exposure.ViewfinderAspectRatio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.ByteBuffer
import kotlin.math.abs
import kotlin.math.hypot

class LuminanceAnalyzerTest {

    @Test
    fun `average metering reports sampled frame average and closes image`() {
        val image = fakeImageProxy(
            width = 4,
            height = 4,
        ) { x, y ->
            x + y * 4
        }

        val reading = analyze(
            image = image,
            meteringMode = MeteringMode.AVERAGE,
            meteringPoint = MeteringPoint.Center,
        )

        assertNotNull(reading)
        assertEquals(7.5, reading?.averageLuma ?: 0.0, 0.0001)
        assertEquals(7.5, reading?.meteredLuma ?: 0.0, 0.0001)
        assertEquals(4, reading?.frameWidth)
        assertEquals(4, reading?.frameHeight)
        assertEquals(MeteringMode.AVERAGE, reading?.meteringMode)
        assertEquals(MeteringPoint.Center, reading?.meteringPoint)
        assertTrue(image.isClosed)
    }

    @Test
    fun `center weighted metering prioritizes brighter center pixels`() {
        val image = fakeImageProxy(
            width = 10,
            height = 10,
        ) { x, y ->
            if (x in 3..6 && y in 3..6) 200 else 20
        }

        val reading = analyze(
            image = image,
            meteringMode = MeteringMode.CENTER_WEIGHTED,
            meteringPoint = MeteringPoint.Center,
        )

        assertNotNull(reading)
        assertTrue((reading?.meteredLuma ?: 0.0) > (reading?.averageLuma ?: 0.0))
        assertTrue((reading?.meteredLuma ?: 0.0) < 200.0)
        assertTrue(image.isClosed)
    }

    @Test
    fun `center weighted metering ignores an off-center tap point`() {
        val image = fakeImageProxy(
            width = 10,
            height = 10,
        ) { x, y ->
            if (x in 3..6 && y in 3..6) 180 else 20
        }

        val reading = analyze(
            image = image,
            meteringMode = MeteringMode.CENTER_WEIGHTED,
            meteringPoint = MeteringPoint.normalized(0.9f, 0.1f),
        )

        assertNotNull(reading)
        assertEquals(MeteringPoint.Center, reading?.meteringPoint)
    }

    @Test
    fun `average metering uses the active square viewfinder crop`() {
        val image = fakeImageProxy(
            width = 8,
            height = 6,
        ) { x, _ ->
            if (x in 1..6) 100 else 0
        }

        val reading = analyze(
            image = image,
            meteringMode = MeteringMode.AVERAGE,
            meteringPoint = MeteringPoint.Center,
            viewfinderAspectRatio = ViewfinderAspectRatio.SQUARE,
        )

        assertNotNull(reading)
        assertEquals(100.0, reading?.averageLuma ?: 0.0, 0.0001)
        assertEquals(100.0, reading?.meteredLuma ?: 0.0, 0.0001)
    }

    @Test
    fun `center weighted metering ignores masked columns outside the square viewfinder`() {
        val image = fakeImageProxy(
            width = 8,
            height = 6,
        ) { x, _ ->
            if (x in 1..6) 40 else 240
        }

        val reading = analyze(
            image = image,
            meteringMode = MeteringMode.CENTER_WEIGHTED,
            meteringPoint = MeteringPoint.Center,
            viewfinderAspectRatio = ViewfinderAspectRatio.SQUARE,
        )

        assertNotNull(reading)
        assertEquals(40.0, reading?.averageLuma ?: 0.0, 0.0001)
        assertEquals(40.0, reading?.meteredLuma ?: 0.0, 0.0001)
    }

    @Test
    fun `spot metering samples around the selected metering point`() {
        val image = fakeImageProxy(
            width = 10,
            height = 10,
        ) { x, y ->
            if (hypot(x - 8.0, y - 2.0) <= 1.2) 240 else 10
        }

        val reading = analyze(
            image = image,
            meteringMode = MeteringMode.SPOT,
            meteringPoint = MeteringPoint.normalized(0.8f, 0.2f),
        )

        assertNotNull(reading)
        assertEquals(240.0, reading?.meteredLuma ?: 0.0, 0.0001)
        assertTrue((reading?.averageLuma ?: 0.0) < 30.0)
    }

    @Test
    fun `spot metering maps the point inside the active square viewfinder`() {
        val image = fakeImageProxy(
            width = 8,
            height = 6,
        ) { x, y ->
            if (hypot(x - 6.0, y - 3.0) <= 0.9) 220 else 5
        }

        val reading = analyze(
            image = image,
            meteringMode = MeteringMode.SPOT,
            meteringPoint = MeteringPoint.normalized(5f / 6f, 0.5f),
            viewfinderAspectRatio = ViewfinderAspectRatio.SQUARE,
        )

        assertNotNull(reading)
        assertEquals(MeteringPoint.normalized(0.75f, 0.5f), reading?.meteringPoint)
        assertEquals(220.0, reading?.meteredLuma ?: 0.0, 0.0001)
    }

    @Test
    fun `rotation remaps preview points before spot metering`() {
        val image = fakeImageProxy(
            width = 10,
            height = 10,
            rotationDegrees = 90,
        ) { x, y ->
            if (hypot(x - 3.0, y - 8.0) <= 1.2) 230 else 5
        }

        val reading = analyze(
            image = image,
            meteringMode = MeteringMode.SPOT,
            meteringPoint = MeteringPoint.normalized(0.2f, 0.3f),
        )

        assertNotNull(reading)
        assertEquals(MeteringPoint.normalized(0.3f, 0.8f), reading?.meteringPoint)
        assertEquals(90, reading?.rotationDegrees)
        assertEquals(230.0, reading?.meteredLuma ?: 0.0, 0.0001)
    }

    @Test
    fun `image is still closed when no plane is available`() {
        val image = FakeImageProxy(
            width = 8,
            height = 8,
            planes = emptyArray(),
            rotationDegrees = 0,
        )

        val reading = analyze(
            image = image,
            meteringMode = MeteringMode.AVERAGE,
            meteringPoint = MeteringPoint.Center,
        )

        assertNull(reading)
        assertTrue(image.isClosed)
    }

    @Test
    fun `histogram bins match sampled pixel counts`() {
        val image = fakeImageProxy(
            width = 4,
            height = 4,
        ) { x, _ ->
            if (x < 2) 32 else 224
        }

        val reading = analyze(
            image = image,
            meteringMode = MeteringMode.AVERAGE,
            meteringPoint = MeteringPoint.Center,
        )

        assertNotNull(reading)
        val histogram = reading!!.histogram
        assertEquals(LuminanceReading.HISTOGRAM_BIN_COUNT, histogram.size)
        assertEquals(16, histogram.sum())
        assertEquals(8, histogram[32])
        assertEquals(8, histogram[224])
    }

    @Test
    fun `analyzer forwards capture metadata with the reading`() {
        val image = fakeImageProxy(
            width = 4,
            height = 4,
        ) { _, _ -> 118 }
        val metadata = CameraCaptureMetadata(
            aperture = 1.8,
            exposureTimeNs = 12_500_000L,
            sensitivityIso = 100,
        )

        val reading = analyze(
            image = image,
            meteringMode = MeteringMode.AVERAGE,
            meteringPoint = MeteringPoint.Center,
            captureMetadata = metadata,
        )

        assertEquals(metadata, reading?.captureMetadata)
    }

    private fun analyze(
        image: FakeImageProxy,
        meteringMode: MeteringMode,
        meteringPoint: MeteringPoint,
        viewfinderAspectRatio: ViewfinderAspectRatio = ViewfinderAspectRatio.FOUR_THREE,
        captureMetadata: CameraCaptureMetadata? = null,
    ): LuminanceReading? {
        var reading: LuminanceReading? = null
        val analyzer = LuminanceAnalyzer(
            meteringModeProvider = { meteringMode },
            meteringPointProvider = { meteringPoint },
            viewfinderAspectRatioProvider = { viewfinderAspectRatio },
            captureMetadataProvider = { captureMetadata },
            onReadingAvailable = { reading = it },
        )

        analyzer.analyze(image)

        return reading
    }

    private fun fakeImageProxy(
        width: Int,
        height: Int,
        rotationDegrees: Int = 0,
        luminanceProvider: (x: Int, y: Int) -> Int,
    ): FakeImageProxy {
        val bytes = ByteArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                bytes[index] = luminanceProvider(x, y).coerceIn(0, 255).toByte()
            }
        }

        return FakeImageProxy(
            width = width,
            height = height,
            planes = arrayOf(
                FakePlaneProxy(
                    data = bytes,
                    rowStride = width,
                    pixelStride = 1,
                )
            ),
            rotationDegrees = rotationDegrees,
        )
    }

    private class FakePlaneProxy(
        private val data: ByteArray,
        private val rowStride: Int,
        private val pixelStride: Int,
    ) : ImageProxy.PlaneProxy {

        override fun getBuffer(): ByteBuffer {
            return ByteBuffer.wrap(data)
        }

        override fun getPixelStride(): Int {
            return pixelStride
        }

        override fun getRowStride(): Int {
            return rowStride
        }
    }

    private class FakeImageProxy(
        private val width: Int,
        private val height: Int,
        private val planes: Array<ImageProxy.PlaneProxy>,
        rotationDegrees: Int,
    ) : ImageProxy {

        private var cropRect: Rect = Rect(0, 0, width, height)

        val isClosed: Boolean
            get() = closed

        private var closed = false
        private val imageInfo = FakeImageInfo(rotationDegrees)

        override fun close() {
            closed = true
        }

        override fun getCropRect(): Rect {
            return cropRect
        }

        override fun setCropRect(rect: Rect?) {
            cropRect = rect ?: Rect(0, 0, width, height)
        }

        override fun getFormat(): Int {
            return 35
        }

        override fun getHeight(): Int {
            return height
        }

        override fun getWidth(): Int {
            return width
        }

        override fun getImage(): Image? {
            return null
        }

        override fun getImageInfo(): ImageInfo {
            return imageInfo
        }

        override fun getPlanes(): Array<ImageProxy.PlaneProxy> {
            return planes
        }
    }

    private class FakeImageInfo(
        private val rotationDegrees: Int,
    ) : ImageInfo {

        override fun getRotationDegrees(): Int {
            return rotationDegrees
        }

        override fun getTagBundle(): TagBundle {
            return TagBundle.emptyBundle()
        }

        override fun getTimestamp(): Long {
            return 0L
        }

        override fun populateExifData(exifBuilder: ExifData.Builder) = Unit
    }
}
