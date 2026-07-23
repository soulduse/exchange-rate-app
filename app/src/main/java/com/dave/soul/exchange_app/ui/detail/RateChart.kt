package com.dave.soul.exchange_app.ui.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/** 의존성 없는 라인 차트 — 종가 시계열 + 최저/최고 밴드 음영. */
@Composable
fun RateChart(
    closes: List<Double>,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
    ) {
        if (closes.size < 2) return@Canvas
        val min = closes.min()
        val max = closes.max()
        val span = (max - min).takeIf { it > 0 } ?: 1.0
        val stepX = size.width / (closes.size - 1)

        fun pointAt(index: Int): Offset {
            val x = index * stepX
            val y = size.height * (1f - ((closes[index] - min) / span).toFloat()) * 0.92f +
                size.height * 0.04f
            return Offset(x, y)
        }

        val linePath = Path().apply {
            moveTo(pointAt(0).x, pointAt(0).y)
            for (i in 1 until closes.size) lineTo(pointAt(i).x, pointAt(i).y)
        }
        val fillPath = Path().apply {
            addPath(linePath)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.25f), lineColor.copy(alpha = 0f)),
            ),
        )
        drawPath(path = linePath, color = lineColor, style = Stroke(width = 4f))
    }
}
