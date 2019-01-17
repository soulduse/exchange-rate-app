package com.dave.soul.exchange_app.util

import android.content.SharedPreferences

class CommonSharedPref(private val pref: SharedPreferences) {

    fun <T> add(key: String, value: T) {
        val edit = pref.edit()
        with(edit) {
            when(value) {
                is String -> putString(key, value)
                is Int -> putInt(key, value)
                is Boolean -> putBoolean(key, value)
                is Long -> putLong(key, value)
                else -> {}
            }
        }
        edit.apply()
    }

    val countDetailView: Int
        get() = pref.getInt(Global.EXTRA_DETAIL_COUNT, 0)

    val clickedAdDate: String?
        get() = pref.getString(Global.EXTRA_CLICKED_AD_DATE, null)
}
