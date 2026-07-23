package com.dave.soul.exchange_app.core.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dave.soul.exchange_app.core.network.RateDto

/** 시세 오프라인 캐시 — 서버 보드 응답 스냅샷. 오프라인/위젯의 단일 진실 원천. */
@Entity(tableName = "rates")
data class RateEntity(
    @PrimaryKey val currencyCode: String,
    val name: String,
    val nameEng: String?,
    val countryCode: String,
    val perUnit: Int,
    val basePrice: Double,
    val change: Double?,
    val changeRatio: Double?,
    val cashBuy: Double?,
    val cashSell: Double?,
    val send: Double?,
    val receive: Double?,
    val high52w: Double?,
    val low52w: Double?,
    val degreeCount: Int?,
    val marketStatus: String?,
    val localTradedAt: String?,
    val fetchedAtMillis: Long,
    // 홈 스파크라인 — 최근 종가 CSV(오름차순). 서버 daily 미적재 시 null
    val spark: String? = null,
) {
    /** 원화 환산 — 1 {currencyCode} = ? KRW (고시 단위 보정). */
    fun krwPerOne(): Double = basePrice / perUnit

    fun sparkValues(): List<Double> =
        spark?.split(",")?.mapNotNull { it.toDoubleOrNull() } ?: emptyList()

    companion object {
        fun from(dto: RateDto, fetchedAtMillis: Long) = RateEntity(
            currencyCode = dto.currencyCode,
            name = dto.name,
            nameEng = dto.nameEng,
            countryCode = dto.countryCode,
            perUnit = dto.perUnit,
            basePrice = dto.basePrice,
            change = dto.change,
            changeRatio = dto.changeRatio,
            cashBuy = dto.cashBuy,
            cashSell = dto.cashSell,
            send = dto.send,
            receive = dto.receive,
            high52w = dto.high52w,
            low52w = dto.low52w,
            degreeCount = dto.degreeCount,
            marketStatus = dto.marketStatus,
            localTradedAt = dto.localTradedAt,
            fetchedAtMillis = fetchedAtMillis,
            spark = dto.spark?.joinToString(","),
        )
    }
}
