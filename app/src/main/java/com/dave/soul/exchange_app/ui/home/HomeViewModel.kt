package com.dave.soul.exchange_app.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dave.soul.exchange_app.R
import com.dave.soul.exchange_app.core.db.RateEntity
import com.dave.soul.exchange_app.core.prefs.UserPrefs
import com.dave.soul.exchange_app.core.repo.RateRepository
import com.dave.soul.exchange_app.push.BriefingTopics
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val selected: List<RateEntity> = emptyList(),
    val all: List<RateEntity> = emptyList(),
    val isRefreshing: Boolean = false,
    val isStale: Boolean = false,
    val errorMessage: String? = null,
) {
    val updatedAtText: String?
        get() = selected.firstOrNull { it.currencyCode == "USD" }?.localTradedAt
            ?: selected.firstOrNull()?.localTradedAt

    val degreeCount: Int?
        get() = selected.firstOrNull { it.currencyCode == "USD" }?.degreeCount
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: RateRepository,
    private val prefs: UserPrefs,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val refreshing = MutableStateFlow(false)
    private val error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<HomeUiState> = combine(
        repository.rates, prefs.selectedCodes, refreshing, error,
    ) { rates, codes, isRefreshing, errorMessage ->
        val byCode = rates.associateBy { it.currencyCode }
        val selected = codes.mapNotNull { byCode[it] }
        val stale = rates.isNotEmpty() &&
            System.currentTimeMillis() - rates.maxOf { it.fetchedAtMillis } > STALE_MILLIS
        HomeUiState(
            selected = selected,
            all = rates,
            isRefreshing = isRefreshing,
            isStale = stale,
            errorMessage = errorMessage,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    init {
        refresh()
        // 기기 언어가 바뀌었을 수 있어 매 실행 브리핑 토픽을 로케일에 재정렬
        viewModelScope.launch { BriefingTopics.sync(prefs.briefingEnabled.first()) }
    }

    fun refresh() {
        viewModelScope.launch {
            refreshing.value = true
            repository.refresh()
                .onSuccess { error.value = null }
                .onFailure { error.value = context.getString(R.string.home_error_load) }
            refreshing.value = false
        }
    }

    fun updateSelection(codes: List<String>) {
        viewModelScope.launch { prefs.setSelectedCodes(codes) }
    }

    // ── 온보딩 (v2 첫 실행: 새 구조 안내 + 브리핑 옵트인) ──

    val onboardingNeeded: StateFlow<Boolean> = prefs.onboardingDone
        .map { !it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun completeOnboarding(enableBriefing: Boolean) {
        viewModelScope.launch {
            if (enableBriefing) {
                prefs.setBriefingEnabled(true)
                BriefingTopics.sync(enabled = true)
            }
            prefs.setOnboardingDone()
        }
    }

    private companion object {
        // 이 시간 넘게 갱신이 없으면 오프라인/스테일 안내
        const val STALE_MILLIS = 30L * 60 * 1000
    }
}
