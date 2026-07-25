package com.dave.soul.exchange_app.ui.detail

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dave.soul.exchange_app.R
import com.dave.soul.exchange_app.core.db.RateEntity
import com.dave.soul.exchange_app.core.network.HistoryItemDto
import com.dave.soul.exchange_app.core.prefs.UserPrefs
import com.dave.soul.exchange_app.core.rates.CrossRates
import com.dave.soul.exchange_app.core.repo.AlertRepository
import com.dave.soul.exchange_app.core.repo.RateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChartState(
    val range: String = "3m",
    val items: List<HistoryItemDto> = emptyList(),
    val isLoading: Boolean = false,
)

/**
 * 헤더가 상태 — 기준통화 관점의 표시가.
 * base=KRW 면 고시가 그대로(절대 등락 포함), base≠KRW 면 크로스가(등락은 %만).
 */
data class HeaderState(
    val baseCurrency: String = "KRW",
    val price: Double? = null,
    val change: Double? = null,
    val changeRatio: Double? = null,
) {
    val isCross: Boolean get() = baseCurrency != "KRW"
}

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val rateRepository: RateRepository,
    private val alertRepository: AlertRepository,
    private val prefs: UserPrefs,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    val currencyCode: String = savedStateHandle.get<String>("code").orEmpty()

    val rate: StateFlow<RateEntity?> = rateRepository.rate(currencyCode)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** 기준통화 관점 헤더가 — base 시세 부재·자기 자신 조회 시 KRW 고시로 폴백. */
    val header: StateFlow<HeaderState> = combine(
        rateRepository.rates, prefs.baseCurrency,
    ) { rates, base ->
        val byCode = rates.associateBy { it.currencyCode }
        val target = byCode[currencyCode]
            ?: return@combine HeaderState(baseCurrency = "KRW")
        val baseKrwPerOne = CrossRates.krwPerOne(base, byCode)
        if (base == "KRW" || base == currencyCode || baseKrwPerOne == null) {
            HeaderState(
                baseCurrency = "KRW",
                price = target.basePrice,
                change = target.change,
                changeRatio = target.changeRatio,
            )
        } else {
            HeaderState(
                baseCurrency = base,
                price = CrossRates.displayValue(target, baseKrwPerOne),
                change = null,
                changeRatio = CrossRates.crossChangeRatio(
                    target.changeRatio, byCode.getValue(base).changeRatio,
                ),
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HeaderState())

    private val _chart = MutableStateFlow(ChartState())
    val chart: StateFlow<ChartState> = _chart.asStateFlow()

    val spreadRate: StateFlow<Float> = prefs.spreadRate
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 50f)

    private val _alertResult = MutableStateFlow<String?>(null)
    val alertResult: StateFlow<String?> = _alertResult.asStateFlow()

    init {
        loadHistory("3m")
        // 상세 표시 중 기준통화가 바뀌면 같은 구간을 새 축으로 재구성
        viewModelScope.launch {
            prefs.baseCurrency.drop(1).collect { loadHistory(_chart.value.range) }
        }
    }

    fun loadHistory(range: String) {
        viewModelScope.launch {
            _chart.value = _chart.value.copy(range = range, isLoading = true)
            fetchHistory(range)
                .onSuccess { _chart.value = ChartState(range, it, isLoading = false) }
                .onFailure { _chart.value = _chart.value.copy(isLoading = false) }
        }
    }

    /**
     * 헤더와 같은 판정으로 차트 축 결정 — 크로스면 target·base 히스토리를
     * 병렬로 받아 날짜 키 조인(CrossRates.crossHistory, 표시가와 동일 수식).
     */
    private suspend fun fetchHistory(range: String): Result<List<HistoryItemDto>> {
        val base = prefs.baseCurrency.first()
        val baseRate =
            if (base == "KRW" || base == currencyCode) null
            else rateRepository.rates.first().firstOrNull { it.currencyCode == base }
        if (baseRate == null) {
            return rateRepository.history(currencyCode, range).map { it.items }
        }
        return coroutineScope {
            val targetDeferred = async { rateRepository.history(currencyCode, range) }
            val baseDeferred = async { rateRepository.history(base, range) }
            val target = targetDeferred.await()
                .getOrElse { return@coroutineScope Result.failure(it) }
            val baseHistory = baseDeferred.await()
                .getOrElse { return@coroutineScope Result.failure(it) }
            Result.success(
                CrossRates.crossHistory(target.items, baseHistory.items, baseRate.perUnit)
            )
        }
    }

    fun setSpreadRate(rate: Float) {
        viewModelScope.launch { prefs.setSpreadRate(rate) }
    }

    fun createAlert(priceType: String, direction: String, targetPrice: Double, repeatMode: String) {
        // 헤더가 크로스면 알림도 같은 축으로 — 목표가 입력이 표시가와 동일 스케일
        val base = header.value.baseCurrency
        viewModelScope.launch {
            alertRepository.create(currencyCode, base, priceType, direction, targetPrice, repeatMode)
                .onSuccess { _alertResult.value = context.getString(R.string.detail_alert_registered) }
                .onFailure { _alertResult.value = context.getString(R.string.detail_alert_failed) }
        }
    }

    fun consumeAlertResult() {
        _alertResult.value = null
    }
}
