package com.dave.soul.exchange_app.ads

import android.app.Activity
import android.content.Context
import com.dave.soul.exchange_app.BuildConfig
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.thread
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** 광고 총괄 — UMP 동의(fail-open) 후 SDK 초기화, 배너 유닛 제공, 전면 게이팅.
 *  광고 밀도는 보수적으로: 배너(탭 하단) + 런치 전면 1회 + 상세 진입 4회당 전면 1회(60s 간격)
 *  + 종료 팝업 네이티브. */
@Singleton
class AdsManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val initialized = AtomicBoolean(false)
    private var interstitial: InterstitialAd? = null
    private var detailEnterCount = 0
    private var lastInterstitialAt = 0L
    private var launchShown = false

    private val _exitNativeAd = MutableStateFlow<NativeAd?>(null)

    /** 종료 팝업용 네이티브 광고 — 로드 완료 시 세팅, 실패/미로드면 null. */
    val exitNativeAd: StateFlow<NativeAd?> = _exitNativeAd

    val bannerUnitId: String
        get() = if (BuildConfig.DEBUG) TEST_BANNER else BANNER_ID

    private val interstitialUnitId: String
        get() = if (BuildConfig.DEBUG) TEST_INTERSTITIAL else INTERSTITIAL_ID

    private val nativeUnitId: String
        get() = if (BuildConfig.DEBUG) TEST_NATIVE else NATIVE_ID

    val isInterstitialReady: Boolean
        get() = interstitial != null

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
        loadExitNative()
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

    /** 종료 팝업 네이티브 — 앱 세션당 1회 로드해 재사용(팝업 후 대개 종료라 재로드 불필요). */
    private fun loadExitNative() {
        if (!initialized.get()) return
        val loader = AdLoader.Builder(context, nativeUnitId)
            .forNativeAd { ad ->
                _exitNativeAd.value?.destroy()
                _exitNativeAd.value = ad
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    _exitNativeAd.value = null
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setMediaAspectRatio(NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_LANDSCAPE)
                    .build(),
            )
            .build()
        loader.loadAd(AdRequest.Builder().build())
    }

    /** 스플래시 → 전면 → 메인 흐름. 준비된 전면이 없으면 즉시 onDone. 세션당 1회. */
    fun showLaunchInterstitial(activity: Activity, onDone: () -> Unit) {
        val ad = interstitial
        if (launchShown || ad == null) {
            onDone()
            return
        }
        launchShown = true
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitial = null
                loadInterstitial()
                onDone()
            }

            override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                interstitial = null
                loadInterstitial()
                onDone()
            }
        }
        lastInterstitialAt = System.currentTimeMillis()
        interstitial = null
        ad.show(activity)
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
        // AdMob 계정: developerkhy (ca-app-pub-1908860913688060, 앱 ~3760538434)
        const val BANNER_ID = "ca-app-pub-1908860913688060/6574404035"
        const val INTERSTITIAL_ID = "ca-app-pub-1908860913688060/2186144132"
        const val NATIVE_ID = "ca-app-pub-1908860913688060/2312962822"
        const val TEST_BANNER = "ca-app-pub-3940256099942544/6300978111"
        const val TEST_INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
        const val TEST_NATIVE = "ca-app-pub-3940256099942544/2247696110"
        const val SHOW_EVERY = 4
        const val MIN_INTERVAL_MILLIS = 60_000L
    }
}
