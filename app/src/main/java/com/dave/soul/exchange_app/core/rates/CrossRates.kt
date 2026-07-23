package com.dave.soul.exchange_app.core.rates

import com.dave.soul.exchange_app.core.db.RateEntity

/**
 * 표시용 시세 1행 — 기준통화(base) 관점으로 환산된 값.
 *
 * base=KRW 면 서버 고시가 그대로(perUnit 단위 KRW), base≠KRW 면 크로스레이트
 * `value = basePrice(A) × perUnit(B) / basePrice(B)` — perUnit(A) 스케일 유지
 * ("100 JPY = 0.646 USD"). 서버 알림 평가(cross_display_price)와 동일 수식이라
 * 목표가·현재가·표시가가 전부 같은 축이다.
 */
data class DisplayRate(
    val code: String,
    val name: String,
    val nameEng: String?,
    val countryCode: String,
    val perUnit: Int,
    val value: Double,
    /** 절대 등락 — 크로스 모드에서는 무의미한 크기라 null(%만 표시). */
    val change: Double?,
    val changeRatio: Double?,
    val spark: List<Double>,
    /** base≠KRW 일 때 리스트에 추가되는 KRW 가상 행 — 상세 진입 없음. */
    val isVirtualKrw: Boolean = false,
)

object CrossRates {

    /** 원화 환산 — 1 {code} = ? KRW. KRW 는 가상 통화(1.0). 미보유 통화는 null. */
    fun krwPerOne(code: String, byCode: Map<String, RateEntity>): Double? =
        if (code == "KRW") 1.0 else byCode[code]?.krwPerOne()

    /** perUnit 단위 target 의 base 통화 환산가 — baseKrwPerOne 은 krwPerOne(base). */
    fun displayValue(target: RateEntity, baseKrwPerOne: Double): Double =
        target.basePrice / baseKrwPerOne

    /**
     * 크로스 등락률(%) — A/B 각각의 전일 대비 %에서 합성.
     * `((1+cA/100)/(1+cB/100)-1)×100`. 한쪽이라도 null 이면 null(배지 숨김).
     */
    fun crossChangeRatio(targetRatio: Double?, baseRatio: Double?): Double? {
        if (targetRatio == null || baseRatio == null) return null
        return ((1 + targetRatio / 100) / (1 + baseRatio / 100) - 1) * 100
    }

    /**
     * 홈/위젯 표시 리스트 — base 관점으로 환산.
     *
     * base=KRW: 선택 통화 전부 고시가 그대로.
     * base≠KRW: KRW 가상 행을 맨 앞에 추가하고 base 자신은 제외(자기 자신 환산은 항상 1).
     * base 시세가 로컬에 없으면(선택 해제 등) KRW 모드로 폴백한다.
     */
    fun displayRates(
        rates: List<RateEntity>,
        selected: List<String>,
        base: String,
        krwName: String,
    ): List<DisplayRate> {
        val byCode = rates.associateBy { it.currencyCode }
        val baseKrwPerOne = krwPerOne(base, byCode)
            ?: return displayRates(rates, selected, base = "KRW", krwName)

        if (base == "KRW") {
            return selected.mapNotNull { code ->
                val rate = byCode[code] ?: return@mapNotNull null
                DisplayRate(
                    code = rate.currencyCode,
                    name = rate.name,
                    nameEng = rate.nameEng,
                    countryCode = rate.countryCode,
                    perUnit = rate.perUnit,
                    value = rate.basePrice,
                    change = rate.change,
                    changeRatio = rate.changeRatio,
                    spark = rate.sparkValues(),
                )
            }
        }

        val baseRate = byCode.getValue(base)
        val krwRow = DisplayRate(
            code = "KRW",
            name = krwName,
            nameEng = null,
            countryCode = "kr",
            perUnit = 1,
            value = 1.0 / baseKrwPerOne,
            change = null,
            // KRW/base 크로스 = KRW(등락 0%) 대 base — base 등락의 역방향
            changeRatio = crossChangeRatio(0.0, baseRate.changeRatio),
            spark = emptyList(),
            isVirtualKrw = true,
        )
        val crossRows = selected.filter { it != base }.mapNotNull { code ->
            val rate = byCode[code] ?: return@mapNotNull null
            DisplayRate(
                code = rate.currencyCode,
                name = rate.name,
                nameEng = rate.nameEng,
                countryCode = rate.countryCode,
                perUnit = rate.perUnit,
                value = displayValue(rate, baseKrwPerOne),
                change = null,
                changeRatio = crossChangeRatio(rate.changeRatio, baseRate.changeRatio),
                spark = emptyList(),
            )
        }
        return listOf(krwRow) + crossRows
    }
}
