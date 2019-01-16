package com.dave.soul.exchange_app.view.activity

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

import com.dave.soul.exchange_app.view.service.AlarmService
import org.jetbrains.anko.startService

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

inline fun <reified T: Service> Context.serviceStart() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(Intent(this, T::class.java))
        return
    }
    startService<T>()
}
