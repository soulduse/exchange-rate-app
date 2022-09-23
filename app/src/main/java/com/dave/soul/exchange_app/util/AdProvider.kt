package com.dave.soul.exchange_app.util

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import com.dave.soul.exchange_app.BuildConfig
import com.dave.soul.exchange_app.R
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import javax.inject.Singleton

/**
 * InterstitialAd Usages
 *
 * adProvider
 *      .with(activity)
 *      .fallback { do something }
 *      .dismissCallback { do something }
 *      .loadInterstitialAd()
 *      .afterLoaded { adProvider.show() }
 *
 * BannerAd Usages
 *
 * adProvider
 *      .loadBannerAd(view)
 */

@Singleton
class AdProvider(private val context: Context) {
    private val adRequest by lazy { AdRequest.Builder().build() }
    private var interstitialAd: InterstitialAd? = null
    private var activity: Activity? = null
    private var fallback: (() -> Unit)? = null
    private var dismissCallback: (() -> Unit)? = null
    private var afterLoadedCallback: (() -> Unit)? = null

    fun with(activity: Activity): AdProvider {
        this.activity = activity
        return this
    }

    fun fallback(fallback: (() -> Unit)?): AdProvider {
        this.fallback = fallback
        return this
    }

    fun dismissCallback(dismissCallback: (() -> Unit)?): AdProvider {
        this.dismissCallback = dismissCallback
        return this
    }

    fun loadInterstitialAd(): AdProvider {
        InterstitialAd.load(
            context,
            TEST_INTERSTITIAL_ID or context.getString(R.string.ad_interstitial_id),
            adRequest,
            getInterstitialAdLoadCallback()
        )
        return this
    }

    fun afterLoaded(afterLoadedCallback: (() -> Unit)? = null): AdProvider {
        this.afterLoadedCallback = afterLoadedCallback
        return this
    }

    fun show() {
        if (interstitialAd != null && activity != null) {
            interstitialAd!!.show(activity!!)
        }
    }

    fun loadBannerAd(adView: ViewGroup) {
        adView.addView(
            AdView(context).apply {
                adUnitId = TEST_BANNER_ID or context.getString(R.string.ad_banner_id)
                setAdSize(getBannerSize(adView))
                loadAd(adRequest)
            }
        )
    }

    private fun getBannerSize(adView: ViewGroup): AdSize {
        val adViewWidth = getAdWidth(adView)
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adViewWidth)
    }

    private fun getInterstitialAdLoadCallback() =
        object : InterstitialAdLoadCallback() {

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                fallback?.invoke()
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                this@AdProvider.interstitialAd = interstitialAd.apply {
                    fullScreenContentCallback = initFullScreenContentCallback()
                }
                afterLoadedCallback?.invoke()
            }
        }

    private fun initFullScreenContentCallback() =
        object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                dismissCallback?.invoke()
            }
        }

    private fun getAdWidth(view: ViewGroup): Int {
        var viewWidthPixels = view.width.toFloat()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            val insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            val insetsWidth = insets.left + insets.right
            val density = Resources.getSystem().displayMetrics.density
            val width = (windowMetrics.bounds.width() - insetsWidth)
            if (viewWidthPixels == 0F) {
                viewWidthPixels = width.toFloat()
            }
            return (viewWidthPixels / density).toInt()
        }

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val density = displayMetrics.density
        if (viewWidthPixels == 0F) {
            viewWidthPixels = displayMetrics.widthPixels.toFloat()
        }
        return (viewWidthPixels / density).toInt()
    }

    private infix fun String.or(that: String) = if (BuildConfig.DEBUG) this else that

    companion object {
        private const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
        private const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
        private const val TEST_NATIVE_ID = "ca-app-pub-3940256099942544/2247696110"
    }
}
