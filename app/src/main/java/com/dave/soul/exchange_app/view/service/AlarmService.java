package com.dave.soul.exchange_app.view.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.dave.soul.exchange_app.R;
import com.dave.soul.exchange_app.view.activity.MainActivity;
import com.dave.soul.exchange_app.view.activity.SettingActivity;
import com.dave.soul.exchange_app.view.adapter.AlarmAdapter;
import com.dave.soul.exchange_app.manager.DataManager;
import com.dave.soul.exchange_app.model.AlarmModel;
import com.dave.soul.exchange_app.realm.RealmController;
import com.dave.soul.exchange_app.util.RestartAlarm;
import com.dave.soul.exchange_app.util.SystemUtil;

import java.util.List;

import io.realm.Realm;


/**
 * Created by soul on 2017. 6. 6..
 */

public class AlarmService extends Service {

    private static final String TAG = AlarmAdapter.class.getSimpleName();
    private static final int MILLISINFUTURE = 86400 * 1000;
    private static final String CHANNEL_ID = "channel_id";

    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;
    private NotificationCompat.InboxStyle inboxStyle;
    private String[] titles = null;
    private Realm realm;
    private boolean alarmSwitch;
    private boolean alarmSound;
    private boolean alarmVibe;
    private int countInterval;

    private CountDownTimer countDownTimer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        RestartAlarm.Companion.getInstance().unregisterRestartAlarm(this);
        super.onCreate();

        initData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        countDownTimer.cancel();
        /**
         * 서비스 종료 시 알람 등록을 통해 서비스 재 실행
         */
        RestartAlarm.Companion.getInstance().registerRestartAlarm(this);
    }

    /**
     * 데이터 초기화
     */
    private void initData() {
        initPreference();
        initNotification();
        countDownTimer();
        countDownTimer.start();
    }

    private void initPreference() {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        Resources res = getResources();
        titles = res.getStringArray(R.array.pref_priceOptions);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String showGraphType = sharedPref.getString(SettingActivity.KEY_PREF_SHOW_GRAPH_TYPE, "");
        String refreshTime = sharedPref.getString(SettingActivity.KEY_PREF_REFRESH_TIME_TYPE, "");
        alarmSwitch = sharedPref.getBoolean(SettingActivity.KEY_PREF_ALARM_SWITCH, false);
        alarmSound = sharedPref.getBoolean(SettingActivity.KEY_PREF_ALARM_SOUND, false);
        alarmVibe = sharedPref.getBoolean(SettingActivity.KEY_PREF_ALARM_VIBE, false);

        Log.d(TAG, "SharedPreferences values : " +
                "showGraphType : " + showGraphType + ", " +
                "refreshTime : " + refreshTime + ", " +
                "alarmSwitch : " + alarmSwitch + ", " +
                "alarmSound : " + alarmSound + ", " +
                "alarmVibe : " + alarmVibe);
        countInterval = Integer.parseInt(refreshTime) * 1000 * 60;
    }

    private void initNotification() {
        inboxStyle = new NotificationCompat.InboxStyle();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotificationManager.createNotificationChannel(getNotificationChannel());
        }
        mBuilder = createNotification();
    }

    private void countDownTimer() {
        countDownTimer = new CountDownTimer(MILLISINFUTURE, countInterval) {
            @Override
            public void onTick(long millisUntilFinished) {
                // 데이터 갱신
                DataManager.newInstance(getApplicationContext()).load();

                Log.d(TAG, "isRunningProcess ===> " + SystemUtil.isAppForground(getApplicationContext()));
                if (!alarmSwitch || SystemUtil.isAppForground(getApplicationContext())) {
                    return;
                }

                realm = Realm.getDefaultInstance();
                try {
                    List<AlarmModel> alarmModelList = RealmController.getAlarms(realm);
                    int alarmSize = alarmModelList.size();

                    // 알림 조건에 맞는 데이터가 있을경우 알림발생 시킴
                    if (alarmSize != 0) {
                        String[] events = new String[alarmSize];

                        for (int i = 0; i < alarmSize; i++) {
                            AlarmModel alarmModel = alarmModelList.get(i);
                            String abbr = alarmModel.getExchangeRate().getCountryAbbr();
                            String standard = titles[alarmModel.getStandardExchange()];
                            double currentPrice = DataManager.getInstance().getPrice(alarmModel.getStandardExchange(), alarmModel.getExchangeRate());
                            String aboveOrBelow = alarmModel.isAboveOrbelow() ? getString(R.string.compare_above) : getString(R.string.compare_below);

                            events[i] = abbr + " " + standard + " : " + currentPrice + "원 - (" + aboveOrBelow + ")";
                            Log.d(TAG, "Event text : " + events[i]);
                        }

                        inboxStyle = new NotificationCompat.InboxStyle();
                        inboxStyle.setBigContentTitle("환율 알림 " + alarmSize + "건");
                        inboxStyle.setSummaryText(alarmSize + "개의 환율 알림 발생");

                        for (String str : events) {
                            inboxStyle.addLine(str);
                        }
                        //스타일 추가
                        mBuilder.setStyle(inboxStyle);
                        mBuilder.setContentTitle("환율");
                        mBuilder.setContentText("환율 알림 " + alarmSize + "건");
                        mBuilder.setSubText("설정한 수치에 도달한 환율이 있습니다.");
                        mBuilder.setContentIntent(createPendingIntent());
                        mBuilder.setWhen(System.currentTimeMillis());
                        mNotificationManager.notify(2130, mBuilder.build());
                    }
                } finally {
                    realm.close();
                }
            }

            @Override
            public void onFinish() {
                countDownTimer();
                countDownTimer.start();
            }
        };
    }

    @TargetApi(Build.VERSION_CODES.O)
    private NotificationChannel getNotificationChannel() {
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "환율", NotificationManager.IMPORTANCE_DEFAULT);
        notificationChannel.setDescription("설정한 수치에 도달한 환율이 있습니다.");
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.BLUE);
        notificationChannel.enableVibration(true);
        notificationChannel.setVibrationPattern(new long[]{100, 200, 100, 200});
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        return notificationChannel;
    }

    /**
     * 노티피케이션을 누르면 실행되는 기능을 가져오는 노티피케이션
     * <p>
     * 실제 기능을 추가하는 것
     *
     * @return
     */
    private PendingIntent createPendingIntent() {
        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        return stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    /**
     * 노티피케이션 빌드
     *
     * @return
     */
    private NotificationCompat.Builder createNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.icon)
                .setSmallIcon(R.mipmap.icon/*스와이프 전 아이콘*/)
                .setAutoCancel(true);

        if (alarmSound && alarmVibe) {
            builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        } else if (alarmSound) {
            builder.setDefaults(Notification.DEFAULT_SOUND);
        } else if (alarmVibe) {
            builder.setDefaults(Notification.DEFAULT_VIBRATE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_MESSAGE)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setVisibility(Notification.VISIBILITY_PUBLIC);
        }
        return builder;
    }
}
