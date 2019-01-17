package com.dave.soul.exchange_app.di

import android.preference.PreferenceManager
import com.dave.soul.exchange_app.util.AdChecker
import com.dave.soul.exchange_app.util.AdProvider
import com.dave.soul.exchange_app.util.CommonSharedPref
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module.module

val appModule = module {
    single { AdProvider.getInstance(androidContext()) }
    single { CommonSharedPref(get()) }
    single { AdChecker(get()) }
    single { PreferenceManager.getDefaultSharedPreferences(androidApplication()) }
}
