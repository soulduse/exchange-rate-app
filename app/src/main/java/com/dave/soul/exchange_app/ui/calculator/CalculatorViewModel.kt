package com.dave.soul.exchange_app.ui.calculator

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dave.soul.exchange_app.R
import com.dave.soul.exchange_app.core.db.RateEntity
import com.dave.soul.exchange_app.core.prefs.UserPrefs
import com.dave.soul.exchange_app.core.repo.RateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** 계산기 행 — KRW 포함 멀티 통화 동시 환산(XE 스타일). */
data class CalcRow(
    val code: String,
    val name: String,
    val nameEng: String?,
    val countryCode: String,
    val amount: Double,
    val isBase: Boolean,
)

data class CalculatorUiState(
    val rows: List<CalcRow> = emptyList(),
    val baseCode: String = "USD",
    val amountText: String = "1",
)

@HiltViewModel
class CalculatorViewModel @Inject constructor(
    repository: RateRepository,
    private val prefs: UserPrefs,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val baseCode = MutableStateFlow("USD")
    private val amountText = MutableStateFlow("1")

    init {
        // 마지막 계산 상태 복원 (구버전 리뷰: "재실행마다 초기화" 불만 해소)
        viewModelScope.launch {
            baseCode.value = prefs.calcBaseCode.first()
            amountText.value = prefs.calcAmount.first()
        }
    }

    val uiState: StateFlow<CalculatorUiState> = combine(
        repository.rates, prefs.selectedCodes, baseCode, amountText,
    ) { rates, selected, base, amount ->
        buildState(rates, selected, base, amount)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalculatorUiState())

    private fun buildState(
        rates: List<RateEntity>,
        selected: List<String>,
        base: String,
        amountText: String,
    ): CalculatorUiState {
        val byCode = rates.associateBy { it.currencyCode }
        val codes = (listOf("KRW") + selected).distinct()
        val amount = amountText.toDoubleOrNull() ?: 0.0

        val baseKrwPerOne =
            if (base == "KRW") 1.0
            else byCode[base]?.krwPerOne()
                ?: return CalculatorUiState(baseCode = base, amountText = amountText)
        val amountInKrw = amount * baseKrwPerOne

        val rows = codes.mapNotNull { code ->
            if (code == "KRW") {
                // name 은 이미 로케일 리소스이므로 nameEng 불필요(null → name 그대로 표시)
                CalcRow(
                    code = "KRW",
                    name = context.getString(R.string.calc_krw_name),
                    nameEng = null,
                    countryCode = "kr",
                    amount = amountInKrw,
                    isBase = base == "KRW",
                )
            } else {
                val rate = byCode[code] ?: return@mapNotNull null
                CalcRow(
                    code = code,
                    name = rate.name,
                    nameEng = rate.nameEng,
                    countryCode = rate.countryCode,
                    amount = amountInKrw / rate.krwPerOne(),
                    isBase = base == code,
                )
            }
        }
        return CalculatorUiState(rows = rows, baseCode = base, amountText = amountText)
    }

    fun setAmount(text: String) {
        if (text.isEmpty() || text.matches(Regex("^\\d*\\.?\\d*$"))) {
            amountText.value = text
            persist()
        }
    }

    /** 행 탭 — 해당 통화를 입력 기준으로 전환(현재 환산값을 이어받아 자연스럽게 전환). */
    fun setBase(code: String) {
        val current = uiState.value.rows.firstOrNull { it.code == code }
        baseCode.value = code
        if (current != null) {
            amountText.value = trimAmount(current.amount)
        }
        persist()
    }

    private fun trimAmount(value: Double): String =
        if (value % 1.0 == 0.0) value.toLong().toString() else "%.2f".format(value)

    private fun persist() {
        viewModelScope.launch { prefs.setCalcState(baseCode.value, amountText.value) }
    }
}
