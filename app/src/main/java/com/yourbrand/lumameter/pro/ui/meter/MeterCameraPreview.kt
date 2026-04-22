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
import android.hardware.camera2.CameraCharacteristics
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.camera2.interop.Camera2CameraControl
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.CaptureRequestOptions
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
import androidx.compose.ui.graphics.StrokeCap
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
import com.yourbrand.lumameter.pro.domain.exposure.AnalysisTool
import com.yourbrand.lumameter.pro.domain.exposure.FrameExposureMetadata
import com.yourbrand.lumameter.pro.domain.exposure.LuminanceReading
import com.yourbrand.lumameter.pro.domain.exposure.MeteringMode
import com.yourbrand.lumameter.pro.domain.exposure.MeteringPoint
import com.yourbrand.lumameter.pro.domain.exposure.ViewfinderAspectRatio
import com.yourbrand.lumameter.pro.domain.exposure.WhiteBalanceGains
import java.util.concurrent.CancellationException
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.abs

@Composable
fun MeterCameraPreview(
    modifier: Modifier = Modifier,
    analysisTool: AnalysisTool,
    meteringMode: MeteringMode,
    meteringPoint: MeteringPoint,
    hasCustomSpotMeteringPoint: Boolean,
    viewfinderAspectRatio: ViewfinderAspectRatio,
    isAeLocked: Boolean,
    requestedZoomRatio: Float,
    requestedManualFocusSliderPosition: Float,
    enableSpotMetering: Boolean,
    showMeteringReticle: Boolean,
    showGuideGrid: Boolean,
    showLevelIndicator: Boolean,
    onMeteringPointChanged: (MeteringPoint) -> Unit,
    onPreviewTapped: () -> Unit,
    onReadingAvailable: (LuminanceReading) -> Unit,
    onZoomCapabilityResolved: (Float, Float) -> Unit,
    onManualFocusCapabilityResolved: (Boolean, Float) -> Unit,
    onZoomRatioApplied: (Float) -> Unit,
    onCameraError: (String) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraStartErrorMsg = stringResource(R.string.failed_to_start_camera)
    val zoomErrorMsg = stringResource(R.string.failed_to_adjust_zoom)
    val focusErrorMsg = stringResource(R.string.failed_to_adjust_focus)

    val previewView = remember(context) {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    val currentMeteringMode by rememberUpdatedState(meteringMode)
    val currentMeteringPoint by rememberUpdatedState(meteringPoint)
    val currentViewfinderAspectRatio by rememberUpdatedState(viewfinderAspectRatio)
    val currentAnalysisTool by rememberUpdatedState(analysisTool)
    val currentEnableSpotMetering by rememberUpdatedState(enableSpotMetering)
    val currentReadingCallback by rememberUpdatedState(onReadingAvailable)
    val currentRequestedZoomRatio by rememberUpdatedState(requestedZoomRatio)
    val currentRequestedManualFocusSliderPosition by rememberUpdatedState(requestedManualFocusSliderPosition)
    val currentZoomCapabilityCallback by rememberUpdatedState(onZoomCapabilityResolved)
    val currentManualFocusCapabilityCallback by rememberUpdatedState(onManualFocusCapabilityResolved)
    val currentZoomRatioCallback by rememberUpdatedState(onZoomRatioApplied)
    val currentErrorCallback by rememberUpdatedState(onCameraError)
    var boundCamera by remember { mutableStateOf<Camera?>(null) }
    var lastAppliedZoomRatio by remember { mutableStateOf<Float?>(null) }
    var latestZoomRequestToken by remember { mutableStateOf(0) }
    var focusCapability by remember { mutableStateOf(ManualFocusCapability()) }
    var lastAppliedManualFocusSliderPosition by remember { mutableStateOf<Float?>(null) }
    var areManualFocusOptionsActive by remember { mutableStateOf(false) }
    var latestManualFocusRequestToken by remember { mutableStateOf(0) }

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

                val resolvedFocusCapability = resolveManualFocusCapability(camera)
                focusCapability = resolvedFocusCapability
                currentManualFocusCapabilityCallback(
                    resolvedFocusCapability.isSupported,
                    resolvedFocusCapability.minimumFocusDistanceDiopters,
                )

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
                focusCapability = ManualFocusCapability()
                currentManualFocusCapabilityCallback(false, 0f)
                currentErrorCallback(cameraStartErrorMsg)
            }
        }

        providerFuture.addListener(
            bindCameraUseCases,
            ContextCompat.getMainExecutor(context),
        )

        onDispose {
            boundCamera = null
            focusCapability = ManualFocusCapability()
            lastAppliedZoomRatio = null
            latestZoomRequestToken += 1
            lastAppliedManualFocusSliderPosition = null
            areManualFocusOptionsActive = false
            latestManualFocusRequestToken += 1
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

    LaunchedEffect(boundCamera, analysisTool, requestedManualFocusSliderPosition, focusCapability) {
        val camera = boundCamera ?: return@LaunchedEffect
        val isManualFocusEnabled = currentAnalysisTool == AnalysisTool.FOCUS &&
            focusCapability.isSupported

        if (!isManualFocusEnabled) {
            if (!areManualFocusOptionsActive) {
                return@LaunchedEffect
            }

            latestManualFocusRequestToken += 1
            val focusRequestToken = latestManualFocusRequestToken
            clearManualFocus(
                camera = camera,
                context = context,
                focusErrorMessage = focusErrorMsg,
                isLatestRequest = { focusRequestToken == latestManualFocusRequestToken },
                onFocusCleared = {
                    areManualFocusOptionsActive = false
                    lastAppliedManualFocusSliderPosition = null
                },
                onFocusRequestFailed = {
                    if (focusRequestToken == latestManualFocusRequestToken) {
                        areManualFocusOptionsActive = false
                        lastAppliedManualFocusSliderPosition = null
                    }
                },
                onCameraError = currentErrorCallback,
            )
            return@LaunchedEffect
        }

        val safeSliderPosition = currentRequestedManualFocusSliderPosition.coerceIn(0f, 1f)
        if (
            areManualFocusOptionsActive &&
            lastAppliedManualFocusSliderPosition != null &&
            abs(lastAppliedManualFocusSliderPosition!! - safeSliderPosition) < FOCUS_UPDATE_EPSILON
        ) {
            return@LaunchedEffect
        }

        val focusDistanceDiopters = focusCapability.minimumFocusDistanceDiopters * (1f - safeSliderPosition)
        lastAppliedManualFocusSliderPosition = safeSliderPosition
        latestManualFocusRequestToken += 1
        val focusRequestToken = latestManualFocusRequestToken
        applyManualFocus(
            camera = camera,
            focusDistanceDiopters = focusDistanceDiopters,
            context = context,
            focusErrorMessage = focusErrorMsg,
            isLatestRequest = { focusRequestToken == latestManualFocusRequestToken },
            onFocusApplied = {
                areManualFocusOptionsActive = true
            },
            onFocusRequestFailed = {
                if (focusRequestToken == latestManualFocusRequestToken) {
                    lastAppliedManualFocusSliderPosition = null
                    areManualFocusOptionsActive = false
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

        if (showGuideGrid) {
            GuideGridOverlay()
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

private fun resolveManualFocusCapability(camera: Camera): ManualFocusCapability {
    val camera2Info = Camera2CameraInfo.from(camera.cameraInfo)
    val minimumFocusDistanceDiopters = camera2Info
        .getCameraCharacteristic(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE)
        ?.coerceAtLeast(0f)
        ?: 0f
    val availableAfModes = camera2Info
        .getCameraCharacteristic(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES)
        ?: intArrayOf()
    val supportsManualFocus = availableAfModes.contains(CaptureRequest.CONTROL_AF_MODE_OFF) &&
        minimumFocusDistanceDiopters > MANUAL_FOCUS_DISTANCE_EPSILON

    return ManualFocusCapability(
        isSupported = supportsManualFocus,
        minimumFocusDistanceDiopters = minimumFocusDistanceDiopters,
    )
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
                if (throwable.isCameraRequestCancellation()) {
                    return@onFailure
                }
                onZoomRequestFailed()
                onCameraError(zoomErrorMessage)
            }
        },
        ContextCompat.getMainExecutor(context),
    )
}

private fun applyManualFocus(
    camera: Camera,
    focusDistanceDiopters: Float,
    context: Context,
    focusErrorMessage: String,
    isLatestRequest: () -> Boolean,
    onFocusApplied: () -> Unit,
    onFocusRequestFailed: () -> Unit,
    onCameraError: (String) -> Unit,
) {
    val camera2Control = Camera2CameraControl.from(camera.cameraControl)
    val focusOptions = CaptureRequestOptions.Builder()
        .setCaptureRequestOption(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF)
        .setCaptureRequestOption(CaptureRequest.LENS_FOCUS_DISTANCE, focusDistanceDiopters.coerceAtLeast(0f))
        .build()
    val focusOperation = camera2Control.setCaptureRequestOptions(focusOptions)

    focusOperation.addListener(
        {
            if (!isLatestRequest()) {
                return@addListener
            }
            runCatching {
                focusOperation.get()
                onFocusApplied()
            }.onFailure { throwable ->
                if (throwable.isCameraRequestCancellation()) {
                    return@onFailure
                }
                onFocusRequestFailed()
                onCameraError(focusErrorMessage)
            }
        },
        ContextCompat.getMainExecutor(context),
    )
}

private fun clearManualFocus(
    camera: Camera,
    context: Context,
    focusErrorMessage: String,
    isLatestRequest: () -> Boolean,
    onFocusCleared: () -> Unit,
    onFocusRequestFailed: () -> Unit,
    onCameraError: (String) -> Unit,
) {
    val camera2Control = Camera2CameraControl.from(camera.cameraControl)
    val clearOperation = camera2Control.clearCaptureRequestOptions()

    clearOperation.addListener(
        {
            if (!isLatestRequest()) {
                return@addListener
            }
            runCatching {
                clearOperation.get()
                onFocusCleared()
            }.onFailure { throwable ->
                if (throwable.isCameraRequestCancellation()) {
                    return@onFailure
                }
                onFocusRequestFailed()
                onCameraError(focusErrorMessage)
            }
        },
        ContextCompat.getMainExecutor(context),
    )
}

private fun Throwable.isCameraRequestCancellation(): Boolean {
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
private fun GuideGridOverlay(
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val strokeWidth = with(density) { 1.dp.toPx() }
    val lineColor = Color(0xFFB5B5B5).copy(alpha = GUIDE_GRID_ALPHA)

    Canvas(modifier = modifier.fillMaxSize()) {
        val verticalStep = size.width / 3f
        val horizontalStep = size.height / 3f

        drawLine(
            color = lineColor,
            start = Offset(verticalStep, 0f),
            end = Offset(verticalStep, size.height),
            strokeWidth = strokeWidth,
        )
        drawLine(
            color = lineColor,
            start = Offset(verticalStep * 2f, 0f),
            end = Offset(verticalStep * 2f, size.height),
            strokeWidth = strokeWidth,
        )
        drawLine(
            color = lineColor,
            start = Offset(0f, horizontalStep),
            end = Offset(size.width, horizontalStep),
            strokeWidth = strokeWidth,
        )
        drawLine(
            color = lineColor,
            start = Offset(0f, horizontalStep * 2f),
            end = Offset(size.width, horizontalStep * 2f),
            strokeWidth = strokeWidth,
        )
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
private const val FOCUS_UPDATE_EPSILON = 0.01f
private const val MANUAL_FOCUS_DISTANCE_EPSILON = 0.001f
private const val GUIDE_GRID_ALPHA = 0.42f
private const val RETICLE_ALPHA = 0.78f
private const val LEVEL_SMOOTHING_FACTOR = 0.75f
private const val LEVEL_THRESHOLD_DEGREES = 1f
private const val LEVEL_ARM_RATIO = 0.20f
private val LEVEL_COLOR_ALIGNED = Color(0xFF4CAF50).copy(alpha = 0.72f)
private val LEVEL_COLOR_DEFAULT = Color(0xFFB5B5B5).copy(alpha = 0.50f)
private val LEVEL_STROKE_WIDTH = 1.5.dp
private val LEVEL_CENTER_GAP_HALF = 14.dp

private data class ManualFocusCapability(
    val isSupported: Boolean = false,
    val minimumFocusDistanceDiopters: Float = 0f,
)
