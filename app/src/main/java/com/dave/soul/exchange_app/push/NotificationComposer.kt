package com.dave.soul.exchange_app.push

import android.content.Context
import com.dave.soul.exchange_app.R
import com.dave.soul.exchange_app.core.util.displayName
import com.dave.soul.exchange_app.core.util.formatPrice
import com.dave.soul.exchange_app.core.util.formatRate
import com.dave.soul.exchange_app.core.util.formatSigned
import com.dave.soul.exchange_app.core.util.priceTypeLabelRes

/**
 * data-only 푸시의 기기 언어 조립 — 서버는 조립 필드만 보내고 문자열은 여기서 만든다.
 * 필드가 모자라면 null 을 돌려 서버 브리지(data.title/body, 한국어) 폴백에 맡긴다.
 */
object NotificationComposer {

    fun compose(context: Context, data: Map<String, String>): Pair<String, String>? =
        when (data["type"]) {
            "EXCHANGE_RATE_ALERT" -> composeAlert(context, data)
            "EXCHANGE_SWING" -> composeSwing(context, data)
            // 브리핑은 서버 조립 문자열(언어별 토픽) — 브리지 그대로 사용
            else -> null
        }

    private fun composeAlert(context: Context, data: Map<String, String>): Pair<String, String>? {
        val code = data["currencyCode"] ?: return null
        val current = data["currentPrice"]?.toDoubleOrNull() ?: return null
        val target = data["targetPrice"]?.toDoubleOrNull() ?: return null
        val labelRes = priceTypeLabelRes(data["priceType"] ?: "") ?: return null
        val directionRes = when (data["direction"]) {
            "ABOVE" -> R.string.alerts_direction_above
            "BELOW" -> R.string.alerts_direction_below
            else -> return null
        }
        val name = displayName(data["currencyName"] ?: code, null, code)
        val perUnit = data["perUnit"]?.toIntOrNull() ?: 1
        val unitText =
            if (perUnit != 1) context.getString(R.string.notif_alert_unit, perUnit, code) else ""
        val base = data["baseCurrency"] ?: "KRW"
        val body = if (base != "KRW") {
            // 크로스 알림 — 값 단위가 base 통화(서버 평가와 동일 스케일), 유형은 BASE 고정
            context.getString(
                R.string.notif_alert_body_cross,
                name,
                unitText,
                formatRate(current),
                base,
                formatRate(target),
                context.getString(directionRes),
            )
        } else {
            context.getString(
                R.string.notif_alert_body,
                name,
                unitText,
                context.getString(labelRes),
                formatPrice(current),
                formatPrice(target),
                context.getString(directionRes),
            )
        }
        return context.getString(R.string.app_name) to body
    }

    private fun composeSwing(context: Context, data: Map<String, String>): Pair<String, String>? {
        val code = data["currencyCode"] ?: return null
        val base = data["basePrice"]?.toDoubleOrNull() ?: return null
        val ratio = data["changeRatio"]?.toDoubleOrNull() ?: return null
        val name = displayName(data["currencyName"] ?: code, null, code)
        val bodyRes =
            if (ratio > 0) R.string.notif_swing_body_up else R.string.notif_swing_body_down
        val body = context.getString(bodyRes, name, formatPrice(base), formatSigned(ratio))
        return context.getString(R.string.notif_swing_title) to body
    }
}
