package com.dave.soul.exchange_app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.dave.soul.exchange_app.core.rates.DisplayRate
import com.dave.soul.exchange_app.core.util.displayName
import com.dave.soul.exchange_app.core.util.flagEmoji
import com.dave.soul.exchange_app.core.util.formatRate
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
        // 바텀 인셋은 루트의 배너+내비 바가 이미 소비 — 기본값이면 배너 위 유령 마진
        contentWindowInsets = WindowInsets(0),
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
                            val degree = state.degreeCount
                                ?.let { " · " + stringResource(R.string.home_degree_count, it) }
                                .orEmpty()
                            Text(
                                text = stringResource(
                                    R.string.home_updated_at,
                                    updated.replace('T', ' ').take(16),
                                ) + degree,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                actions = {
                    BaseCurrencySelector(
                        baseCurrency = state.baseCurrency,
                        candidates = state.baseCandidates,
                        countryCodeOf = { code ->
                            if (code == "KRW") "kr"
                            else state.all.firstOrNull { it.currencyCode == code }
                                ?.countryCode.orEmpty()
                        },
                        onSelect = viewModel::setBaseCurrency,
                    )
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
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 8.dp),
            ) {
                if (state.isStale) {
                    item { NoticeBanner(stringResource(R.string.home_offline_notice)) }
                }
                state.errorMessage?.let { message ->
                    item { NoticeBanner(message, isError = true) }
                }
                if (state.baseCurrency == "KRW") {
                    state.selected.firstOrNull()?.let { hero ->
                        item(key = "hero-${hero.code}") {
                            HeroCard(
                                rate = hero,
                                baseCurrency = state.baseCurrency,
                                onClick = { onCurrencyClick(hero.code) },
                            )
                        }
                    }
                    items(state.selected.drop(1), key = { it.code }) { rate ->
                        RateRow(
                            rate = rate,
                            baseCurrency = state.baseCurrency,
                            onClick = { onCurrencyClick(rate.code) },
                        )
                    }
                } else {
                    // 크로스 모드 — 히어로 없이 균일 행(KRW 가상 행은 상세 진입 없음)
                    items(state.selected, key = { it.code }) { rate ->
                        RateRow(
                            rate = rate,
                            baseCurrency = state.baseCurrency,
                            onClick = if (rate.isVirtualKrw) null
                            else ({ onCurrencyClick(rate.code) }),
                        )
                    }
                }
            }
        }
    }

    if (showPicker) {
        CurrencyPickerDialog(
            all = state.all,
            selected = state.selectedCodes,
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

/** 등락 배지 — 연한 배경 칩. 절대 등락이 없으면(크로스 모드) %만 표시. */
@Composable
private fun ChangeBadge(change: Double?, changeRatio: Double?) {
    val value = change ?: changeRatio ?: return
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val container = when {
        value > 0 -> if (dark) RiseRedContainerDark else RiseRedContainer
        value < 0 -> if (dark) FallBlueContainerDark else FallBlueContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val text =
        if (change != null) {
            formatSigned(change) + changeRatio?.let { " (${formatSigned(it)}%)" }.orEmpty()
        } else {
            "${formatSigned(changeRatio ?: 0.0)}%"
        }
    Text(
        text = text,
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

/** 값 단위 라벨 — 기준통화 KRW 면 "원", 그 외엔 통화 코드 그대로. */
@Composable
private fun unitLabel(baseCurrency: String): String =
    if (baseCurrency == "KRW") stringResource(R.string.unit_krw) else baseCurrency

/** 대표 통화(목록 첫 번째) 히어로 카드 — KRW 기준 모드 전용. */
@Composable
private fun HeroCard(rate: DisplayRate, baseCurrency: String, onClick: () -> Unit) {
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
                        displayName(rate.name, rate.nameEng, rate.code),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        rate.code +
                            if (rate.perUnit != 1) {
                                stringResource(R.string.home_per_unit, rate.perUnit)
                            } else "",
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
                            formatRate(rate.value),
                            style = MaterialTheme.typography.displaySmall,
                        )
                        Text(
                            unitLabel(baseCurrency),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp, bottom = 5.dp),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    ChangeBadge(rate.change, rate.changeRatio)
                }
                if (rate.spark.size >= 2) {
                    Sparkline(
                        values = rate.spark,
                        color = changeColor(rate.change),
                        modifier = Modifier.size(width = 96.dp, height = 44.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun RateRow(rate: DisplayRate, baseCurrency: String, onClick: (() -> Unit)?) {
    val shape = RoundedCornerShape(16.dp)
    val colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    val content: @Composable () -> Unit = {
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
                    displayName(rate.name, rate.nameEng, rate.code),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    rate.code + if (rate.perUnit != 1) " (${rate.perUnit})" else "",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (rate.spark.size >= 2) {
                Sparkline(
                    values = rate.spark,
                    color = changeColor(rate.change),
                    modifier = Modifier.size(width = 64.dp, height = 28.dp),
                )
                Spacer(Modifier.width(14.dp))
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        formatRate(rate.value),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        unitLabel(baseCurrency),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 3.dp, bottom = 2.dp),
                    )
                }
                Spacer(Modifier.height(3.dp))
                ChangeBadge(rate.change, rate.changeRatio)
            }
        }
    }
    if (onClick != null) {
        Card(onClick = onClick, shape = shape, colors = colors,
            modifier = Modifier.fillMaxWidth()) { content() }
    } else {
        Card(shape = shape, colors = colors, modifier = Modifier.fillMaxWidth()) { content() }
    }
}

/** 기준통화 드롭다운 — 홈/상세/위젯 표시 기준을 바꾼다(계산기와 독립). */
@Composable
private fun BaseCurrencySelector(
    baseCurrency: String,
    candidates: List<String>,
    countryCodeOf: (String) -> String,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { expanded = true }) {
            Text(
                text = flagEmoji(countryCodeOf(baseCurrency)) + " " + baseCurrency,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Icon(
                Icons.Filled.ArrowDropDown,
                contentDescription = stringResource(R.string.home_base_currency),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Text(
                stringResource(R.string.home_base_currency),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            )
            candidates.forEach { code ->
                DropdownMenuItem(
                    text = { Text(flagEmoji(countryCodeOf(code)) + " " + code) },
                    trailingIcon = {
                        if (code == baseCurrency) Icon(Icons.Filled.Check, contentDescription = null)
                    },
                    onClick = {
                        onSelect(code)
                        expanded = false
                    },
                )
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
        title = { Text(stringResource(R.string.onboarding_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.onboarding_bullet_1))
                Text(stringResource(R.string.onboarding_bullet_2))
                Text(stringResource(R.string.onboarding_bullet_3))
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.onboarding_migration_notice),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onComplete(true) }) {
                Text(stringResource(R.string.onboarding_enable_briefing))
            }
        },
        dismissButton = {
            TextButton(onClick = { onComplete(false) }) {
                Text(stringResource(R.string.onboarding_start))
            }
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
                    placeholder = { Text(stringResource(R.string.home_currency_search)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                LazyColumn(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .heightIn(max = 440.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    val filtered = all.filter {
                        query.isBlank() || it.name.contains(query, ignoreCase = true) ||
                            it.nameEng?.contains(query, ignoreCase = true) == true ||
                            it.currencyCode.contains(query, ignoreCase = true)
                    }
                    items(filtered, key = { it.currencyCode }) { rate ->
                        val isChecked = rate.currencyCode in checked.value
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isChecked) MaterialTheme.colorScheme.secondaryContainer
                                    else Color.Transparent,
                                )
                                .clickable {
                                    val set = checked.value
                                    if (!set.add(rate.currencyCode)) set.remove(rate.currencyCode)
                                    checked.value = set.toMutableSet()
                                }
                                .heightIn(min = 56.dp)
                                .padding(horizontal = 8.dp, vertical = 6.dp),
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
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = null,
                            )
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
            }) { Text(stringResource(R.string.home_confirm)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.home_cancel)) }
        },
    )
}
