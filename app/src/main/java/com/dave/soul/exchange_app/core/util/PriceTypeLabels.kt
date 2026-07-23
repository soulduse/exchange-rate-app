package com.dave.soul.exchange_app.core.util

import androidx.annotation.StringRes
import com.dave.soul.exchange_app.R

/** 가격 유형 코드 → 라벨 리소스 — 알림 목록·푸시 조립 공용. */
val PRICE_TYPE_LABEL_RES: Map<String, Int> = mapOf(
    "BASE" to R.string.detail_base_price,
    "CASH_BUY" to R.string.detail_cash_buy,
    "CASH_SELL" to R.string.detail_cash_sell,
    "SEND" to R.string.detail_send,
    "RECEIVE" to R.string.detail_receive,
)

@StringRes
fun priceTypeLabelRes(type: String): Int? = PRICE_TYPE_LABEL_RES[type]
