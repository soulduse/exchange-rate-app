package com.dave.soul.exchange_app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dave.soul.exchange_app.BuildConfig
import com.dave.soul.exchange_app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val briefing by viewModel.briefingEnabled.collectAsState()
    val swing by viewModel.swingEnabled.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val message by viewModel.message.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var showFeedback by remember { mutableStateOf(false) }

    LaunchedEffect(message) {
        message?.let {
            snackbar.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = { TopAppBar(title = { Text(stringResource(R.string.tab_settings)) }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Card {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    ToggleRow(
                        title = stringResource(R.string.settings_briefing),
                        description = stringResource(R.string.settings_briefing_desc),
                        checked = briefing,
                        onCheckedChange = viewModel::setBriefing,
                    )
                    ToggleRow(
                        title = stringResource(R.string.settings_swing),
                        description = stringResource(R.string.settings_swing_desc),
                        checked = swing,
                        onCheckedChange = viewModel::setSwing,
                    )
                }
            }
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("화면 테마", fontWeight = FontWeight.SemiBold)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 8.dp),
                    ) {
                        listOf("SYSTEM" to "시스템", "LIGHT" to "라이트", "DARK" to "다크")
                            .forEach { (value, label) ->
                                FilterChip(
                                    selected = themeMode == value,
                                    onClick = { viewModel.setThemeMode(value) },
                                    label = { Text(label) },
                                )
                            }
                    }
                }
            }
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        stringResource(R.string.settings_feedback),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                    )
                    TextButton(onClick = { showFeedback = true }) { Text("의견 보내기") }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    stringResource(R.string.settings_version),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(BuildConfig.VERSION_NAME, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }

    if (showFeedback) {
        FeedbackDialog(
            onSend = { category, content ->
                viewModel.sendFeedback(category, content, BuildConfig.VERSION_NAME)
                showFeedback = false
            },
            onDismiss = { showFeedback = false },
        )
    }
}

@Composable
private fun ToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(
                description,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun FeedbackDialog(
    onSend: (category: String, content: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var category by remember { mutableStateOf("ETC") }
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_feedback)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = category == "BUG",
                        onClick = { category = "BUG" },
                        label = { Text("버그") },
                    )
                    FilterChip(
                        selected = category == "FEATURE",
                        onClick = { category = "FEATURE" },
                        label = { Text("기능 제안") },
                    )
                    FilterChip(
                        selected = category == "ETC",
                        onClick = { category = "ETC" },
                        label = { Text("기타") },
                    )
                }
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = { Text("불편한 점이나 바라는 점을 알려주세요") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = content.isNotBlank(),
                onClick = { onSend(category, content) },
            ) { Text("보내기") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}
