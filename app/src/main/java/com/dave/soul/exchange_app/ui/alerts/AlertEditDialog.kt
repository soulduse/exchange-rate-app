package com.dave.soul.exchange_app.ui.alerts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.dave.soul.exchange_app.core.util.formatPrice

private val PRICE_TYPES = listOf(
    "BASE" to "매매기준율",
    "CASH_BUY" to "현찰 살 때",
    "CASH_SELL" to "현찰 팔 때",
    "SEND" to "송금 보낼 때",
    "RECEIVE" to "송금 받을 때",
)

/** 알림 생성/수정 다이얼로그 — 소수점 입력 지원(구버전 리뷰 불만 해소). */
@Composable
fun AlertEditDialog(
    currencyCode: String,
    currentPrice: Double?,
    onSave: (priceType: String, direction: String, targetPrice: Double, repeatMode: String) -> Unit,
    onDismiss: () -> Unit,
    initialPriceType: String = "BASE",
    initialDirection: String = "ABOVE",
    initialTarget: String = "",
    initialRepeat: String = "ONCE",
) {
    var priceType by remember { mutableStateOf(initialPriceType) }
    var direction by remember { mutableStateOf(initialDirection) }
    var repeatMode by remember { mutableStateOf(initialRepeat) }
    var targetText by remember {
        mutableStateOf(initialTarget.ifEmpty { currentPrice?.let { "%.2f".format(it) } ?: "" })
    }
    val target = targetText.toDoubleOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("$currencyCode 알림") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                currentPrice?.let {
                    Text(
                        "현재 ${formatPrice(it)}원",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    PRICE_TYPES.take(3).forEach { (value, label) ->
                        FilterChip(
                            selected = priceType == value,
                            onClick = { priceType = value },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PRICE_TYPES.drop(3).forEach { (value, label) ->
                        FilterChip(
                            selected = priceType == value,
                            onClick = { priceType = value },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        )
                    }
                }
                OutlinedTextField(
                    value = targetText,
                    onValueChange = { input ->
                        // 숫자+소수점만 허용
                        if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d*$"))) {
                            targetText = input
                        }
                    },
                    label = { Text("목표 환율") },
                    singleLine = true,
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
                                    targetText = "%.2f".format(preset)
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
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = direction == "ABOVE",
                        onClick = { direction = "ABOVE" },
                        label = { Text("이상") },
                    )
                    FilterChip(
                        selected = direction == "BELOW",
                        onClick = { direction = "BELOW" },
                        label = { Text("이하") },
                    )
                    FilterChip(
                        selected = repeatMode == "ONCE",
                        onClick = { repeatMode = "ONCE" },
                        label = { Text("1회") },
                    )
                    FilterChip(
                        selected = repeatMode == "REPEAT",
                        onClick = { repeatMode = "REPEAT" },
                        label = { Text("반복") },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = target != null && target > 0,
                onClick = { target?.let { onSave(priceType, direction, it, repeatMode) } },
            ) { Text("저장") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}
