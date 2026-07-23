package com.dave.soul.exchange_app.push

import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dave.soul.exchange_app.ExchangeApp
import com.dave.soul.exchange_app.MainActivity
import com.dave.soul.exchange_app.R
import com.dave.soul.exchange_app.core.repo.AlertRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ExchangeMessagingService : FirebaseMessagingService() {

    @Inject lateinit var alertRepository: AlertRepository

    // 서비스는 시스템이 수시로 재생성 — onDestroy 에서 반드시 cancel (스코프 누적 방지)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        scope.launch { alertRepository.registerDevice(token) }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        // 서버는 data-only 로 보낸다 — 조립 필드가 있으면 기기 언어로 렌더하고,
        // 없으면 서버 브리지(data.title/body) → notification payload 순으로 폴백.
        val composed = NotificationComposer.compose(this, message.data)
        val title = composed?.first
            ?: message.data["title"]
            ?: message.notification?.title
            ?: getString(R.string.app_name)
        val body = composed?.second
            ?: message.data["body"]
            ?: message.notification?.body
            ?: return
        showNotification(title, body, message.data["currencyCode"])
    }

    private fun showNotification(title: String, body: String, currencyCode: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            currencyCode?.let { putExtra("currencyCode", it) }
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(this, ExchangeApp.CHANNEL_ALERTS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()
        runCatching {
            NotificationManagerCompat.from(this).notify(body.hashCode(), notification)
        }
    }
}
