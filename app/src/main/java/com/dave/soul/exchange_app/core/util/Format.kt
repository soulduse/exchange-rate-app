package com.dave.soul.exchange_app.core.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Currency
import java.util.Locale

// 로케일별 구분자(1.234,56 vs 1,234.56) 반영 — 로케일 전환 대응 + DecimalFormat 비스레드세이프
// 회피를 위해 호출마다 생성한다(생성 비용은 μs 단위라 리스트 렌더에도 무시 가능).
private fun priceFormat() = DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.getDefault()))
private fun amountFormat() = DecimalFormat("#,##0.##", DecimalFormatSymbols.getInstance(Locale.getDefault()))

/** 시세 표시 — 1,480.60 */
fun formatPrice(value: Double): String = priceFormat().format(value)

/** 계산 금액 표시 — 1,234.56 (불필요한 소수 생략) */
fun formatAmount(value: Double): String = amountFormat().format(value)

/** 등락 부호 포함 — +1.90 / -1.90 */
fun formatSigned(value: Double): String =
    (if (value > 0) "+" else "") + priceFormat().format(value)

/**
 * 기준 시각 축약 — "2026-07-23T20:22:10" → "07.23 20:22".
 * ISO-8601 유사 문자열을 인덱스로 파싱(포맷터 미의존). 형식이 맞지 않으면 null.
 */
fun shortTradedAt(localTradedAt: String?): String? {
    val s = localTradedAt ?: return null
    // "yyyy-MM-ddTHH:mm" 최소 길이(16) 및 구분자 위치 검증
    if (s.length < 16 || s[4] != '-' || s[7] != '-' || (s[10] != 'T' && s[10] != ' ')) return null
    val month = s.substring(5, 7)
    val day = s.substring(8, 10)
    val time = s.substring(11, 16)
    return "$month.$day $time"
}

/** 국가코드 → 국기 이모지 (regional indicator). "eu" 등도 렌더링됨. */
fun flagEmoji(countryCode: String): String {
    if (countryCode.length != 2) return "🏳️"
    return countryCode.uppercase().map { ch ->
        String(Character.toChars(0x1F1E6 + (ch - 'A')))
    }.joinToString("")
}

/** "미국 USD" → "미국" (통화코드 중복 표기 제거) */
fun displayName(name: String, currencyCode: String): String =
    name.removeSuffix(currencyCode).trim().ifEmpty { name }

/**
 * 통화 표시명 — 한국어 로케일은 서버 한국어명, 그 외 로케일은
 * 서버 영문명 → java.util.Currency 로케일 통화명(전 언어 현지화) → 한국어명 순 폴백.
 */
fun displayName(name: String, nameEng: String?, currencyCode: String): String {
    if (Locale.getDefault().language == "ko") return displayName(name, currencyCode)
    if (!nameEng.isNullOrBlank()) return nameEng
    val localized = runCatching {
        Currency.getInstance(currencyCode).getDisplayName(Locale.getDefault())
    }.getOrNull()
    // getDisplayName 은 미지원 로케일에서 코드 자체를 돌려준다 — 그 경우 한국어명 폴백
    return if (localized != null && !localized.equals(currencyCode, ignoreCase = true)) localized
    else displayName(name, currencyCode)
}
