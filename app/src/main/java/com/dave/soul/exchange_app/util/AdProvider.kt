package com.dave.soul.exchange_app.util

import android.content.Context
import android.widget.RelativeLayout
import com.dave.soul.exchange_app.BuildConfig
import com.dave.soul.exchange_app.R
import com.google.android.gms.ads.*
import org.jetbrains.anko.longToast
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

class AdProvider private constructor(private val context: Context): KoinComponent {
    private lateinit var mInterstitialAd: InterstitialAd
    private var goToMainListener: (() -> Unit)? = null
    private lateinit var adRequest: AdRequest
    private val adChecker: AdChecker by inject()
    private var isAdOnce: Boolean = false

    fun init(): AdProvider {
        RemoteConfigUtil.initialize()
        MobileAds.initialize(context, context.getString(R.string.banner_app_unit_id))
        isAdOnce = RemoteConfigUtil.getConfigBoolean("ad_once")
        adRequest = AdRequest.Builder().build()
        mInterstitialAd = InterstitialAd(context).apply {
            adUnitId = TEST_INTERSTITIAL_ID or context.getString(R.string.ad_interstitial_ad)
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    DLog.w("admob onAdLoaded !!")
                    DLog.w("isClicked AD ---> ${adChecker.isClickedAd()}")
                    if (isAdOnce && adChecker.isClickedAd()) {
                        return
                    }
                    showAd()
                }

                override fun onAdClosed() {
                    DLog.w("admob onAdClosed !!")
                    goToMainListener?.invoke()
                }

                override fun onAdFailedToLoad(p0: Int) {
                    DLog.w("admob onAdFailedToLoad !! $p0")
                    goToMainListener?.invoke()
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    DLog.w("admob click ad onAdClicked !![InterstitialAd]")
                    if (isAdOnce) {
                        clickedAd()
                    }
                }
            }
        }
        return this
    }

    fun loadInterstitialAd(): AdProvider {
        mInterstitialAd.loadAd(adRequest)
        return this
    }

    fun loadBannerAd(bannerContainerView: RelativeLayout) {
        val bannerAd = initBanner()
        if (isAdOnce && adChecker.isClickedAd()) {
            return
        }

        bannerContainerView.addView(bannerAd)
    }

    private fun initBanner(): AdView = AdView(context).apply {
        adSize = AdSize.SMART_BANNER
        adUnitId = TEST_BANNER_ID or context.getString(R.string.banner_ad_unit_id)
        adListener = object : AdListener() {
            override fun onAdOpened() {
                DLog.w("admob click ad onAdOpened !![BANNER]")
                if(isAdOnce) {
                    clickedAd()
                }
            }
        }
        loadAd(adRequest)
    }

    private fun clickedAd() {
        adChecker.clickedAd()
        context.longToast(R.string.thanks_for_clicked_ad)
    }

    fun listener(goToMainListener: () -> Unit): AdProvider {
        this.goToMainListener = goToMainListener
        return this
    }

    private fun showAd() {
        if (mInterstitialAd.isLoaded) {
            mInterstitialAd.show()
        }
    }

    private infix fun String.or(that: String): String = if (BuildConfig.DEBUG) this else that

    companion object : SingletonHolder<AdProvider, Context>(::AdProvider) {
        private const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
        private const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    }
}
