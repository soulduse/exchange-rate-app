package com.dave.soul.exchange_app.core.network

import kotlinx.serialization.Serializable

@Serializable
data class BoardResponse(
    val updatedAt: String? = null,
    val rates: List<RateDto> = emptyList(),
)

@Serializable
data class RateDto(
    val currencyCode: String,
    val name: String,
    val nameEng: String? = null,
    val countryCode: String,
    val perUnit: Int = 1,
    val basePrice: Double,
    val change: Double? = null,
    val changeRatio: Double? = null,
    val cashBuy: Double? = null,
    val cashSell: Double? = null,
    val send: Double? = null,
    val receive: Double? = null,
    val high52w: Double? = null,
    val low52w: Double? = null,
    val degreeCount: Int? = null,
    val marketStatus: String? = null,
    val localTradedAt: String? = null,
    val source: String = "NAVER",
    val spark: List<Double>? = null,
)

@Serializable
data class HistoryResponse(
    val currencyCode: String,
    val range: String,
    val items: List<HistoryItemDto> = emptyList(),
)

@Serializable
data class HistoryItemDto(
    val date: String,
    val close: Double,
    val cashBuy: Double? = null,
    val cashSell: Double? = null,
    val send: Double? = null,
    val receive: Double? = null,
)

@Serializable
data class DeviceRegisterRequest(
    val deviceId: String,
    val fcmToken: String,
    val packageName: String,
)

@Serializable
data class DeviceRegisterResponse(val deviceId: String)

@Serializable
data class AlertDto(
    val id: Long,
    val currencyCode: String,
    // 기준통화 — KRW=원화 고시 알림(기존), 그 외=크로스레이트(값 단위가 base 통화)
    val baseCurrency: String = "KRW",
    val priceType: String,
    val direction: String,
    val targetPrice: Double,
    val repeatMode: String,
    val active: Boolean,
    val lastFiredAt: String? = null,
) {
    val isCross: Boolean get() = baseCurrency != "KRW"
}

@Serializable
data class AlertListResponse(val alerts: List<AlertDto> = emptyList())

@Serializable
data class AlertCreateRequest(
    val deviceId: String,
    val currencyCode: String,
    val baseCurrency: String = "KRW",
    val priceType: String,
    val direction: String,
    val targetPrice: Double,
    val repeatMode: String,
)

@Serializable
data class AlertUpdateRequest(
    val deviceId: String,
    val targetPrice: Double? = null,
    val direction: String? = null,
    val priceType: String? = null,
    val repeatMode: String? = null,
    val active: Boolean? = null,
)
