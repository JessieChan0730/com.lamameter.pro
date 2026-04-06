package com.yourbrand.lumameter.pro.ui.meter

import android.content.Context
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.yourbrand.lumameter.pro.R
import com.yourbrand.lumameter.pro.data.camera.LuminanceAnalyzer
import com.yourbrand.lumameter.pro.domain.exposure.LuminanceReading
import com.yourbrand.lumameter.pro.domain.exposure.MeteringMode
import com.yourbrand.lumameter.pro.domain.exposure.MeteringPoint
import java.util.concurrent.Executors
import kotlin.math.abs

@Composable
fun MeterCameraPreview(
    modifier: Modifier = Modifier,
    meteringMode: MeteringMode,
    meteringPoint: MeteringPoint,
    isAeLocked: Boolean,
    requestedZoomRatio: Float,
    onMeteringPointChanged: (MeteringPoint) -> Unit,
    onPreviewTapped: () -> Unit,
    onReadingAvailable: (LuminanceReading) -> Unit,
    onZoomCapabilityResolved: (Float, Float) -> Unit,
    onZoomRatioApplied: (Float) -> Unit,
    onCameraError: (String) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember(context) {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    val currentMeteringMode by rememberUpdatedState(meteringMode)
    val currentMeteringPoint by rememberUpdatedState(meteringPoint)
    val currentReadingCallback by rememberUpdatedState(onReadingAvailable)
    val currentRequestedZoomRatio by rememberUpdatedState(requestedZoomRatio)
    val currentZoomCapabilityCallback by rememberUpdatedState(onZoomCapabilityResolved)
    val currentZoomRatioCallback by rememberUpdatedState(onZoomRatioApplied)
    val currentErrorCallback by rememberUpdatedState(onCameraError)
    var boundCamera by remember { mutableStateOf<Camera?>(null) }
    var lastAppliedZoomRatio by remember { mutableStateOf<Float?>(null) }

    DisposableEffect(lifecycleOwner, previewView) {
        val analyzerExecutor = Executors.newSingleThreadExecutor()
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

                val analysis = ImageAnalysis.Builder()
                    .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                analysis.setAnalyzer(
                    analyzerExecutor,
                    LuminanceAnalyzer(
                        meteringModeProvider = { currentMeteringMode },
                        meteringPointProvider = { currentMeteringPoint },
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
                applyZoomRatio(
                    camera = camera,
                    zoomRatio = safeZoomRatio,
                    context = context,
                    onZoomRatioApplied = currentZoomRatioCallback,
                    onCameraError = currentErrorCallback,
                )
            }.onFailure { _ ->
                boundCamera = null
                currentErrorCallback(context.getString(R.string.failed_to_start_camera))
            }
        }

        providerFuture.addListener(
            bindCameraUseCases,
            ContextCompat.getMainExecutor(context),
        )

        onDispose {
            boundCamera = null
            lastAppliedZoomRatio = null
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
        applyZoomRatio(
            camera = camera,
            zoomRatio = safeZoomRatio,
            context = context,
            onZoomRatioApplied = currentZoomRatioCallback,
            onCameraError = currentErrorCallback,
        )
    }

    Box(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures { tapOffset ->
                val width = size.width.toFloat().coerceAtLeast(1f)
                val height = size.height.toFloat().coerceAtLeast(1f)
                onMeteringPointChanged(
                    MeteringPoint.normalized(
                        x = tapOffset.x / width,
                        y = tapOffset.y / height,
                    )
                )
                onPreviewTapped()
            }
        }
    ) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize(),
        )

        MeterReticle(
            meteringPoint = meteringPoint,
            isAeLocked = isAeLocked,
        )
    }
}

private fun applyZoomRatio(
    camera: Camera,
    zoomRatio: Float,
    context: Context,
    onZoomRatioApplied: (Float) -> Unit,
    onCameraError: (String) -> Unit,
) {
    val zoomOperation = camera.cameraControl.setZoomRatio(zoomRatio)
    zoomOperation.addListener(
        {
            runCatching {
                zoomOperation.get()
                val appliedZoomRatio = camera.cameraInfo.zoomState.value?.zoomRatio ?: zoomRatio
                onZoomRatioApplied(appliedZoomRatio)
            }.onFailure {
                onCameraError(context.getString(R.string.failed_to_adjust_zoom))
            }
        },
        ContextCompat.getMainExecutor(context),
    )
}

@Composable
private fun MeterReticle(
    meteringPoint: MeteringPoint,
    isAeLocked: Boolean,
) {
    val density = LocalDensity.current
    val outerRadius = with(density) { 30.dp.toPx() }
    val innerRadius = with(density) { 10.dp.toPx() }
    val strokeWidth = with(density) { 2.dp.toPx() }
    val guideLength = with(density) { 18.dp.toPx() }
    val color = if (isAeLocked) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.primary
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(
            x = size.width * meteringPoint.x,
            y = size.height * meteringPoint.y,
        )

        drawCircle(
            color = color.copy(alpha = 0.22f),
            radius = outerRadius,
            center = center,
            style = Stroke(width = strokeWidth),
        )
        drawCircle(
            color = color,
            radius = innerRadius,
            center = center,
            style = Stroke(width = strokeWidth),
        )
        drawLine(
            color = color,
            start = Offset(center.x - guideLength, center.y),
            end = Offset(center.x - innerRadius, center.y),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(center.x + innerRadius, center.y),
            end = Offset(center.x + guideLength, center.y),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(center.x, center.y - guideLength),
            end = Offset(center.x, center.y - innerRadius),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(center.x, center.y + innerRadius),
            end = Offset(center.x, center.y + guideLength),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

private const val DEFAULT_ZOOM_RATIO = 1f
private const val ZOOM_UPDATE_EPSILON = 0.01f
