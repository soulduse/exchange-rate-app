package com.dave.soul.exchange_app.util

import com.dave.soul.exchange_app.R
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import java.util.concurrent.TimeUnit

object RemoteConfigUtil {

    fun initialize() {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(TimeUnit.HOURS.toSeconds(12))
            .build()

        remoteConfig.run {
            setConfigSettingsAsync(configSettings)
            setDefaultsAsync(R.xml.remote_config_defaults)
            fetch(0).addOnCompleteListener { task ->
                if (task.isSuccessful) remoteConfig.fetchAndActivate()
            }
        }
    }

    fun getConfigBoolean(key: String): Boolean = FirebaseRemoteConfig.getInstance().getBoolean(key)
}
