package com.dave.soul.exchange_app.util

import com.dave.soul.exchange_app.BuildConfig
import com.dave.soul.exchange_app.R
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

object RemoteConfigUtil {

    fun initialize() {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build()

        remoteConfig.run {
            setConfigSettings(configSettings)
            setDefaults(R.xml.remote_config_defaults)
            fetch(0).addOnCompleteListener { task ->
                if (task.isSuccessful) remoteConfig.activateFetched()
            }
        }
    }

    fun getConfigBoolean(key: String): Boolean
            = FirebaseRemoteConfig.getInstance().getBoolean(key)
}
