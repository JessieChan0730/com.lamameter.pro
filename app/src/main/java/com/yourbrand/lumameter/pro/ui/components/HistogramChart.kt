package com.yourbrand.lumameter.pro.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

@Composable
fun HistogramChart(
    histogram: IntArray,
    modifier: Modifier = Modifier,
    barColor: Color? = null,
    backgroundColor: Color? = null,
    baselineColor: Color? = null,
) {
    val resolvedBarColor = barColor ?: MaterialTheme.colorScheme.primary.copy(alpha = 0.88f)
    val resolvedBackgroundColor = backgroundColor ?: Color.Black.copy(alpha = 0.42f)
    val resolvedBaselineColor = baselineColor ?: Color.White.copy(alpha = 0.22f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(resolvedBackgroundColor),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (histogram.isEmpty()) {
                return@Canvas
            }

            val maxCount = histogram.maxOrNull()?.coerceAtLeast(1) ?: 1
            val barWidth = size.width / histogram.size.coerceAtLeast(1)

            drawLine(
                color = resolvedBaselineColor,
                start = Offset(x = 0f, y = size.height),
                end = Offset(x = size.width, y = size.height),
                strokeWidth = 1.dp.toPx(),
                cap = StrokeCap.Square,
            )

            histogram.forEachIndexed { index, value ->
                if (value <= 0) {
                    return@forEachIndexed
                }

                val barHeight = size.height * (value.toFloat() / maxCount.toFloat())
                val x = barWidth * index + barWidth / 2f
                drawLine(
                    color = resolvedBarColor,
                    start = Offset(x = x, y = size.height),
                    end = Offset(x = x, y = size.height - barHeight),
                    strokeWidth = barWidth.coerceAtLeast(1f),
                    cap = StrokeCap.Square,
                )
            }
        }
    }
}
