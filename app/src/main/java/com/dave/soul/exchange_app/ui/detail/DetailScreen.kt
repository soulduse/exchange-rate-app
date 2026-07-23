package com.dave.soul.exchange_app.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dave.soul.exchange_app.R
import com.dave.soul.exchange_app.core.db.RateEntity
import com.dave.soul.exchange_app.core.util.flagEmoji
import com.dave.soul.exchange_app.core.util.formatPrice
import com.dave.soul.exchange_app.core.util.formatSigned
import com.dave.soul.exchange_app.ui.alerts.AlertEditDialog
import com.dave.soul.exchange_app.ui.theme.FallBlue
import com.dave.soul.exchange_app.ui.theme.RiseRed

private val CHART_RANGES = listOf("1w", "1m", "3m", "6m", "1y", "3y")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    currencyCode: String,
    onBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel(),
) {
    val rate by viewModel.rate.collectAsState()
    val chart by viewModel.chart.collectAsState()
    val spreadRate by viewModel.spreadRate.collectAsState()
    val alertResult by viewModel.alertResult.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var showAlertDialog by remember { mutableStateOf(false) }

    LaunchedEffect(alertResult) {
        alertResult?.let {
            snackbar.showSnackbar(it)
            viewModel.consumeAlertResult()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {
                    val r = rate
                    Text(if (r != null) "${flagEmoji(r.countryCode)} ${r.name}" else currencyCode)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
            )
        }
    ) { padding ->
        val r = rate ?: return@Scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PriceHeader(r)
            ChartCard(chart = chart, onRangeSelect = viewModel::loadHistory)
            FourPricesCard(r)
            SpreadCard(r, spreadRate, viewModel::setSpreadRate)
            Band52wCard(r)
            Button(
                onClick = { showAlertDialog = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.detail_create_alert))
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    if (showAlertDialog && rate != null) {
        AlertEditDialog(
            currencyCode = currencyCode,
            currentPrice = rate?.basePrice,
            onSave = { priceType, direction, target, repeat ->
                viewModel.createAlert(priceType, direction, target, repeat)
                showAlertDialog = false
            },
            onDismiss = { showAlertDialog = false },
        )
    }
}

@Composable
private fun PriceHeader(rate: RateEntity) {
    val change = rate.change ?: 0.0
    val changeColor = when {
        change > 0 -> RiseRed
        change < 0 -> FallBlue
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Column {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                formatPrice(rate.basePrice),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "원" + if (rate.perUnit != 1) " / ${rate.perUnit}${rate.currencyCode}" else "",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }
        Row {
            Text(
                formatSigned(change),
                color = changeColor,
                style = MaterialTheme.typography.titleSmall,
            )
            rate.changeRatio?.let {
                Spacer(Modifier.width(6.dp))
                Text(
                    "(${formatSigned(it)}%)",
                    color = changeColor,
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            rate.degreeCount?.let {
                Spacer(Modifier.width(8.dp))
                Text(
                    "${it}회차",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ChartCard(chart: ChartState, onRangeSelect: (String) -> Unit) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CHART_RANGES.forEach { range ->
                    FilterChip(
                        selected = chart.range == range,
                        onClick = { onRangeSelect(range) },
                        label = { Text(range.uppercase()) },
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            if (chart.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            val closes = chart.items.map { it.close }
            if (closes.size >= 2) {
                RateChart(closes = closes)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        "최저 ${formatPrice(closes.min())}",
                        style = MaterialTheme.typography.labelSmall,
                        color = FallBlue,
                    )
                    Text(
                        "최고 ${formatPrice(closes.max())}",
                        style = MaterialTheme.typography.labelSmall,
                        color = RiseRed,
                    )
                }
            } else if (!chart.isLoading) {
                Text(
                    "차트 데이터를 준비 중입니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun FourPricesCard(rate: RateEntity) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PriceLine(stringResource(R.string.detail_cash_buy), rate.cashBuy)
            PriceLine(stringResource(R.string.detail_cash_sell), rate.cashSell)
            PriceLine(stringResource(R.string.detail_send), rate.send)
            PriceLine(stringResource(R.string.detail_receive), rate.receive)
        }
    }
}

@Composable
private fun PriceLine(label: String, value: Double?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value?.let(::formatPrice) ?: "—",
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun SpreadCard(rate: RateEntity, spreadRate: Float, onSpreadChange: (Float) -> Unit) {
    val cashBuy = rate.cashBuy
    val cashSell = rate.cashSell
    if (cashBuy == null || cashSell == null) return
    // 우대율은 스프레드(기준율과의 차)에 적용된다
    val effectiveBuy = rate.basePrice + (cashBuy - rate.basePrice) * (1 - spreadRate / 100.0)
    val effectiveSell = rate.basePrice - (rate.basePrice - cashSell) * (1 - spreadRate / 100.0)

    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.detail_spread_rate) + " ${spreadRate.toInt()}%",
                fontWeight = FontWeight.SemiBold,
            )
            Slider(
                value = spreadRate,
                onValueChange = onSpreadChange,
                valueRange = 0f..100f,
                steps = 19,
            )
            PriceLine("우대 적용 살 때", effectiveBuy)
            Spacer(Modifier.height(6.dp))
            PriceLine("우대 적용 팔 때", effectiveSell)
        }
    }
}

@Composable
private fun Band52wCard(rate: RateEntity) {
    val high = rate.high52w
    val low = rate.low52w
    if (high == null || low == null || high <= low) return
    val position = ((rate.basePrice - low) / (high - low)).coerceIn(0.0, 1.0)
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(stringResource(R.string.detail_52w_band), fontWeight = FontWeight.SemiBold)
                Text(
                    "최근 1년 중 상위 ${(100 - position * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { position.toFloat() },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(formatPrice(low), style = MaterialTheme.typography.labelSmall, color = FallBlue)
                Text(formatPrice(high), style = MaterialTheme.typography.labelSmall, color = RiseRed)
            }
        }
    }
}
