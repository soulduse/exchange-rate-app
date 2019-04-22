package com.dave.soul.exchange_app.view.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.dave.soul.exchange_app.R
import com.dave.soul.exchange_app.util.AdProvider
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.singleTop

class SplashActivity: AppCompatActivity() {

    private val adProvider: AdProvider by lazy { AdProvider.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        initAd()
    }

    private fun initAd() {
            adProvider
                    .init()
                    .listener { goToMain() }
                    .loadInterstitialAd()
    }

    private fun goToMain() {
        startActivity(intentFor<MainActivity>().singleTop())
        finish()
    }
}
