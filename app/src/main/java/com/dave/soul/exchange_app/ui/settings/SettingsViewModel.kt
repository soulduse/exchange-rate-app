package com.dave.soul.exchange_app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dave.soul.exchange_app.core.prefs.UserPrefs
import com.dave.soul.exchange_app.core.repo.AlertRepository
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: UserPrefs,
    private val alertRepository: AlertRepository,
) : ViewModel() {

    val briefingEnabled: StateFlow<Boolean> = prefs.briefingEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val swingEnabled: StateFlow<Boolean> = prefs.swingEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val themeMode: StateFlow<String> = prefs.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "SYSTEM")

    fun setThemeMode(mode: String) {
        viewModelScope.launch { prefs.setThemeMode(mode) }
    }

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun setBriefing(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setBriefingEnabled(enabled)
            toggleTopic("exchange_briefing", enabled)
        }
    }

    fun setSwing(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setSwingEnabled(enabled)
            toggleTopic("exchange_swing", enabled)
        }
    }

    fun sendFeedback(category: String, content: String, appVersion: String) {
        viewModelScope.launch {
            alertRepository.submitFeedback(category, content, appVersion)
                .onSuccess { _message.value = "소중한 의견 감사합니다!" }
                .onFailure { _message.value = "전송에 실패했습니다. 잠시 후 다시 시도해 주세요." }
        }
    }

    fun consumeMessage() {
        _message.value = null
    }

    private fun toggleTopic(topic: String, subscribe: Boolean) {
        val messaging = FirebaseMessaging.getInstance()
        if (subscribe) messaging.subscribeToTopic(topic) else messaging.unsubscribeFromTopic(topic)
    }
}
