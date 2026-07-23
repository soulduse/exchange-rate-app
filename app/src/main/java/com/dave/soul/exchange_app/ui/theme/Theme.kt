package com.dave.soul.exchange_app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

// 상승/하락 — 국내 금융 관례(상승 빨강, 하락 파랑)
val RiseRed = Color(0xFFE5484D)
val FallBlue = Color(0xFF3B6EF6)
val RiseRedContainer = Color(0xFFFDECEC)
val FallBlueContainer = Color(0xFFEAF0FE)
val RiseRedContainerDark = Color(0xFF3A2224)
val FallBlueContainerDark = Color(0xFF1D2740)

private val LightColors = lightColorScheme(
    primary = Color(0xFF2E5BFF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3EAFF),
    onPrimaryContainer = Color(0xFF102A66),
    secondary = Color(0xFF5A6478),
    secondaryContainer = Color(0xFFE8ECF4),
    background = Color(0xFFF4F6FA),
    onBackground = Color(0xFF171B24),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF171B24),
    surfaceVariant = Color(0xFFEDF0F6),
    onSurfaceVariant = Color(0xFF6B7385),
    outline = Color(0xFFD8DDE7),
    outlineVariant = Color(0xFFE7EAF1),
    error = Color(0xFFD93438),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8AA6FF),
    onPrimary = Color(0xFF0E1B3E),
    primaryContainer = Color(0xFF25335E),
    onPrimaryContainer = Color(0xFFDCE4FF),
    secondary = Color(0xFF9AA3B8),
    secondaryContainer = Color(0xFF262B36),
    background = Color(0xFF0F1116),
    onBackground = Color(0xFFE7EAF1),
    surface = Color(0xFF181B22),
    onSurface = Color(0xFFE7EAF1),
    surfaceVariant = Color(0xFF222633),
    onSurfaceVariant = Color(0xFF98A0B3),
    outline = Color(0xFF3A4050),
    outlineVariant = Color(0xFF2A2F3C),
    error = Color(0xFFFF6B6E),
)

private val AppTypography = Typography().let { base ->
    base.copy(
        titleLarge = base.titleLarge.copy(fontWeight = FontWeight.Bold),
        titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        displaySmall = base.displaySmall.copy(fontWeight = FontWeight.Bold),
    )
}

/** THEME_MODE 설정값 → 다크 여부. SYSTEM 은 시스템 설정 추종. */
@Composable
fun resolveDarkTheme(themeMode: String): Boolean = when (themeMode) {
    "LIGHT" -> false
    "DARK" -> true
    else -> isSystemInDarkTheme()
}

@Composable
fun ExchangeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}
