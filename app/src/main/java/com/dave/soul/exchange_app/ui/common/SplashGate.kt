package com.dave.soul.exchange_app.ui.common

import android.app.Activity
import android.os.SystemClock
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dave.soul.exchange_app.R
import com.dave.soul.exchange_app.ads.AdsManager
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlinx.coroutines.delay

/**
 * 스플래시 → 전면광고 → 메인 게이트.
 * 최소 노출 1.9s + 전면 프리로드 폴링(최대 6s) + failsafe(10s). 첫 세션은 광고 면제.
 * [adExempt]는 launchCount 비동기 로드 전 null — 짧게 대기 후 판정한다.
 *
 * 연출(자산 0, 전부 코드 드로잉): 배경 차트 라인이 좌→우로 그려지고,
 * ₩ 순환 화살표 아크가 스윕 인 후 계속 회전, 궤도 점이 공전, 앱명 페이드업.
 */
@Composable
fun SplashGate(
    adsManager: AdsManager,
    adExempt: Boolean?,
    onFinished: () -> Unit,
) {
    val activity = LocalContext.current as? Activity
    val exemptState = rememberUpdatedState(adExempt)

    LaunchedEffect(Unit) {
        val start = SystemClock.elapsedRealtime()
        fun elapsed() = SystemClock.elapsedRealtime() - start
        while (exemptState.value == null && elapsed() < EXEMPT_WAIT_MILLIS) delay(50)
        val exempt = exemptState.value ?: true
        Log.i("SplashGate", "exempt=$exempt raw=${exemptState.value} elapsed=${elapsed()}")
        if (!exempt) {
            while (!adsManager.isInterstitialReady && elapsed() < AD_WAIT_MILLIS) delay(150)
        }
        val remain = MIN_EXPOSURE_MILLIS - elapsed()
        if (remain > 0) delay(remain)
        Log.i("SplashGate", "decision: ready=${adsManager.isInterstitialReady} elapsed=${elapsed()}")
        val host = activity
        if (exempt || host == null || !adsManager.isInterstitialReady) {
            onFinished()
        } else {
            var done = false
            Log.i("SplashGate", "showing launch interstitial")
            adsManager.showLaunchInterstitial(host) {
                done = true
                onFinished()
            }
            // failsafe — 콜백이 오지 않는 극단 상황에서도 앱 진입 보장
            delay(FAILSAFE_MILLIS)
            if (!done) onFinished()
        }
    }

    // ── 인트로 진행(아크 스윕·₩ 팝·차트 라인·앱명)과 상시 회전/펄스 ──
    val intro = remember { Animatable(0f) }
    val glyphScale = remember { Animatable(0.4f) }
    val chartProgress = remember { Animatable(0f) }
    val nameAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        chartProgress.animateTo(1f, tween(durationMillis = 1500, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        intro.animateTo(1f, tween(durationMillis = 900, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        delay(250)
        glyphScale.animateTo(
            1f,
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        )
    }
    LaunchedEffect(Unit) {
        delay(550)
        nameAlpha.animateTo(1f, tween(durationMillis = 600))
    }
    val infinite = rememberInfiniteTransition(label = "splashLoop")
    val rotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 2600, easing = LinearEasing)),
        label = "rotation",
    )
    val pulse by infinite.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(listOf(Color(0xFF3D6BFF), Color(0xFF2A50E0), Color(0xFF1E3FBF))),
            ),
    ) {
        // 배경: 좌→우로 그려지는 환율 차트 라인
        Canvas(modifier = Modifier.fillMaxSize()) {
            val pts = listOf(
                0.00f to 0.78f, 0.09f to 0.72f, 0.18f to 0.75f, 0.28f to 0.66f,
                0.38f to 0.70f, 0.50f to 0.60f, 0.62f to 0.64f, 0.74f to 0.54f,
                0.86f to 0.58f, 1.00f to 0.46f,
            )
            val visible = (pts.size - 1) * chartProgress.value
            val whole = visible.toInt().coerceAtMost(pts.size - 2)
            val path = Path()
            fun at(i: Int) = Offset(pts[i].first * size.width, pts[i].second * size.height)
            path.moveTo(at(0).x, at(0).y)
            for (i in 1..whole) path.lineTo(at(i).x, at(i).y)
            val frac = visible - whole
            if (frac > 0f && whole + 1 <= pts.size - 1) {
                val a = at(whole)
                val b = at(whole + 1)
                path.lineTo(a.x + (b.x - a.x) * frac, a.y + (b.y - a.y) * frac)
            }
            drawPath(
                path = path,
                color = Color.White.copy(alpha = 0.18f),
                style = Stroke(width = 10f, cap = StrokeCap.Round),
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center),
        ) {
            // 브랜드 마크: ₩ + 순환 화살표 아크 (회전·펄스·궤도 점)
            Canvas(modifier = Modifier.size(170.dp)) {
                val radius = min(size.width, size.height) * 0.36f * pulse
                val stroke = radius * 0.22f
                val center = Offset(size.width / 2f, size.height / 2f)
                val sweep = 128f * intro.value
                val arcTopLeft = Offset(center.x - radius, center.y - radius)
                val arcSize = Size(radius * 2f, radius * 2f)
                rotate(rotation, pivot = center) {
                    listOf(-24f, 156f).forEach { startAngle ->
                        drawArc(
                            color = Color.White,
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = arcTopLeft,
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Round),
                        )
                        // 아크 끝 화살촉
                        val endAngle = (startAngle + sweep) * (Math.PI / 180.0)
                        val tip = Offset(
                            center.x + radius * cos(endAngle).toFloat(),
                            center.y + radius * sin(endAngle).toFloat(),
                        )
                        drawCircle(color = Color.White, radius = stroke * 0.72f, center = tip)
                    }
                }
                // 궤도 점 — 본 회전보다 빠르게 역방향 공전
                val orbitAngle = (-rotation * 1.8f - 60f) * (Math.PI / 180.0).toFloat()
                val orbitR = radius * 1.42f
                drawCircle(
                    color = Color.White.copy(alpha = 0.85f),
                    radius = stroke * 0.4f,
                    center = Offset(
                        center.x + orbitR * cos(orbitAngle),
                        center.y + orbitR * sin(orbitAngle),
                    ),
                )
                // 중앙 ₩ 글리프 — 오버슈트 팝
                val glyph = textMeasurer.measure(
                    "₩",
                    TextStyle(
                        fontSize = (46 * glyphScale.value).sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                    ),
                )
                drawText(
                    glyph,
                    topLeft = Offset(
                        center.x - glyph.size.width / 2f,
                        center.y - glyph.size.height / 2f,
                    ),
                )
            }
            Spacer(Modifier.height(18.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .graphicsLayer { alpha = nameAlpha.value }
                    .offset(y = ((1f - nameAlpha.value) * 14).dp),
            )
        }
    }
}

private const val MIN_EXPOSURE_MILLIS = 1_900L
private const val EXEMPT_WAIT_MILLIS = 2_000L
private const val AD_WAIT_MILLIS = 6_000L
private const val FAILSAFE_MILLIS = 10_000L
