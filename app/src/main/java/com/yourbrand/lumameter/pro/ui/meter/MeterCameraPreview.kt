package com.yourbrand.lumameter.pro.ui.meter

import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.yourbrand.lumameter.pro.R
import com.yourbrand.lumameter.pro.data.camera.LuminanceAnalyzer
import com.yourbrand.lumameter.pro.domain.exposure.FrameExposureMetadata
import com.yourbrand.lumameter.pro.domain.exposure.LuminanceReading
import com.yourbrand.lumameter.pro.domain.exposure.MeteringMode
import com.yourbrand.lumameter.pro.domain.exposure.MeteringPoint
import com.yourbrand.lumameter.pro.domain.exposure.ReferenceGridType
import com.yourbrand.lumameter.pro.domain.exposure.ViewfinderAspectRatio
import com.yourbrand.lumameter.pro.domain.exposure.WhiteBalanceGains
import java.util.concurrent.CancellationException
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sin

@Composable
fun MeterCameraPreview(
    modifier: Modifier = Modifier,
    meteringMode: MeteringMode,
    meteringPoint: MeteringPoint,
    hasCustomSpotMeteringPoint: Boolean,
    viewfinderAspectRatio: ViewfinderAspectRatio,
    isAeLocked: Boolean,
    requestedZoomRatio: Float,
    enableSpotMetering: Boolean,
    showMeteringReticle: Boolean,
    referenceGridType: ReferenceGridType?,
    showLevelIndicator: Boolean,
    onMeteringPointChanged: (MeteringPoint) -> Unit,
    onPreviewTapped: () -> Unit,
    onReadingAvailable: (LuminanceReading) -> Unit,
    onZoomCapabilityResolved: (Float, Float) -> Unit,
    onZoomRatioApplied: (Float) -> Unit,
    onCameraError: (String) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraStartErrorMsg = stringResource(R.string.failed_to_start_camera)
    val zoomErrorMsg = stringResource(R.string.failed_to_adjust_zoom)

    val previewView = remember(context) {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    val currentMeteringMode by rememberUpdatedState(meteringMode)
    val currentMeteringPoint by rememberUpdatedState(meteringPoint)
    val currentViewfinderAspectRatio by rememberUpdatedState(viewfinderAspectRatio)
    val currentEnableSpotMetering by rememberUpdatedState(enableSpotMetering)
    val currentReadingCallback by rememberUpdatedState(onReadingAvailable)
    val currentRequestedZoomRatio by rememberUpdatedState(requestedZoomRatio)
    val currentZoomCapabilityCallback by rememberUpdatedState(onZoomCapabilityResolved)
    val currentZoomRatioCallback by rememberUpdatedState(onZoomRatioApplied)
    val currentErrorCallback by rememberUpdatedState(onCameraError)
    var boundCamera by remember { mutableStateOf<Camera?>(null) }
    var lastAppliedZoomRatio by remember { mutableStateOf<Float?>(null) }
    var latestZoomRequestToken by remember { mutableStateOf(0) }

    DisposableEffect(lifecycleOwner, previewView) {
        val analyzerExecutor = Executors.newSingleThreadExecutor()
        val latestMetadata = AtomicReference<FrameExposureMetadata?>(null)
        var cameraProvider: ProcessCameraProvider? = null
        val providerFuture = ProcessCameraProvider.getInstance(context)

        val bindCameraUseCases = Runnable {
            runCatching {
                val provider = providerFuture.get()
                cameraProvider = provider

                val preview = Preview.Builder()
                    .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                    .build()
                    .also { it.setSurfaceProvider(previewView.surfaceProvider) }

                val analysisBuilder = ImageAnalysis.Builder()
                    .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)

                @Suppress("RestrictedApi")
                @androidx.camera.camera2.interop.ExperimentalCamera2Interop
                Camera2Interop.Extender(analysisBuilder)
                    .setSessionCaptureCallback(object : CameraCaptureSession.CaptureCallback() {
                        override fun onCaptureCompleted(
                            session: CameraCaptureSession,
                            request: CaptureRequest,
                            result: TotalCaptureResult,
                        ) {
                            val exposureTimeNs = result.get(CaptureResult.SENSOR_EXPOSURE_TIME)
                            val sensitivity = result.get(CaptureResult.SENSOR_SENSITIVITY)
                            val aperture = result.get(CaptureResult.LENS_APERTURE)
                            val colorGains = result.get(CaptureResult.COLOR_CORRECTION_GAINS)
                            if (exposureTimeNs != null && sensitivity != null && aperture != null) {
                                latestMetadata.set(
                                    FrameExposureMetadata(
                                        exposureTimeNs = exposureTimeNs,
                                        sensitivity = sensitivity,
                                        aperture = aperture,
                                        whiteBalanceGains = colorGains?.let { gains ->
                                            WhiteBalanceGains(
                                                red = gains.red,
                                                greenEven = gains.greenEven,
                                                greenOdd = gains.greenOdd,
                                                blue = gains.blue,
                                            )
                                        },
                                    )
                                )
                            }
                        }
                    })

                val analysis = analysisBuilder.build()

                analysis.setAnalyzer(
                    analyzerExecutor,
                    LuminanceAnalyzer(
                        meteringModeProvider = { currentMeteringMode },
                        meteringPointProvider = { currentMeteringPoint },
                        viewfinderAspectRatioProvider = { currentViewfinderAspectRatio },
                        metadataProvider = { latestMetadata.get() },
                        onReadingAvailable = { reading -> currentReadingCallback(reading) },
                    )
                )

                provider.unbindAll()
                val camera = provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis,
                )
                boundCamera = camera

                val zoomState = camera.cameraInfo.zoomState.value
                val minZoomRatio = zoomState?.minZoomRatio ?: DEFAULT_ZOOM_RATIO
                val maxZoomRatio = zoomState?.maxZoomRatio ?: DEFAULT_ZOOM_RATIO
                currentZoomCapabilityCallback(minZoomRatio, maxZoomRatio)

                val safeZoomRatio = currentRequestedZoomRatio.coerceIn(minZoomRatio, maxZoomRatio)
                lastAppliedZoomRatio = safeZoomRatio
                latestZoomRequestToken += 1
                val zoomRequestToken = latestZoomRequestToken
                applyZoomRatio(
                    camera = camera,
                    zoomRatio = safeZoomRatio,
                    context = context,
                    zoomErrorMessage = zoomErrorMsg,
                    isLatestRequest = { zoomRequestToken == latestZoomRequestToken },
                    onZoomRatioApplied = currentZoomRatioCallback,
                    onZoomRequestFailed = {
                        if (zoomRequestToken == latestZoomRequestToken) {
                            lastAppliedZoomRatio = null
                        }
                    },
                    onCameraError = currentErrorCallback,
                )
            }.onFailure { _ ->
                boundCamera = null
                currentErrorCallback(cameraStartErrorMsg)
            }
        }

        providerFuture.addListener(
            bindCameraUseCases,
            ContextCompat.getMainExecutor(context),
        )

        onDispose {
            boundCamera = null
            lastAppliedZoomRatio = null
            latestZoomRequestToken += 1
            cameraProvider?.unbindAll()
            analyzerExecutor.shutdown()
        }
    }

    LaunchedEffect(boundCamera, requestedZoomRatio) {
        val camera = boundCamera ?: return@LaunchedEffect
        val zoomState = camera.cameraInfo.zoomState.value
        val minZoomRatio = zoomState?.minZoomRatio ?: DEFAULT_ZOOM_RATIO
        val maxZoomRatio = zoomState?.maxZoomRatio ?: DEFAULT_ZOOM_RATIO
        val safeZoomRatio = requestedZoomRatio.coerceIn(minZoomRatio, maxZoomRatio)

        if (lastAppliedZoomRatio != null && abs(lastAppliedZoomRatio!! - safeZoomRatio) < ZOOM_UPDATE_EPSILON) {
            return@LaunchedEffect
        }

        lastAppliedZoomRatio = safeZoomRatio
        latestZoomRequestToken += 1
        val zoomRequestToken = latestZoomRequestToken
        applyZoomRatio(
            camera = camera,
            zoomRatio = safeZoomRatio,
            context = context,
            zoomErrorMessage = zoomErrorMsg,
            isLatestRequest = { zoomRequestToken == latestZoomRequestToken },
            onZoomRatioApplied = currentZoomRatioCallback,
            onZoomRequestFailed = {
                if (zoomRequestToken == latestZoomRequestToken) {
                    lastAppliedZoomRatio = null
                }
            },
            onCameraError = currentErrorCallback,
        )
    }

    val displayedReticlePoint = resolveDisplayedReticlePoint(
        meteringMode = meteringMode,
        meteringPoint = meteringPoint,
        hasCustomSpotMeteringPoint = hasCustomSpotMeteringPoint,
    )

    Box(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures { tapOffset ->
                if (currentEnableSpotMetering && currentMeteringMode == MeteringMode.SPOT) {
                    val width = size.width.toFloat().coerceAtLeast(1f)
                    val height = size.height.toFloat().coerceAtLeast(1f)
                    onMeteringPointChanged(
                        MeteringPoint.normalized(
                            x = tapOffset.x / width,
                            y = tapOffset.y / height,
                        )
                    )
                }
                onPreviewTapped()
            }
        }
    ) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize(),
        )

        referenceGridType?.let { type ->
            ReferenceGridOverlay(
                type = type,
                viewfinderAspectRatio = viewfinderAspectRatio,
            )
        }

        if (showLevelIndicator) {
            LevelIndicatorOverlay()
        }

        displayedReticlePoint?.takeIf { showMeteringReticle }?.let { reticlePoint ->
            MeterReticle(
                meteringPoint = reticlePoint,
                isAeLocked = isAeLocked,
                meteringMode = meteringMode,
            )
        }
    }
}

internal fun resolveDisplayedReticlePoint(
    meteringMode: MeteringMode,
    meteringPoint: MeteringPoint,
    hasCustomSpotMeteringPoint: Boolean,
): MeteringPoint? {
    return when (meteringMode) {
        MeteringMode.AVERAGE -> null
        MeteringMode.CENTER_WEIGHTED -> MeteringPoint.Center
        MeteringMode.SPOT -> if (hasCustomSpotMeteringPoint) meteringPoint else null
    }
}

internal data class GuideLineSegment(
    val start: MeteringPoint,
    val end: MeteringPoint,
)

internal data class ReferenceGuideGeometry(
    val lines: List<GuideLineSegment>,
    val spiralPoints: List<MeteringPoint> = emptyList(),
)

internal fun buildReferenceGuideGeometry(
    type: ReferenceGridType,
    viewfinderAspectRatio: ViewfinderAspectRatio = ViewfinderAspectRatio.Default,
): ReferenceGuideGeometry {
    return when (type) {
        ReferenceGridType.THIRDS -> ReferenceGuideGeometry(
            lines = buildGuideLineSegments(
                fractions = listOf(1f / 3f, 2f / 3f),
            ),
        )

        ReferenceGridType.GOLDEN_SPIRAL -> buildGoldenSpiralGeometry(
            horizontalMode = viewfinderAspectRatio.prefersHorizontalGoldenSpiral(),
        )

        ReferenceGridType.DIAGONAL -> ReferenceGuideGeometry(
            lines = listOf(
                GuideLineSegment(
                    start = MeteringPoint(x = 0f, y = 0f),
                    end = MeteringPoint(x = 1f, y = 1f),
                ),
                GuideLineSegment(
                    start = MeteringPoint(x = 1f, y = 0f),
                    end = MeteringPoint(x = 0f, y = 1f),
                )
            ),
        )
    }
}

private fun buildGoldenSpiralGeometry(
    horizontalMode: Boolean,
): ReferenceGuideGeometry {
    val sampledPoints = sampleGoldenSpiralPoints()
    val horizontalOffset = calculateHorizontalCenterOffset(
        points = sampledPoints,
        additionalOffset = -GOLDEN_SPIRAL_HORIZONTAL_BIAS,
    )
    val focusPoint = resolveGoldenSpiralFocusPoint(horizontalOffset)
    val baseGeometry = ReferenceGuideGeometry(
        lines = buildGoldenSpiralFocusLines(focusPoint),
        spiralPoints = offsetPoints(
            points = sampledPoints,
            xOffset = horizontalOffset,
            yOffset = GOLDEN_SPIRAL_VERTICAL_BIAS,
        ),
    )
    val orientedGeometry = if (horizontalMode) {
        baseGeometry.transpose()
    } else {
        baseGeometry
    }
    val orientedFocusPoint = if (horizontalMode) {
        focusPoint.transpose()
    } else {
        focusPoint
    }

    return orientedGeometry.mirrorSpiralHorizontally(
        pivotX = orientedFocusPoint.x,
    ).rebalanceHorizontally()
}

private fun buildGoldenSpiralFocusLines(
    focusPoint: MeteringPoint,
): List<GuideLineSegment> {
    return listOf(
        GuideLineSegment(
            start = MeteringPoint(x = focusPoint.x, y = 0f),
            end = MeteringPoint(x = focusPoint.x, y = 1f),
        ),
        GuideLineSegment(
            start = MeteringPoint(x = 0f, y = focusPoint.y),
            end = MeteringPoint(x = 1f, y = focusPoint.y),
        ),
    )
}

private fun ReferenceGuideGeometry.transpose(): ReferenceGuideGeometry {
    return ReferenceGuideGeometry(
        lines = lines.map { it.transpose() },
        spiralPoints = spiralPoints.map { it.transpose() },
    )
}

private fun GuideLineSegment.transpose(): GuideLineSegment {
    return GuideLineSegment(
        start = start.transpose(),
        end = end.transpose(),
    )
}

private fun MeteringPoint.transpose(): MeteringPoint {
    return MeteringPoint(
        x = y,
        y = x,
    )
}

private fun ReferenceGuideGeometry.mirrorSpiralHorizontally(
    pivotX: Float,
): ReferenceGuideGeometry {
    return copy(
        spiralPoints = spiralPoints.map { point ->
            point.mirrorHorizontally(pivotX)
        },
    )
}

private fun ReferenceGuideGeometry.rebalanceHorizontally(): ReferenceGuideGeometry {
    val visibleSpiralPoints = spiralPoints.filter { point ->
        point.x in 0f..1f && point.y in 0f..1f
    }
    if (visibleSpiralPoints.size < 2) {
        return this
    }

    val minX = visibleSpiralPoints.minOf { it.x }
    val maxX = visibleSpiralPoints.maxOf { it.x }
    val visibleWidth = maxX - minX
    if (visibleWidth < 0.0001f) {
        return this
    }

    val targetGap = minOf(minX, 1f - maxX)
    val scaleX = (1f - (2f * targetGap)) / visibleWidth

    return transformHorizontally { x ->
        targetGap + ((x - minX) * scaleX)
    }
}

private fun ReferenceGuideGeometry.transformHorizontally(
    transformX: (Float) -> Float,
): ReferenceGuideGeometry {
    return ReferenceGuideGeometry(
        lines = lines.map { segment ->
            GuideLineSegment(
                start = segment.start.transformHorizontally(transformX),
                end = segment.end.transformHorizontally(transformX),
            )
        },
        spiralPoints = spiralPoints.map { point ->
            point.transformHorizontally(transformX)
        },
    )
}

private fun MeteringPoint.mirrorHorizontally(
    pivotX: Float,
): MeteringPoint {
    return MeteringPoint(
        x = (2f * pivotX) - x,
        y = y,
    )
}

private fun MeteringPoint.transformHorizontally(
    transformX: (Float) -> Float,
): MeteringPoint {
    return MeteringPoint(
        x = transformX(x),
        y = y,
    )
}

private fun buildGuideLineSegments(
    fractions: List<Float>,
): List<GuideLineSegment> {
    return buildList {
        fractions.sorted().forEach { fraction ->
            add(
                GuideLineSegment(
                    start = MeteringPoint(x = fraction, y = 0f),
                    end = MeteringPoint(x = fraction, y = 1f),
                )
            )
            add(
                GuideLineSegment(
                    start = MeteringPoint(x = 0f, y = fraction),
                    end = MeteringPoint(x = 1f, y = fraction),
                )
            )
        }
    }
}

internal fun sampleGoldenSpiralPoints(
    pointCount: Int = GOLDEN_SPIRAL_POINT_COUNT,
): List<MeteringPoint> {
    if (pointCount <= 1) {
        return listOf(
            MeteringPoint(
                x = GOLDEN_SECTION_FOCUS,
                y = GOLDEN_SECTION_FOCUS,
            )
        )
    }

    val angleSpan = GOLDEN_SPIRAL_TURNS * 2f * Math.PI.toFloat()
    return List(pointCount) { index ->
        val progress = index / (pointCount - 1).toFloat()
        val outerWeight = 1f - progress
        val tailAngleAdjustment = GOLDEN_SPIRAL_TAIL_ANGLE_ADJUSTMENT *
            outerWeight * outerWeight * outerWeight
        val angle = GOLDEN_SPIRAL_START_ANGLE_RADIANS + angleSpan * progress + tailAngleAdjustment
        val radius = GOLDEN_SPIRAL_MAX_RADIUS * exp(-GOLDEN_SPIRAL_DECAY * angleSpan * progress)

        MeteringPoint(
            x = GOLDEN_SECTION_FOCUS + radius * cos(angle),
            y = GOLDEN_SECTION_FOCUS + radius * sin(angle),
        )
    }
}

internal fun calculateHorizontalCenterOffset(
    points: List<MeteringPoint>,
    additionalOffset: Float = 0f,
): Float {
    if (points.isEmpty()) {
        return additionalOffset
    }

    val minX = points.minOf { it.x }
    val maxX = points.maxOf { it.x }
    return 0.5f - ((minX + maxX) / 2f) + additionalOffset
}

internal fun resolveGoldenSpiralFocusPoint(
    horizontalOffset: Float,
): MeteringPoint {
    return MeteringPoint(
        x = GOLDEN_SECTION_FOCUS + horizontalOffset,
        y = GOLDEN_SECTION_FOCUS + GOLDEN_SPIRAL_VERTICAL_BIAS,
    )
}

private fun offsetPoints(
    points: List<MeteringPoint>,
    xOffset: Float = 0f,
    yOffset: Float = 0f,
): List<MeteringPoint> {
    if (abs(xOffset) < 0.0001f && abs(yOffset) < 0.0001f) {
        return points
    }

    return points.map { point ->
        MeteringPoint(
            x = point.x + xOffset,
            y = point.y + yOffset,
        )
    }
}

private fun ViewfinderAspectRatio.prefersHorizontalGoldenSpiral(): Boolean {
    return when (this) {
        ViewfinderAspectRatio.SIXTEEN_NINE -> true

        else -> false
    }
}

private fun applyZoomRatio(
    camera: Camera,
    zoomRatio: Float,
    context: Context,
    zoomErrorMessage: String,
    isLatestRequest: () -> Boolean,
    onZoomRatioApplied: (Float) -> Unit,
    onZoomRequestFailed: () -> Unit,
    onCameraError: (String) -> Unit,
) {
    val zoomOperation = camera.cameraControl.setZoomRatio(zoomRatio)
    zoomOperation.addListener(
        {
            if (!isLatestRequest()) {
                return@addListener
            }
            runCatching {
                zoomOperation.get()
                val appliedZoomRatio = camera.cameraInfo.zoomState.value?.zoomRatio ?: zoomRatio
                onZoomRatioApplied(appliedZoomRatio)
            }.onFailure { throwable ->
                if (throwable.isZoomRequestCancellation()) {
                    return@onFailure
                }
                onZoomRequestFailed()
                onCameraError(zoomErrorMessage)
            }
        },
        ContextCompat.getMainExecutor(context),
    )
}

private fun Throwable.isZoomRequestCancellation(): Boolean {
    var current: Throwable? = this
    while (current != null) {
        if (current is CancellationException || current.javaClass.simpleName == "OperationCanceledException") {
            return true
        }
        current = current.cause
    }
    return false
}

@Composable
private fun ReferenceGridOverlay(
    type: ReferenceGridType,
    viewfinderAspectRatio: ViewfinderAspectRatio,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val strokeWidth = with(density) { 1.dp.toPx() }
    val lineColor = Color(0xFFB5B5B5).copy(alpha = GUIDE_GRID_ALPHA)
    val geometry = remember(type, viewfinderAspectRatio) {
        buildReferenceGuideGeometry(
            type = type,
            viewfinderAspectRatio = viewfinderAspectRatio,
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        geometry.lines.forEach { segment ->
            drawLine(
                color = lineColor,
                start = Offset(
                    x = size.width * segment.start.x,
                    y = size.height * segment.start.y,
                ),
                end = Offset(
                    x = size.width * segment.end.x,
                    y = size.height * segment.end.y,
                ),
                strokeWidth = strokeWidth,
            )
        }

        if (geometry.spiralPoints.size > 1) {
            val spiralPath = Path().apply {
                val first = geometry.spiralPoints.first()
                moveTo(
                    x = size.width * first.x,
                    y = size.height * first.y,
                )
                geometry.spiralPoints.drop(1).forEach { point ->
                    lineTo(
                        x = size.width * point.x,
                        y = size.height * point.y,
                    )
                }
            }
            drawPath(
                path = spiralPath,
                color = lineColor,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                ),
            )
        }
    }
}

@Composable
private fun MeterReticle(
    meteringPoint: MeteringPoint,
    isAeLocked: Boolean,
    meteringMode: MeteringMode,
) {
    val density = LocalDensity.current
    val frameHalfSize = with(density) { 20.dp.toPx() }
    val cornerLength = with(density) { 9.dp.toPx() }
    val crossHalfLength = with(density) { 5.dp.toPx() }
    val strokeWidth = with(density) { 2.dp.toPx() }
    val contrastStrokeWidth = strokeWidth + with(density) { 2.dp.toPx() }
    val color = if (isAeLocked) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.primary
    }.copy(alpha = RETICLE_ALPHA)
    val contrastColor = Color.Black.copy(alpha = 0.22f)
    val showCornerBrackets = meteringMode != MeteringMode.CENTER_WEIGHTED

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(
            x = size.width * meteringPoint.x,
            y = size.height * meteringPoint.y,
        )
        val left = center.x - frameHalfSize
        val right = center.x + frameHalfSize
        val top = center.y - frameHalfSize
        val bottom = center.y + frameHalfSize

        fun drawReticleLines(lineColor: Color, lineStrokeWidth: Float) {
            if (showCornerBrackets) {
                drawLine(
                    color = lineColor,
                    start = Offset(left, top),
                    end = Offset(left + cornerLength, top),
                    strokeWidth = lineStrokeWidth,
                    cap = StrokeCap.Square,
                )
                drawLine(
                    color = lineColor,
                    start = Offset(left, top),
                    end = Offset(left, top + cornerLength),
                    strokeWidth = lineStrokeWidth,
                    cap = StrokeCap.Square,
                )
                drawLine(
                    color = lineColor,
                    start = Offset(right - cornerLength, top),
                    end = Offset(right, top),
                    strokeWidth = lineStrokeWidth,
                    cap = StrokeCap.Square,
                )
                drawLine(
                    color = lineColor,
                    start = Offset(right, top),
                    end = Offset(right, top + cornerLength),
                    strokeWidth = lineStrokeWidth,
                    cap = StrokeCap.Square,
                )
                drawLine(
                    color = lineColor,
                    start = Offset(left, bottom),
                    end = Offset(left + cornerLength, bottom),
                    strokeWidth = lineStrokeWidth,
                    cap = StrokeCap.Square,
                )
                drawLine(
                    color = lineColor,
                    start = Offset(left, bottom - cornerLength),
                    end = Offset(left, bottom),
                    strokeWidth = lineStrokeWidth,
                    cap = StrokeCap.Square,
                )
                drawLine(
                    color = lineColor,
                    start = Offset(right - cornerLength, bottom),
                    end = Offset(right, bottom),
                    strokeWidth = lineStrokeWidth,
                    cap = StrokeCap.Square,
                )
                drawLine(
                    color = lineColor,
                    start = Offset(right, bottom - cornerLength),
                    end = Offset(right, bottom),
                    strokeWidth = lineStrokeWidth,
                    cap = StrokeCap.Square,
                )
            }
            drawLine(
                color = lineColor,
                start = Offset(center.x - crossHalfLength, center.y),
                end = Offset(center.x + crossHalfLength, center.y),
                strokeWidth = lineStrokeWidth,
                cap = StrokeCap.Square,
            )
            drawLine(
                color = lineColor,
                start = Offset(center.x, center.y - crossHalfLength),
                end = Offset(center.x, center.y + crossHalfLength),
                strokeWidth = lineStrokeWidth,
                cap = StrokeCap.Square,
            )
        }

        drawReticleLines(contrastColor, contrastStrokeWidth)
        drawReticleLines(color, strokeWidth)
    }
}

@Composable
private fun LevelIndicatorOverlay(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val sensorManager = remember(context) {
        context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    }
    var rollDegrees by remember { mutableStateOf(0f) }

    DisposableEffect(sensorManager) {
        val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (sensorManager == null || rotationSensor == null) {
            onDispose { }
        } else {
            val rotationMatrix = FloatArray(9)
            val orientation = FloatArray(3)
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    val rawRoll = Math.toDegrees(orientation[2].toDouble()).toFloat()
                    rollDegrees = rollDegrees * LEVEL_SMOOTHING_FACTOR +
                        rawRoll * (1f - LEVEL_SMOOTHING_FACTOR)
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }
            sensorManager.registerListener(
                listener,
                rotationSensor,
                SensorManager.SENSOR_DELAY_UI,
            )
            onDispose {
                sensorManager.unregisterListener(listener)
            }
        }
    }

    val isLevel = abs(rollDegrees) < LEVEL_THRESHOLD_DEGREES
    val animatedRoll by animateFloatAsState(
        targetValue = rollDegrees,
        animationSpec = tween(
            durationMillis = 100,
            easing = FastOutSlowInEasing,
        ),
        label = "levelRoll",
    )

    val lineColor = if (isLevel) LEVEL_COLOR_ALIGNED else LEVEL_COLOR_DEFAULT
    val strokeWidth = with(density) { LEVEL_STROKE_WIDTH.toPx() }
    val contrastStrokeWidth = strokeWidth + with(density) { 2.dp.toPx() }
    val gapHalf = with(density) { LEVEL_CENTER_GAP_HALF.toPx() }

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val armLength = size.width * LEVEL_ARM_RATIO
        val rollRad = Math.toRadians(animatedRoll.toDouble()).toFloat()
        val dx = kotlin.math.cos(rollRad)
        val dy = kotlin.math.sin(rollRad)

        val gapStartX = center.x + gapHalf * dx
        val gapStartY = center.y + gapHalf * dy
        val gapEndX = center.x - gapHalf * dx
        val gapEndY = center.y - gapHalf * dy

        val leftEnd = Offset(center.x - armLength * dx, center.y - armLength * dy)
        val rightEnd = Offset(center.x + armLength * dx, center.y + armLength * dy)

        val contrastColor = Color.Black.copy(alpha = 0.22f)

        // contrast shadow
        drawLine(
            color = contrastColor,
            start = leftEnd,
            end = Offset(gapEndX, gapEndY),
            strokeWidth = contrastStrokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = contrastColor,
            start = Offset(gapStartX, gapStartY),
            end = rightEnd,
            strokeWidth = contrastStrokeWidth,
            cap = StrokeCap.Round,
        )

        // main line
        drawLine(
            color = lineColor,
            start = leftEnd,
            end = Offset(gapEndX, gapEndY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = lineColor,
            start = Offset(gapStartX, gapStartY),
            end = rightEnd,
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

private const val DEFAULT_ZOOM_RATIO = 1f
private const val ZOOM_UPDATE_EPSILON = 0.01f
private const val GUIDE_GRID_ALPHA = 0.42f
private const val GOLDEN_RATIO = 1.6180339f
private const val GOLDEN_SECTION_FOCUS = 1f / GOLDEN_RATIO
private const val GOLDEN_SPIRAL_MAX_RADIUS = 0.92f
private const val GOLDEN_SPIRAL_TURNS = 2.25f
private const val GOLDEN_SPIRAL_POINT_COUNT = 160
private const val GOLDEN_SPIRAL_HORIZONTAL_BIAS = 0.07f
private const val GOLDEN_SPIRAL_VERTICAL_BIAS = 0.015f
private const val GOLDEN_SPIRAL_TAIL_ANGLE_ADJUSTMENT = -0.06f
private const val GOLDEN_SPIRAL_START_ANGLE_RADIANS = -2.3561945f
private val GOLDEN_SPIRAL_DECAY = (2f * ln(GOLDEN_RATIO) / Math.PI.toFloat())
private const val RETICLE_ALPHA = 0.78f
private const val LEVEL_SMOOTHING_FACTOR = 0.75f
private const val LEVEL_THRESHOLD_DEGREES = 1f
private const val LEVEL_ARM_RATIO = 0.20f
private val LEVEL_COLOR_ALIGNED = Color(0xFF4CAF50).copy(alpha = 0.72f)
private val LEVEL_COLOR_DEFAULT = Color(0xFFB5B5B5).copy(alpha = 0.50f)
private val LEVEL_STROKE_WIDTH = 1.5.dp
private val LEVEL_CENTER_GAP_HALF = 14.dp
