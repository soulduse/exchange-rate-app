package com.dave.soul.exchange_app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dave.soul.exchange_app.R
import com.dave.soul.exchange_app.core.db.RateEntity
import com.dave.soul.exchange_app.core.util.displayName
import com.dave.soul.exchange_app.core.util.flagEmoji
import com.dave.soul.exchange_app.core.util.formatPrice
import com.dave.soul.exchange_app.core.util.formatSigned
import com.dave.soul.exchange_app.ui.common.Sparkline
import com.dave.soul.exchange_app.ui.theme.FallBlue
import com.dave.soul.exchange_app.ui.theme.FallBlueContainer
import com.dave.soul.exchange_app.ui.theme.FallBlueContainerDark
import com.dave.soul.exchange_app.ui.theme.RiseRed
import com.dave.soul.exchange_app.ui.theme.RiseRedContainer
import com.dave.soul.exchange_app.ui.theme.RiseRedContainerDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onCurrencyClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val onboardingNeeded by viewModel.onboardingNeeded.collectAsState()
    var showPicker by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                title = {
                    Column {
                        Text(
                            stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                        )
                        state.updatedAtText?.let { updated ->
                            val degree = state.degreeCount?.let { " · ${it}회차" }.orEmpty()
                            Text(
                                text = updated.replace('T', ' ').take(16) + " 기준" + degree,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showPicker = true }) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = stringResource(R.string.home_add_currency),
                        )
                    }
                },
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            ) {
                if (state.isStale) {
                    item { NoticeBanner(stringResource(R.string.home_offline_notice)) }
                }
                state.errorMessage?.let { message ->
                    item { NoticeBanner(message, isError = true) }
                }
                state.selected.firstOrNull()?.let { hero ->
                    item(key = "hero-${hero.currencyCode}") {
                        HeroCard(rate = hero, onClick = { onCurrencyClick(hero.currencyCode) })
                    }
                }
                items(state.selected.drop(1), key = { it.currencyCode }) { rate ->
                    RateRow(rate = rate, onClick = { onCurrencyClick(rate.currencyCode) })
                }
            }
        }
    }

    if (showPicker) {
        CurrencyPickerDialog(
            all = state.all,
            selected = state.selected.map { it.currencyCode },
            onConfirm = { codes ->
                viewModel.updateSelection(codes)
                showPicker = false
            },
            onDismiss = { showPicker = false },
        )
    }

    if (onboardingNeeded) {
        OnboardingDialog(onComplete = viewModel::completeOnboarding)
    }
}

@Composable
private fun changeColor(change: Double?): Color = when {
    (change ?: 0.0) > 0 -> RiseRed
    (change ?: 0.0) < 0 -> FallBlue
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}

/** 등락 배지 — 연한 배경 칩. */
@Composable
private fun ChangeBadge(change: Double?, changeRatio: Double?) {
    val value = change ?: return
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val container = when {
        value > 0 -> if (dark) RiseRedContainerDark else RiseRedContainer
        value < 0 -> if (dark) FallBlueContainerDark else FallBlueContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val ratioText = changeRatio?.let { " (${formatSigned(it)}%)" }.orEmpty()
    Text(
        text = formatSigned(value) + ratioText,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = changeColor(value),
        modifier = Modifier
            .background(container, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

private fun Color.luminance(): Float =
    (0.299f * red + 0.587f * green + 0.114f * blue)

/** 대표 통화(목록 첫 번째) 히어로 카드. */
@Composable
private fun HeroCard(rate: RateEntity, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(flagEmoji(rate.countryCode), style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        displayName(rate.name, rate.currencyCode),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        rate.currencyCode + if (rate.perUnit != 1) " · ${rate.perUnit}단위" else "",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            formatPrice(rate.basePrice),
                            style = MaterialTheme.typography.displaySmall,
                        )
                        Text(
                            "원",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp, bottom = 5.dp),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    ChangeBadge(rate.change, rate.changeRatio)
                }
                val sparkValues = rate.sparkValues()
                if (sparkValues.size >= 2) {
                    Sparkline(
                        values = sparkValues,
                        color = changeColor(rate.change),
                        modifier = Modifier.size(width = 96.dp, height = 44.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun RateRow(rate: RateEntity, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(flagEmoji(rate.countryCode), style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    displayName(rate.name, rate.currencyCode),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    rate.currencyCode + if (rate.perUnit != 1) " (${rate.perUnit})" else "",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            val sparkValues = rate.sparkValues()
            if (sparkValues.size >= 2) {
                Sparkline(
                    values = sparkValues,
                    color = changeColor(rate.change),
                    modifier = Modifier.size(width = 64.dp, height = 28.dp),
                )
                Spacer(Modifier.width(14.dp))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    formatPrice(rate.basePrice),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(3.dp))
                ChangeBadge(rate.change, rate.changeRatio)
            }
        }
    }
}

@Composable
private fun NoticeBanner(message: String, isError: Boolean = false) {
    Text(
        text = message,
        style = MaterialTheme.typography.labelMedium,
        color = if (isError) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
    )
}

@Composable
private fun OnboardingDialog(onComplete: (enableBriefing: Boolean) -> Unit) {
    AlertDialog(
        onDismissRequest = { onComplete(false) },
        title = { Text("환율알리미가 새로워졌어요 ✨") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("• 목표 환율 알림이 서버에서 실시간으로 도착해요")
                Text("• 현찰·송금 4가지 환율과 우대율 계산을 지원해요")
                Text("• 52주 범위·기간별 차트로 타이밍을 판단하세요")
                Spacer(Modifier.height(4.dp))
                Text(
                    "기존에 설정하신 알림은 새 구조로 이전되지 않았어요. 알림 탭에서 다시 등록해 주세요.",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onComplete(true) }) { Text("아침 브리핑도 받을래요") }
        },
        dismissButton = {
            TextButton(onClick = { onComplete(false) }) { Text("시작하기") }
        },
    )
}

@Composable
private fun CurrencyPickerDialog(
    all: List<RateEntity>,
    selected: List<String>,
    onConfirm: (List<String>) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val checked = remember { mutableStateOf(selected.toMutableSet()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.home_add_currency)) },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    placeholder = { Text("통화 검색") },
                    modifier = Modifier.fillMaxWidth(),
                )
                LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
                    val filtered = all.filter {
                        query.isBlank() || it.name.contains(query, ignoreCase = true) ||
                            it.currencyCode.contains(query, ignoreCase = true)
                    }
                    items(filtered, key = { it.currencyCode }) { rate ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val set = checked.value
                                    if (!set.add(rate.currencyCode)) set.remove(rate.currencyCode)
                                    checked.value = set.toMutableSet()
                                },
                        ) {
                            Checkbox(
                                checked = rate.currencyCode in checked.value,
                                onCheckedChange = null,
                            )
                            Text("${flagEmoji(rate.countryCode)} ${rate.name}")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val kept = selected.filter { it in checked.value }
                val added = checked.value.filter { it !in selected }
                onConfirm(kept + added)
            }) { Text("확인") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}
