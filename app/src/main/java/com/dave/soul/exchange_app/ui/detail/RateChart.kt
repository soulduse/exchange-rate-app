package com.dave.soul.exchange_app.ui.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dave.soul.exchange_app.core.util.formatPrice

/**
 * 의존성 없는 라인 차트 — 종가 시계열 + 그라데이션 음영 + x/y축 눈금.
 * y축: 최고/중간/최저 그리드라인+가격 라벨, x축: 시작/중간/끝 날짜 라벨.
 * 드래그하면 가장 가까운 지점에 십자선(크로스헤어)과 가격·날짜 라벨을 표시한다.
 */
@Composable
fun RateChart(
    closes: List<Double>,
    dates: List<String> = emptyList(),
    lineColor: Color = MaterialTheme.colorScheme.primary,
    axisColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier: Modifier = Modifier,
) {
    var touchX by remember(closes) { mutableStateOf<Float?>(null) }
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
    val axisStyle = TextStyle(fontSize = 10.sp, color = axisColor)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .pointerInput(closes) {
                detectDragGestures(
                    onDragStart = { offset -> touchX = offset.x },
                    onDrag = { change, _ ->
                        change.consume()
                        touchX = change.position.x
                    },
                    onDragEnd = { touchX = null },
                    onDragCancel = { touchX = null },
                )
            },
    ) {
        if (closes.size < 2) return@Canvas
        val min = closes.min()
        val max = closes.max()
        val span = (max - min).takeIf { it > 0 } ?: 1.0

        // x축 날짜 라벨 영역을 하단에 확보
        val xLabelH = if (dates.isNotEmpty()) 40f else 0f
        val chartH = size.height - xLabelH
        val stepX = size.width / (closes.size - 1)

        fun pointAt(index: Int): Offset {
            val x = index * stepX
            val y = chartH * (1f - ((closes[index] - min) / span).toFloat()) * 0.90f +
                chartH * 0.06f
            return Offset(x, y)
        }

        fun yFor(value: Double): Float =
            chartH * (1f - ((value - min) / span).toFloat()) * 0.90f + chartH * 0.06f

        // ── y축: 최고/중간/최저 그리드라인 + 가격 라벨 ──
        val gridDash = PathEffect.dashPathEffect(floatArrayOf(6f, 8f))
        val gridColor = axisColor.copy(alpha = 0.25f)
        listOf(max, (max + min) / 2, min).forEach { value ->
            val gy = yFor(value)
            drawLine(
                color = gridColor,
                start = Offset(0f, gy),
                end = Offset(size.width, gy),
                strokeWidth = 1.5f,
                pathEffect = gridDash,
            )
            val text = textMeasurer.measure(formatPrice(value), axisStyle)
            drawText(
                text,
                topLeft = Offset(size.width - text.size.width - 6f, gy - text.size.height - 3f),
            )
        }

        // ── x축: 시작/중간/끝 날짜 라벨 ──
        fun shortDate(raw: String): String =
            raw.split("-").takeIf { it.size == 3 }
                ?.let { (y, m, d) -> "${y.takeLast(2)}.${m.toInt()}.${d.toInt()}" } ?: raw
        if (dates.isNotEmpty()) {
            val indices = listOf(0, (dates.size - 1) / 2, dates.size - 1).distinct()
            indices.forEach { i ->
                val text = textMeasurer.measure(shortDate(dates[i]), axisStyle)
                val tx = (i * stepX - text.size.width / 2f)
                    .coerceIn(0f, size.width - text.size.width)
                drawText(text, topLeft = Offset(tx, chartH + 8f))
            }
        }

        val linePath = Path().apply {
            moveTo(pointAt(0).x, pointAt(0).y)
            for (i in 1 until closes.size) lineTo(pointAt(i).x, pointAt(i).y)
        }
        val fillPath = Path().apply {
            addPath(linePath)
            lineTo(size.width, chartH)
            lineTo(0f, chartH)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.25f), lineColor.copy(alpha = 0f)),
                endY = chartH,
            ),
        )
        drawPath(path = linePath, color = lineColor, style = Stroke(width = 4f))

        // ── 크로스헤어: 드래그 x → 가장 가까운 데이터 지점에 스냅 ──
        val x = touchX ?: return@Canvas
        val index = (x / stepX).toInt().coerceIn(0, closes.size - 1)
        val snapped = pointAt(index)
        val dash = PathEffect.dashPathEffect(floatArrayOf(12f, 10f))
        val crossColor = lineColor.copy(alpha = 0.8f)
        drawLine(
            color = crossColor,
            start = Offset(snapped.x, 0f),
            end = Offset(snapped.x, chartH),
            strokeWidth = 2.5f,
            pathEffect = dash,
        )
        drawLine(
            color = crossColor,
            start = Offset(0f, snapped.y),
            end = Offset(size.width, snapped.y),
            strokeWidth = 2.5f,
            pathEffect = dash,
        )
        drawCircle(color = Color.White, radius = 14f, center = snapped)
        drawCircle(color = lineColor, radius = 9f, center = snapped)

        // 라벨: 가격 + 날짜. 터치 지점 반대편으로 플립해 화면 밖으로 안 나가게.
        val dateText = dates.getOrNull(index)?.let { " · ${shortDate(it)}" }.orEmpty()
        val label = textMeasurer.measure(formatPrice(closes[index]) + dateText, labelStyle)
        val padH = 20f
        val padV = 12f
        val boxW = label.size.width + padH * 2
        val boxH = label.size.height + padV * 2
        val boxX = if (snapped.x + 24f + boxW <= size.width) snapped.x + 24f else snapped.x - 24f - boxW
        val boxY = (snapped.y - boxH - 24f).coerceAtLeast(0f)
        drawRoundRect(
            color = lineColor,
            topLeft = Offset(boxX, boxY),
            size = Size(boxW, boxH),
            cornerRadius = CornerRadius(16f, 16f),
        )
        drawText(label, topLeft = Offset(boxX + padH, boxY + padV))
    }
}
