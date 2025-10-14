package com.dave.soul.exchange_app.view.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.preference.PreferenceManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.dave.soul.exchange_app.R
import com.dave.soul.exchange_app.manager.DataManager
import com.dave.soul.exchange_app.model.AlarmModel
import com.dave.soul.exchange_app.realm.RealmController
import com.dave.soul.exchange_app.util.SystemUtil
import com.dave.soul.exchange_app.view.activity.MainActivity
import com.dave.soul.exchange_app.view.activity.SettingActivity
import io.realm.Realm

class BackupWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    private val CHANNEL_ID = "channel_id"

    override fun doWork(): Result {
        Log.i(TAG, "BackupWorker doWork() started")

        return try {
            // 1. 데이터 갱신
            DataManager.newInstance(applicationContext).load()
            Log.i(TAG, "Data refreshed")

            // 2. 환율 알림 체크 및 발송
            checkAndSendAlarms()

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in BackupWorker", e)
            Result.failure()
        }
    }

    private fun checkAndSendAlarms() {
        // SharedPreferences에서 알림 설정 가져오기
        val sharedPref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val alarmSwitch = sharedPref.getBoolean(SettingActivity.KEY_PREF_ALARM_SWITCH, false)
        val alarmSound = sharedPref.getBoolean(SettingActivity.KEY_PREF_ALARM_SOUND, false)
        val alarmVibe = sharedPref.getBoolean(SettingActivity.KEY_PREF_ALARM_VIBE, false)

        Log.i(TAG, "Alarm settings - switch: $alarmSwitch, sound: $alarmSound, vibe: $alarmVibe")

        // 알림 꺼져있거나 앱이 포그라운드면 알림 안 보냄
        if (!alarmSwitch || SystemUtil.isAppForground(applicationContext)) {
            Log.i(TAG, "Alarm disabled or app in foreground, skipping notification")
            return
        }

        val realm = Realm.getDefaultInstance()
        try {
            val alarmModelList = RealmController.getAlarms(realm)
            val alarmSize = alarmModelList.size

            if (alarmSize == 0) {
                Log.i(TAG, "No alarms set")
                return
            }

            Log.i(TAG, "Found $alarmSize alarm(s)")

            // 알림 조건에 맞는 데이터 확인
            val resources: Resources = applicationContext.resources
            val titles = resources.getStringArray(R.array.pref_priceOptions)
            val events = mutableListOf<String>()

            for (i in 0 until alarmSize) {
                val alarmModel = alarmModelList[i] ?: continue
                if (!alarmModel.isAlarmSwitch) {
                    continue // 이 알람이 꺼져있으면 스킵
                }

                val exchangeRate = alarmModel.exchangeRate ?: continue
                val abbr = exchangeRate.countryAbbr
                val standard = titles[alarmModel.standardExchange]
                val currentPrice = DataManager.getInstance().getPrice(
                    alarmModel.standardExchange,
                    exchangeRate
                )
                val targetPrice = alarmModel.price
                val isAbove = alarmModel.isAboveOrbelow

                // 조건 체크: 이상/이하 확인
                val conditionMet = if (isAbove) {
                    currentPrice >= targetPrice
                } else {
                    currentPrice <= targetPrice
                }

                if (conditionMet) {
                    val aboveOrBelow = if (isAbove) {
                        applicationContext.getString(R.string.compare_above)
                    } else {
                        applicationContext.getString(R.string.compare_below)
                    }
                    events.add("$abbr $standard : ${currentPrice}원 - ($aboveOrBelow)")
                    Log.i(TAG, "Alarm condition met: ${events.last()}")
                }
            }

            // 조건에 맞는 알람이 있으면 노티피케이션 발송
            if (events.isNotEmpty()) {
                sendNotification(events, alarmSound, alarmVibe)
            } else {
                Log.i(TAG, "No alarm conditions met")
            }
        } finally {
            realm.close()
        }
    }

    private fun sendNotification(events: List<String>, enableSound: Boolean, enableVibe: Boolean) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android O+ 에서 Notification Channel 생성
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "환율 알림",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "설정한 수치에 도달한 환율이 있습니다."
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
                vibrationPattern = longArrayOf(100, 200, 100, 200)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PRIVATE
            }
            notificationManager.createNotificationChannel(channel)
        }

        // InboxStyle로 여러 알림 표시
        val inboxStyle = NotificationCompat.InboxStyle()
        inboxStyle.setBigContentTitle("환율 알림 ${events.size}건")
        inboxStyle.setSummaryText("${events.size}개의 환율 알림 발생")

        events.forEach { event ->
            inboxStyle.addLine(event)
        }

        // PendingIntent 생성
        val resultIntent = Intent(applicationContext, MainActivity::class.java)
        val stackBuilder = TaskStackBuilder.create(applicationContext)
        stackBuilder.addParentStack(MainActivity::class.java)
        stackBuilder.addNextIntent(resultIntent)

        val pendingIntent = stackBuilder.getPendingIntent(
            0,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Notification 생성
        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.icon)
            .setContentTitle("환율")
            .setContentText("환율 알림 ${events.size}건")
            .setSubText("설정한 수치에 도달한 환율이 있습니다.")
            .setStyle(inboxStyle)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setWhen(System.currentTimeMillis())

        // 사운드/진동 설정
        if (enableSound && enableVibe) {
            builder.setDefaults(android.app.Notification.DEFAULT_SOUND or android.app.Notification.DEFAULT_VIBRATE)
        } else if (enableSound) {
            builder.setDefaults(android.app.Notification.DEFAULT_SOUND)
        } else if (enableVibe) {
            builder.setDefaults(android.app.Notification.DEFAULT_VIBRATE)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(android.app.Notification.CATEGORY_MESSAGE)
                .setPriority(android.app.Notification.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        }

        // 노티피케이션 발송
        notificationManager.notify(2130, builder.build())
        Log.i(TAG, "Notification sent with ${events.size} alarm(s)")
    }

    companion object {
        private const val TAG = "BackupWorker"
    }
}
