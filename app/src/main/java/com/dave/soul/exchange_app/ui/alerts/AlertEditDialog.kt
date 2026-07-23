package com.dave.soul.exchange_app.ui.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dave.soul.exchange_app.R
import com.dave.soul.exchange_app.core.util.formatRate

// 가격 유형 코드 → 라벨 리소스. 렌더 시 stringResource로 현지화.
private val PRICE_TYPES = listOf(
    "BASE" to R.string.detail_base_price,
    "CASH_BUY" to R.string.detail_cash_buy,
    "CASH_SELL" to R.string.detail_cash_sell,
    "SEND" to R.string.detail_send,
    "RECEIVE" to R.string.detail_receive,
)

/** 목표가 입력/프리셋 문자열 — 크로스(1 안팎 값)는 4자리, KRW 고시는 2자리. */
private fun targetText(value: Double): String =
    if (value < 10) "%.4f".format(value) else "%.2f".format(value)

/**
 * 알림 생성/수정 다이얼로그 — 섹션 라벨 + 세그먼트 버튼 구조(v2.2 리디자인).
 * base≠KRW(크로스)면 가격 유형은 매매기준율 고정이라 섹션을 숨기고,
 * 값 단위는 base 통화 코드로 표기한다(서버 평가와 동일 스케일).
 */
@Composable
fun AlertEditDialog(
    currencyCode: String,
    currentPrice: Double?,
    onSave: (priceType: String, direction: String, targetPrice: Double, repeatMode: String) -> Unit,
    onDismiss: () -> Unit,
    baseCurrency: String = "KRW",
    initialPriceType: String = "BASE",
    initialDirection: String = "ABOVE",
    initialTarget: String = "",
    initialRepeat: String = "ONCE",
) {
    val isCross = baseCurrency != "KRW"
    var priceType by remember { mutableStateOf(if (isCross) "BASE" else initialPriceType) }
    var direction by remember { mutableStateOf(initialDirection) }
    var repeatMode by remember { mutableStateOf(initialRepeat) }
    var targetInput by remember {
        mutableStateOf(initialTarget.ifEmpty { currentPrice?.let(::targetText) ?: "" })
    }
    val target = targetInput.toDoubleOrNull()
    val unitText =
        if (isCross) baseCurrency else stringResource(R.string.unit_krw)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (isCross) {
                    stringResource(R.string.alerts_dialog_title_cross, currencyCode, baseCurrency)
                } else {
                    stringResource(R.string.alerts_dialog_title, currencyCode)
                }
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.verticalScroll(rememberScrollState()),
            ) {
                // 현재가 강조 카드 — 목표 입력의 기준점을 한눈에
                currentPrice?.let {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer,
                                RoundedCornerShape(12.dp),
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                    ) {
                        Text(
                            stringResource(R.string.alerts_current_price_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                        Text(
                            formatRate(it) + " " + unitText,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }

                // 가격 유형 — 크로스는 매매기준율 고정이라 섹션 자체를 숨긴다
                if (!isCross) {
                    SectionLabel(stringResource(R.string.alerts_section_price_type))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        PRICE_TYPES.take(3).forEach { (value, labelRes) ->
                            FilterChip(
                                selected = priceType == value,
                                onClick = { priceType = value },
                                label = {
                                    Text(
                                        stringResource(labelRes),
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                                },
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PRICE_TYPES.drop(3).forEach { (value, labelRes) ->
                            FilterChip(
                                selected = priceType == value,
                                onClick = { priceType = value },
                                label = {
                                    Text(
                                        stringResource(labelRes),
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                                },
                            )
                        }
                    }
                }

                SectionLabel(stringResource(R.string.alerts_section_target))
                OutlinedTextField(
                    value = targetInput,
                    onValueChange = { input ->
                        // 숫자+소수점만 허용
                        if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d*$"))) {
                            targetInput = input
                        }
                    },
                    singleLine = true,
                    suffix = { Text(unitText) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                // 빠른 목표가 프리셋 — 현재가 기준 ±0.5% / ±1%
                currentPrice?.let { price ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(-1.0, -0.5, 0.5, 1.0).forEach { pct ->
                            val preset = price * (1 + pct / 100)
                            FilterChip(
                                selected = false,
                                onClick = {
                                    targetInput = targetText(preset)
                                    direction = if (pct >= 0) "ABOVE" else "BELOW"
                                },
                                label = {
                                    Text(
                                        (if (pct > 0) "+" else "") + "$pct%",
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                                },
                            )
                        }
                    }
                }

                SectionLabel(stringResource(R.string.alerts_section_condition))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = direction == "ABOVE",
                        onClick = { direction = "ABOVE" },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    ) { Text(stringResource(R.string.alerts_direction_above)) }
                    SegmentedButton(
                        selected = direction == "BELOW",
                        onClick = { direction = "BELOW" },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    ) { Text(stringResource(R.string.alerts_direction_below)) }
                }

                SectionLabel(stringResource(R.string.alerts_section_repeat))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = repeatMode == "ONCE",
                        onClick = { repeatMode = "ONCE" },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    ) { Text(stringResource(R.string.alerts_repeat_once)) }
                    SegmentedButton(
                        selected = repeatMode == "REPEAT",
                        onClick = { repeatMode = "REPEAT" },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    ) { Text(stringResource(R.string.alerts_repeat_repeat)) }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = target != null && target > 0,
                onClick = { target?.let { onSave(priceType, direction, it, repeatMode) } },
            ) { Text(stringResource(R.string.alerts_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.alerts_cancel)) }
        },
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
