package com.dave.soul.exchange_app.core.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

/** 설정/선택 통화/디바이스 식별자 — DataStore 단일 저장소. */
@Singleton
class UserPrefs @Inject constructor(@ApplicationContext private val context: Context) {

    private object Keys {
        val DEVICE_ID = stringPreferencesKey("device_id")
        val SELECTED_CODES = stringPreferencesKey("selected_codes")
        val BRIEFING_ENABLED = booleanPreferencesKey("briefing_enabled")
        val SWING_ENABLED = booleanPreferencesKey("swing_enabled")
        val SPREAD_RATE = floatPreferencesKey("spread_rate")
        val CALC_BASE_CODE = stringPreferencesKey("calc_base_code")
        val CALC_AMOUNT = stringPreferencesKey("calc_amount")
        val THEME_MODE = stringPreferencesKey("theme_mode")  // SYSTEM/LIGHT/DARK
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
    }

    val selectedCodes: Flow<List<String>> = context.dataStore.data.map { prefs ->
        (prefs[Keys.SELECTED_CODES] ?: DEFAULT_CODES).split(",").filter { it.isNotBlank() }
    }

    val briefingEnabled: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.BRIEFING_ENABLED] ?: false }

    val swingEnabled: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.SWING_ENABLED] ?: false }

    val spreadRate: Flow<Float> = context.dataStore.data.map { it[Keys.SPREAD_RATE] ?: 50f }

    val calcBaseCode: Flow<String> =
        context.dataStore.data.map { it[Keys.CALC_BASE_CODE] ?: "USD" }

    val calcAmount: Flow<String> = context.dataStore.data.map { it[Keys.CALC_AMOUNT] ?: "1" }

    val themeMode: Flow<String> = context.dataStore.data.map { it[Keys.THEME_MODE] ?: "SYSTEM" }

    val onboardingDone: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.ONBOARDING_DONE] ?: false }

    /** 앱 설치 단위 UUID — 최초 접근 시 생성·고정(PII 아님, 서버 알림 스코핑 키). */
    suspend fun deviceId(): String {
        val existing = context.dataStore.data.first()[Keys.DEVICE_ID]
        if (existing != null) return existing
        val generated = UUID.randomUUID().toString()
        context.dataStore.edit { it[Keys.DEVICE_ID] = generated }
        return generated
    }

    suspend fun setSelectedCodes(codes: List<String>) {
        context.dataStore.edit { it[Keys.SELECTED_CODES] = codes.joinToString(",") }
    }

    suspend fun setBriefingEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.BRIEFING_ENABLED] = enabled }
    }

    suspend fun setSwingEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SWING_ENABLED] = enabled }
    }

    suspend fun setSpreadRate(rate: Float) {
        context.dataStore.edit { it[Keys.SPREAD_RATE] = rate }
    }

    suspend fun setCalcState(baseCode: String, amount: String) {
        context.dataStore.edit {
            it[Keys.CALC_BASE_CODE] = baseCode
            it[Keys.CALC_AMOUNT] = amount
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode }
    }

    suspend fun setOnboardingDone() {
        context.dataStore.edit { it[Keys.ONBOARDING_DONE] = true }
    }

    companion object {
        const val DEFAULT_CODES = "USD,JPY,EUR,CNY"
    }
}
