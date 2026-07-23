package com.dave.soul.exchange_app.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap

/** 홈 행용 미니 추이 차트 — 최근 종가 시계열. 2포인트 미만이면 그리지 않음. */
@Composable
fun Sparkline(
    values: List<Double>,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        if (values.size < 2) return@Canvas
        val min = values.min()
        val max = values.max()
        val span = (max - min).takeIf { it > 0 } ?: 1.0
        val stepX = size.width / (values.size - 1)
        val path = Path()
        values.forEachIndexed { index, value ->
            val x = index * stepX
            val y = size.height * (1f - ((value - min) / span).toFloat()) * 0.84f +
                size.height * 0.08f
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path = path, color = color, style = Stroke(width = 3f, cap = StrokeCap.Round))
        // 마지막 값 강조점
        val lastY = size.height * (1f - ((values.last() - min) / span).toFloat()) * 0.84f +
            size.height * 0.08f
        drawCircle(color = color, radius = 4f, center = Offset(size.width, lastY))
    }
}
