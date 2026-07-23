package com.dave.soul.exchange_app.push

import com.google.firebase.messaging.FirebaseMessaging
import java.util.Locale

/**
 * 브리핑은 서버 조립 문자열이라 언어별 토픽으로 분리돼 있다 — 기기 언어에 맞는 하나만 구독.
 * 로케일이 바뀌어도 따라가도록 켤 때만이 아니라 앱 시작 시에도 sync 를 호출한다.
 */
object BriefingTopics {
    private const val TOPIC_KO = "exchange_briefing"
    private const val TOPIC_EN = "exchange_briefing_en"
    private val ALL = listOf(TOPIC_KO, TOPIC_EN)

    private fun current(): String =
        if (Locale.getDefault().language == "ko") TOPIC_KO else TOPIC_EN

    /** 활성이면 현 언어 토픽만 구독하고 나머지는 해지, 비활성이면 전부 해지. */
    fun sync(enabled: Boolean) {
        val messaging = FirebaseMessaging.getInstance()
        val active = current()
        for (topic in ALL) {
            if (enabled && topic == active) {
                messaging.subscribeToTopic(topic)
            } else {
                messaging.unsubscribeFromTopic(topic)
            }
        }
    }
}
