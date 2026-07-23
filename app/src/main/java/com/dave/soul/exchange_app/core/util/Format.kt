package com.dave.soul.exchange_app.core.util

import java.text.DecimalFormat

private val priceFormat = DecimalFormat("#,##0.00")
private val amountFormat = DecimalFormat("#,##0.##")

/** 시세 표시 — 1,480.60 */
fun formatPrice(value: Double): String = priceFormat.format(value)

/** 계산 금액 표시 — 1,234.56 (불필요한 소수 생략) */
fun formatAmount(value: Double): String = amountFormat.format(value)

/** 등락 부호 포함 — +1.90 / -1.90 */
fun formatSigned(value: Double): String =
    (if (value > 0) "+" else "") + priceFormat.format(value)

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
