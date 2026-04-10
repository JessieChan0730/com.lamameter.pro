package com.yourbrand.lumameter.pro.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun HistogramChart(
    histogram: IntArray,
    modifier: Modifier = Modifier,
    barColor: Color = Color.White.copy(alpha = 0.85f),
    backgroundColor: Color = Color.Black.copy(alpha = 0.55f),
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor),
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val binCount = histogram.size
            if (binCount == 0) return@Canvas

            val maxCount = histogram.max()
            if (maxCount <= 0) return@Canvas

            val barWidth = size.width / binCount
            val maxHeight = size.height

            for (i in 0 until binCount) {
                val barHeight = (histogram[i].toFloat() / maxCount) * maxHeight
                if (barHeight < 0.5f) continue

                drawLine(
                    color = barColor,
                    start = Offset(
                        x = barWidth * i + barWidth / 2f,
                        y = maxHeight,
                    ),
                    end = Offset(
                        x = barWidth * i + barWidth / 2f,
                        y = maxHeight - barHeight,
                    ),
                    strokeWidth = barWidth.coerceAtLeast(1f),
                )
            }
        }
    }
}
