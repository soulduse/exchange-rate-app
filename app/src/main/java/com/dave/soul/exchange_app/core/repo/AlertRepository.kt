package com.dave.soul.exchange_app.core.repo

import com.dave.soul.exchange_app.BuildConfig
import com.dave.soul.exchange_app.core.network.AlertCreateRequest
import com.dave.soul.exchange_app.core.network.AlertDto
import com.dave.soul.exchange_app.core.network.AlertUpdateRequest
import com.dave.soul.exchange_app.core.network.DeviceRegisterRequest
import com.dave.soul.exchange_app.core.network.ExchangeApi
import com.dave.soul.exchange_app.core.prefs.UserPrefs
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

/** 알림/디바이스 — 서버가 원본(평가·발송 전부 서버), 앱은 CRUD 클라이언트. */
@Singleton
class AlertRepository @Inject constructor(
    private val api: ExchangeApi,
    private val prefs: UserPrefs,
) {

    suspend fun registerDevice(fcmToken: String): Result<Unit> = runCatching {
        api.registerDevice(
            DeviceRegisterRequest(
                deviceId = prefs.deviceId(),
                fcmToken = fcmToken,
                packageName = BuildConfig.APPLICATION_ID,
            )
        )
    }

    suspend fun alerts(): Result<List<AlertDto>> =
        runCatching { api.getAlerts(prefs.deviceId()).alerts }

    suspend fun create(
        currencyCode: String,
        baseCurrency: String,
        priceType: String,
        direction: String,
        targetPrice: Double,
        repeatMode: String,
    ): Result<AlertDto> = runCatching {
        api.createAlert(
            AlertCreateRequest(
                deviceId = prefs.deviceId(),
                currencyCode = currencyCode,
                baseCurrency = baseCurrency,
                priceType = priceType,
                direction = direction,
                targetPrice = targetPrice,
                repeatMode = repeatMode,
            )
        )
    }

    suspend fun update(
        id: Long,
        targetPrice: Double? = null,
        direction: String? = null,
        priceType: String? = null,
        repeatMode: String? = null,
        active: Boolean? = null,
    ): Result<AlertDto> = runCatching {
        api.updateAlert(
            id,
            AlertUpdateRequest(
                deviceId = prefs.deviceId(),
                targetPrice = targetPrice,
                direction = direction,
                priceType = priceType,
                repeatMode = repeatMode,
                active = active,
            )
        )
    }

    suspend fun delete(id: Long): Result<Unit> =
        runCatching { api.deleteAlert(id, prefs.deviceId()) }

    suspend fun submitFeedback(category: String, content: String, appVersion: String): Result<Unit> =
        runCatching {
            val plain = "text/plain".toMediaType()
            api.submitFeedback(
                packageName = BuildConfig.APPLICATION_ID,
                category = category.toRequestBody(plain),
                content = content.toRequestBody(plain),
                deviceId = prefs.deviceId().toRequestBody(plain),
                appVersion = appVersion.toRequestBody(plain),
            )
        }
}
