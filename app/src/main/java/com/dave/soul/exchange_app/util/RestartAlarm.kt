package com.dave.soul.exchange_app.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import com.dave.soul.exchange_app.view.activity.RestartService

/**
 * Created by soul on 2017. 8. 17..
 */
class RestartAlarm {

    /**
     * 알람 매니져에 서비스 등록
     */
    fun registerRestartAlarm(context :Context) {
        Log.i("000 AlarmService", "registerRestartAlarm")
        val intent = Intent(context, RestartService::class.java)
        intent.action = "ACTION.RESTART.AlarmService"
        val sender = PendingIntent.getBroadcast(context, 0, intent, 0)

        var firstTime = SystemClock.elapsedRealtime()
        firstTime += (1 * 1000).toLong()

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        /**
         * 알람 등록
         */
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, (1 * 1000).toLong(), sender)
    }

    /**
     * 알람 매니져에 서비스 해제
     */
    fun unregisterRestartAlarm(context :Context) {
        Log.i("000 AlarmService", "unregisterRestartAlarm")
        val intent = Intent(context, RestartService::class.java)
        intent.action = "ACTION.RESTART.AlarmService"
        val sender = PendingIntent.getBroadcast(context, 0, intent, 0)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        /**
         * 알람 취소
         */
        alarmManager.cancel(sender)
    }

    private object Holder { val INSTANCE = RestartAlarm() }

    companion object {
        val instance: RestartAlarm by lazy { Holder.INSTANCE }
    }
}
