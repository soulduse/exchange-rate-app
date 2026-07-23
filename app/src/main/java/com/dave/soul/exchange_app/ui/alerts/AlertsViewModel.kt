package com.dave.soul.exchange_app.ui.alerts

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dave.soul.exchange_app.R
import com.dave.soul.exchange_app.core.db.RateEntity
import com.dave.soul.exchange_app.core.network.AlertDto
import com.dave.soul.exchange_app.core.prefs.UserPrefs
import com.dave.soul.exchange_app.core.repo.AlertRepository
import com.dave.soul.exchange_app.core.repo.RateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AlertsUiState(
    val alerts: List<AlertDto> = emptyList(),
    val isLoading: Boolean = false,
    val message: String? = null,
)

@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val alertRepository: AlertRepository,
    rateRepository: RateRepository,
    prefs: UserPrefs,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(AlertsUiState())
    val state: StateFlow<AlertsUiState> = _state.asStateFlow()

    val rates: StateFlow<List<RateEntity>> = rateRepository.rates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** 홈과 같은 기준통화 — 알림 생성 다이얼로그의 현재가/목표가 축. */
    val baseCurrency: StateFlow<String> = prefs.baseCurrency
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "KRW")

    init {
        reload()
    }

    fun reload() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            alertRepository.alerts()
                .onSuccess { _state.value = AlertsUiState(alerts = it) }
                .onFailure {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        message = context.getString(R.string.alerts_error_list),
                    )
                }
        }
    }

    fun create(
        currencyCode: String,
        baseCurrency: String,
        priceType: String,
        direction: String,
        targetPrice: Double,
        repeatMode: String,
    ) {
        viewModelScope.launch {
            alertRepository
                .create(currencyCode, baseCurrency, priceType, direction, targetPrice, repeatMode)
                .onSuccess { reload() }
                .onFailure {
                    _state.value = _state.value.copy(
                        message = context.getString(R.string.alerts_error_create),
                    )
                }
        }
    }

    fun toggleActive(alert: AlertDto) {
        viewModelScope.launch {
            alertRepository.update(alert.id, active = !alert.active)
                .onSuccess { reload() }
                .onFailure {
                    _state.value = _state.value.copy(
                        message = context.getString(R.string.alerts_error_update),
                    )
                }
        }
    }

    fun delete(alert: AlertDto) {
        viewModelScope.launch {
            alertRepository.delete(alert.id)
                .onSuccess { reload() }
                .onFailure {
                    _state.value = _state.value.copy(
                        message = context.getString(R.string.alerts_error_delete),
                    )
                }
        }
    }

    fun consumeMessage() {
        _state.value = _state.value.copy(message = null)
    }
}
