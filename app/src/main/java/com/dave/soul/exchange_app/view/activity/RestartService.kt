package com.dave.soul.exchange_app.view.activity

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.preference.PreferenceManager
import android.util.Log
import androidx.work.WorkManager
import com.dave.soul.exchange_app.view.activity.SettingActivity
import com.dave.soul.exchange_app.view.service.AlarmService
import com.dave.soul.exchange_app.view.service.BackupWorker
// Removed anko import
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Created by soul on 2017. 7. 31..
 */

class RestartService : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i("000 RestartService", "RestartService called : " + intent.action!!)

        /**
         * 서비스 죽일때 알람으로 다시 서비스 등록
         */
        if (intent.action == "ACTION.RESTART.AlarmService") {
            Log.i("000 RestartService", "ACTION.RESTART.AlarmService ")
            context.serviceStart<AlarmService>()
        }

        /**
         * 폰 재시작 할때 서비스 등록
         */
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.i("RestartService", "ACTION_BOOT_COMPLETED")
            context.serviceStart<AlarmService>()
        }
    }
}

inline fun <reified T : Service> Context.serviceStart() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Timber.w("Android 12+ detected, using WorkManager for background alarm checks")

        // Get refresh interval from SharedPreferences
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val refreshTime = sharedPref.getString(SettingActivity.KEY_PREF_REFRESH_TIME_TYPE, "30") ?: "30"
        val intervalMinutes = refreshTime.toLongOrNull() ?: 30L

        // Use PeriodicWorkRequest for repeating work (minimum 15 minutes)
        val repeatInterval = maxOf(intervalMinutes, 15L) // WorkManager minimum is 15 minutes

        val request = androidx.work.PeriodicWorkRequest.Builder(
            BackupWorker::class.java,
            repeatInterval,
            TimeUnit.MINUTES
        )
            .addTag("EXCHANGE_ALARM_WORKER")
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "ExchangeAlarmWork",
            androidx.work.ExistingPeriodicWorkPolicy.REPLACE,
            request
        )

        Timber.i("WorkManager scheduled with $repeatInterval minute interval")
        return
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(Intent(this, T::class.java))
        return
    }
    startService(Intent(this, T::class.java))
}
