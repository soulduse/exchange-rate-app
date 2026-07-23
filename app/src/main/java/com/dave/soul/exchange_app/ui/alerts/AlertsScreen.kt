package com.dave.soul.exchange_app.ui.alerts

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.annotation.StringRes
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dave.soul.exchange_app.R
import com.dave.soul.exchange_app.core.db.RateEntity
import com.dave.soul.exchange_app.core.network.AlertDto
import com.dave.soul.exchange_app.core.rates.CrossRates
import com.dave.soul.exchange_app.core.util.displayName
import com.dave.soul.exchange_app.core.util.flagEmoji
import com.dave.soul.exchange_app.core.util.formatPrice
import com.dave.soul.exchange_app.core.util.formatRate
import com.dave.soul.exchange_app.core.util.priceTypeLabelRes

/** 가격 유형 코드의 현지화 라벨 — 미지원 코드는 코드 원문 폴백. */
@Composable
private fun priceTypeLabel(type: String): String {
    @StringRes val resId = priceTypeLabelRes(type)
    return if (resId != null) stringResource(resId) else type
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(viewModel: AlertsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val rates by viewModel.rates.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var showCreate by remember { mutableStateOf(false) }
    var pickerCode by remember { mutableStateOf("USD") }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = { TopAppBar(title = { Text(stringResource(R.string.tab_alerts)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreate = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.alerts_add))
            }
        },
    ) { padding ->
        if (state.alerts.isEmpty() && !state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(R.string.alerts_empty),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.alerts, key = { it.id }) { alert ->
                    AlertRow(
                        alert = alert,
                        onToggle = { viewModel.toggleActive(alert) },
                        onDelete = { viewModel.delete(alert) },
                    )
                }
            }
        }
    }

    if (showCreate) {
        val base by viewModel.baseCurrency.collectAsState()
        val byCode = rates.associateBy { it.currencyCode }
        // 기준통화 자신에 대한 알림은 크로스 불성립(base==대상 400),
        // base 시세 미보유 시에도 크로스 계산 불가 — 둘 다 KRW 축으로 폴백
        val baseKrwPerOne =
            if (pickerCode == base) null else CrossRates.krwPerOne(base, byCode)
        val dialogBase = if (base != "KRW" && baseKrwPerOne != null) base else "KRW"
        val currentPrice = byCode[pickerCode]?.let { target ->
            if (dialogBase == "KRW") target.basePrice
            else CrossRates.displayValue(target, baseKrwPerOne!!)
        }
        CurrencySelectableAlertDialog(
            rates = rates,
            selectedCode = pickerCode,
            onCodeChange = { pickerCode = it },
            baseCurrency = dialogBase,
            currentPrice = currentPrice,
            onSave = { base2, priceType, direction, target, repeat ->
                viewModel.create(pickerCode, base2, priceType, direction, target, repeat)
                showCreate = false
            },
            onDismiss = { showCreate = false },
        )
    }
}

@Composable
private fun CurrencySelectableAlertDialog(
    rates: List<RateEntity>,
    selectedCode: String,
    onCodeChange: (String) -> Unit,
    baseCurrency: String,
    currentPrice: Double?,
    onSave: (String, String, String, Double, String) -> Unit,
    onDismiss: () -> Unit,
) {
    // 1단계 통화 선택 → 2단계 조건 입력. 주요 통화 우선 정렬.
    val ordered = remember(rates) {
        val majors = listOf("USD", "JPY", "EUR", "CNY")
        val byCode = rates.associateBy { it.currencyCode }
        (majors.mapNotNull { byCode[it] } +
            rates.filterNot { it.currencyCode in majors }.sortedBy { it.currencyCode })
    }
    var pickingCurrency by remember { mutableStateOf(true) }

    if (pickingCurrency) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.alerts_select_currency)) },
            text = {
                LazyColumn {
                    items(ordered, key = { it.currencyCode }) { rate ->
                        // 홈 통화 추가 피커와 동일한 국기+현지화 통화명 행 — 전 언어 대응
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onCodeChange(rate.currencyCode)
                                    pickingCurrency = false
                                }
                                .padding(vertical = 10.dp),
                        ) {
                            Text(
                                flagEmoji(rate.countryCode),
                                style = MaterialTheme.typography.headlineSmall,
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    displayName(rate.name, rate.nameEng, rate.currencyCode),
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    rate.currencyCode +
                                        if (rate.perUnit != 1) " (${rate.perUnit})" else "",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            if (rate.currencyCode == selectedCode) {
                                Text("✓", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.alerts_cancel)) }
            },
        )
    } else {
        AlertEditDialog(
            currencyCode = selectedCode,
            baseCurrency = baseCurrency,
            currentPrice = currentPrice,
            onSave = { priceType, direction, target, repeat ->
                onSave(baseCurrency, priceType, direction, target, repeat)
            },
            onDismiss = onDismiss,
        )
    }
}

@Composable
private fun AlertRow(alert: AlertDto, onToggle: () -> Unit, onDelete: () -> Unit) {
    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        // 크로스 알림은 FX 페어 표기("EUR/USD") — 가격 유형 라벨은 생략
                        if (alert.isCross) "${alert.currencyCode}/${alert.baseCurrency}"
                        else alert.currencyCode,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (!alert.isCross) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            priceTypeLabel(alert.priceType),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                val directionLabel = stringResource(
                    if (alert.direction == "ABOVE") R.string.alerts_direction_above
                    else R.string.alerts_direction_below,
                )
                val repeatLabel = stringResource(
                    if (alert.repeatMode == "REPEAT") R.string.alerts_repeat_repeat
                    else R.string.alerts_repeat_once,
                )
                Text(
                    if (alert.isCross) {
                        stringResource(
                            R.string.alerts_row_summary_cross,
                            formatRate(alert.targetPrice),
                            alert.baseCurrency,
                            directionLabel,
                            repeatLabel,
                        )
                    } else {
                        stringResource(
                            R.string.alerts_row_summary,
                            formatPrice(alert.targetPrice),
                            directionLabel,
                            repeatLabel,
                        )
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Switch(checked = alert.active, onCheckedChange = { onToggle() })
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.alerts_delete),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
