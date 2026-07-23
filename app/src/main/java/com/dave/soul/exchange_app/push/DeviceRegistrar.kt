package com.dave.soul.exchange_app.push

import com.dave.soul.exchange_app.core.repo.AlertRepository
import com.google.firebase.messaging.FirebaseMessaging
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

/** 앱 시작 시 FCM 토큰을 서버에 등록 — 목표환율 알림의 발송 타겟 확보. */
@Singleton
class DeviceRegistrar @Inject constructor(
    private val alertRepository: AlertRepository,
) {
    suspend fun ensureRegistered() {
        val token = runCatching { FirebaseMessaging.getInstance().token.await() }.getOrNull()
            ?: return
        alertRepository.registerDevice(token)
    }
}
