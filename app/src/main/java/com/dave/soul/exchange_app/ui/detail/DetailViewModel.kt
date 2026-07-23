package com.dave.soul.exchange_app.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dave.soul.exchange_app.core.db.RateEntity
import com.dave.soul.exchange_app.core.network.HistoryItemDto
import com.dave.soul.exchange_app.core.prefs.UserPrefs
import com.dave.soul.exchange_app.core.repo.AlertRepository
import com.dave.soul.exchange_app.core.repo.RateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChartState(
    val range: String = "3m",
    val items: List<HistoryItemDto> = emptyList(),
    val isLoading: Boolean = false,
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val rateRepository: RateRepository,
    private val alertRepository: AlertRepository,
    private val prefs: UserPrefs,
) : ViewModel() {

    val currencyCode: String = savedStateHandle.get<String>("code").orEmpty()

    val rate: StateFlow<RateEntity?> = rateRepository.rate(currencyCode)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _chart = MutableStateFlow(ChartState())
    val chart: StateFlow<ChartState> = _chart.asStateFlow()

    val spreadRate: StateFlow<Float> = prefs.spreadRate
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 50f)

    private val _alertResult = MutableStateFlow<String?>(null)
    val alertResult: StateFlow<String?> = _alertResult.asStateFlow()

    init {
        loadHistory("3m")
    }

    fun loadHistory(range: String) {
        viewModelScope.launch {
            _chart.value = _chart.value.copy(range = range, isLoading = true)
            rateRepository.history(currencyCode, range)
                .onSuccess { _chart.value = ChartState(range, it.items, isLoading = false) }
                .onFailure { _chart.value = _chart.value.copy(isLoading = false) }
        }
    }

    fun setSpreadRate(rate: Float) {
        viewModelScope.launch { prefs.setSpreadRate(rate) }
    }

    fun createAlert(priceType: String, direction: String, targetPrice: Double, repeatMode: String) {
        viewModelScope.launch {
            alertRepository.create(currencyCode, priceType, direction, targetPrice, repeatMode)
                .onSuccess { _alertResult.value = "알림을 등록했어요." }
                .onFailure { _alertResult.value = "알림 등록에 실패했습니다. 잠시 후 다시 시도해 주세요." }
        }
    }

    fun consumeAlertResult() {
        _alertResult.value = null
    }
}
