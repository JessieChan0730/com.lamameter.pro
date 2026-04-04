package com.yourbrand.lumameter.pro.ui.meter

import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.yourbrand.lumameter.pro.R
import com.yourbrand.lumameter.pro.data.camera.LuminanceAnalyzer
import com.yourbrand.lumameter.pro.domain.exposure.LuminanceReading
import com.yourbrand.lumameter.pro.domain.exposure.MeteringMode
import com.yourbrand.lumameter.pro.domain.exposure.MeteringPoint
import com.yourbrand.lumameter.pro.ui.theme.MeterAmberLight
import com.yourbrand.lumameter.pro.ui.theme.MeterGreenLight
import java.util.concurrent.Executors

@Composable
fun MeterCameraPreview(
    modifier: Modifier = Modifier,
    meteringMode: MeteringMode,
    meteringPoint: MeteringPoint,
    isAeLocked: Boolean,
    onMeteringPointChanged: (MeteringPoint) -> Unit,
    onReadingAvailable: (LuminanceReading) -> Unit,
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
    val currentErrorCallback by rememberUpdatedState(onCameraError)

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
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis,
                )
            }.onFailure { _ ->
                currentErrorCallback(context.getString(R.string.failed_to_start_camera))
            }
        }

        providerFuture.addListener(
            bindCameraUseCases,
            ContextCompat.getMainExecutor(context),
        )

        onDispose {
            cameraProvider?.unbindAll()
            analyzerExecutor.shutdown()
        }
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
    val color = if (isAeLocked) MeterAmberLight else MeterGreenLight

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
