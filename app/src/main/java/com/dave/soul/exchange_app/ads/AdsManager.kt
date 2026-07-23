package com.dave.soul.exchange_app.ads

import android.app.Activity
import android.content.Context
import com.dave.soul.exchange_app.BuildConfig
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.thread

/** 광고 총괄 — UMP 동의(fail-open) 후 SDK 초기화, 배너 유닛 제공, 전면 게이팅.
 *  광고 밀도는 보수적으로: 배너(탭 하단) + 상세 진입 4회당 전면 1회(60s 간격). */
@Singleton
class AdsManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val initialized = AtomicBoolean(false)
    private var interstitial: InterstitialAd? = null
    private var detailEnterCount = 0
    private var lastInterstitialAt = 0L

    val bannerUnitId: String
        get() = if (BuildConfig.DEBUG) TEST_BANNER else BANNER_ID

    private val interstitialUnitId: String
        get() = if (BuildConfig.DEBUG) TEST_INTERSTITIAL else INTERSTITIAL_ID

    /** UMP 동의 흐름 → SDK 초기화. 어떤 실패든 초기화는 진행(fail-open — 광고 전멸 방지). */
    fun initialize(activity: Activity) {
        val consentInfo = UserMessagingPlatform.getConsentInformation(activity)
        val params = ConsentRequestParameters.Builder().build()
        consentInfo.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { _ ->
                    initMobileAds(consentInfo)
                }
            },
            { initMobileAds(consentInfo) },
        )
        // UMP 콜백이 아예 안 오는 극단 상황 대비 — canRequestAds 면 즉시 초기화
        if (consentInfo.canRequestAds()) {
            initMobileAds(consentInfo)
        }
    }

    private fun initMobileAds(consentInfo: ConsentInformation) {
        if (!consentInfo.canRequestAds()) return
        if (!initialized.compareAndSet(false, true)) return
        thread { MobileAds.initialize(context) }
        loadInterstitial()
    }

    private fun loadInterstitial() {
        if (!initialized.get()) return
        InterstitialAd.load(
            context,
            interstitialUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitial = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitial = null
                }
            },
        )
    }

    /** 상세 화면 진입 훅 — 4회마다 + 60초 간격 조건에서 전면 노출. */
    fun onDetailEnter(activity: Activity) {
        detailEnterCount++
        val ad = interstitial ?: return
        val now = System.currentTimeMillis()
        if (detailEnterCount % SHOW_EVERY != 0) return
        if (now - lastInterstitialAt < MIN_INTERVAL_MILLIS) return
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitial = null
                loadInterstitial()
            }
        }
        lastInterstitialAt = now
        ad.show(activity)
    }

    private companion object {
        // 기존 AdMob 계정 유닛 유지 (계정 이관 결정 전까지)
        const val BANNER_ID = "ca-app-pub-1908860913688060/6574404035"
        const val INTERSTITIAL_ID = "ca-app-pub-1908860913688060/2186144132"
        const val TEST_BANNER = "ca-app-pub-3940256099942544/6300978111"
        const val TEST_INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
        const val SHOW_EVERY = 4
        const val MIN_INTERVAL_MILLIS = 60_000L
    }
}
