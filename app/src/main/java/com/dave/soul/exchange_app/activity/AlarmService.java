package com.dave.soul.exchange_app.activity;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.dave.soul.exchange_app.R;
import com.dave.soul.exchange_app.adapter.AlarmAdapter;
import com.dave.soul.exchange_app.manager.DataManager;
import com.dave.soul.exchange_app.model.AlarmModel;
import com.dave.soul.exchange_app.realm.RealmController;
import com.dave.soul.exchange_app.util.SystemUtil;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;


/**
 * Created by soul on 2017. 6. 6..
 */

public class AlarmService extends Service {

    private static final String TAG = AlarmAdapter.class.getSimpleName();

    private ScheduledExecutorService reloadScheduler;
    private ScheduledFuture scheduledFuture;

    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;
    private NotificationCompat.InboxStyle inboxStyle;
    private String[] titles = null;
    private Realm realm;
    private boolean alarmSwitch;
    private boolean alarmSound;
    private boolean alarmVibe;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        Resources res = getResources();
        titles = res.getStringArray(R.array.pref_priceOptions);

        inboxStyle  = new NotificationCompat.InboxStyle();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        unregisterRestartAlarm();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences sharedPref    = PreferenceManager.getDefaultSharedPreferences(this);
        String showGraphType            = sharedPref.getString(SettingActivity.KEY_PREF_SHOW_GRAPH_TYPE, "");
        String refreshTime              = sharedPref.getString(SettingActivity.KEY_PREF_REFRESH_TIME_TYPE, "");
        alarmSwitch             = sharedPref.getBoolean(SettingActivity.KEY_PREF_ALARM_SWITCH, false);
        alarmSound              = sharedPref.getBoolean(SettingActivity.KEY_PREF_ALARM_SOUND, false);
        alarmVibe               = sharedPref.getBoolean(SettingActivity.KEY_PREF_ALARM_VIBE, false);

        Log.d(TAG, "SharedPreferences values : " +
                "showGraphType : "+showGraphType+", " +
                "refreshTime : "+refreshTime+", " +
                "alarmSwitch : "+alarmSwitch+", " +
                "alarmSound : "+alarmSound+", " +
                "alarmVibe : "+alarmVibe);
        int repeatTime = Integer.parseInt(refreshTime);

        Log.w(TAG, intent.toString()+" / flags : "+flags+" / startId : "+startId);

        mBuilder    = createNotification();

        if(reloadScheduler == null){
            reloadScheduler = Executors.newSingleThreadScheduledExecutor();
            scheduledFuture = reloadScheduler.scheduleAtFixedRate(scheduleJob, 0, repeatTime, TimeUnit.MINUTES);
        }else{
            scheduledFuture.cancel(false);
            scheduledFuture = reloadScheduler.scheduleAtFixedRate(scheduleJob, 0, repeatTime, TimeUnit.MINUTES);
        }

        return START_REDELIVER_INTENT;
    }

    /**
     * 노티피케이션을 누르면 실행되는 기능을 가져오는 노티피케이션
     *
     * 실제 기능을 추가하는 것
     * @return
     */
    private PendingIntent createPendingIntent(){
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
     * @return
     */
    private NotificationCompat.Builder createNotification(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.icon)
                .setSmallIcon(R.mipmap.icon/*스와이프 전 아이콘*/)
                .setAutoCancel(true);


        if(alarmSound && alarmVibe) {
            builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        }else if(alarmSound){
//            builder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
            builder.setDefaults(Notification.DEFAULT_SOUND);
        }else if(alarmVibe){
            builder.setDefaults(Notification.DEFAULT_VIBRATE);
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_MESSAGE)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        return builder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        reloadScheduler.shutdownNow();
        /**
         * 서비스 종료 시 알람 등록을 통해 서비스 재 실행
         */
        registerRestartAlarm();
    }

    private Runnable scheduleJob = new Runnable() {
        @Override
        public void run() {

            // 데이터 갱신
            DataManager.newInstance(getApplicationContext()).load();

//            Log.d(TAG, "isRunningProcess ===> "+SystemUtil.isRunningActivity(getApplicationContext(), "com.example.soul.exchange_app"));
            Log.d(TAG, "isRunningProcess ===> "+SystemUtil.isAppForground(getApplicationContext()));
            if(!alarmSwitch || SystemUtil.isAppForground(getApplicationContext())){
                return;
            }

            realm = Realm.getDefaultInstance();
            try{
                List<AlarmModel> alarmModelList = RealmController.getAlarms(realm);
                int alarmSize = alarmModelList.size();

                // 알림 조건에 맞는 데이터가 있을경우 알림발생 시킴
                if(alarmSize!=0){
                   String[] events = new String[alarmSize];

                    for(int i=0; i<alarmSize; i++){
                        AlarmModel alarmModel = alarmModelList.get(i);
                        String abbr             = alarmModel.getExchangeRate().getCountryAbbr();
                        String standard         = titles[alarmModel.getStandardExchange()];
                        double currentPrice     = DataManager.getInstance().getPrice(alarmModel.getStandardExchange(), alarmModel.getExchangeRate());
                        String aboveOrBelow     = alarmModel.isAboveOrbelow() ? getString(R.string.compare_above) : getString(R.string.compare_below);

                        events[i] = abbr+" "+standard+" : "+currentPrice+"원 - ("+aboveOrBelow+")";
                        Log.d(TAG, "Event text : "+events[i]);
                    }

                    inboxStyle  = new NotificationCompat.InboxStyle();
                    inboxStyle.setBigContentTitle("환율 알림 "+alarmSize+"건");
                    inboxStyle.setSummaryText(alarmSize+"개의 환율 알림 발생");

                    for (String str : events) {
                        inboxStyle.addLine(str);
                    }
                    //스타일 추가
                    mBuilder.setStyle(inboxStyle);
                    mBuilder.setContentTitle("환율");
                    mBuilder.setContentText("환율 알림 "+alarmSize+"건");
                    mBuilder.setSubText("설정한 수치에 도달한 환율이 있습니다.");
                    mBuilder.setContentIntent(createPendingIntent());
                    mBuilder.setWhen(System.currentTimeMillis());
//                    startForeground(0, mBuilder.build());
                    startForeground(1 , new Notification());
                    mNotificationManager.notify(2130, mBuilder.build());
                    mNotificationManager.cancel(1);
                    stopSelf(1);
//                    stopForeground(false);
                }
            }finally {
                realm.close();
            }
        }
    };




    /**
     * 알람 매니져에 서비스 등록
     */
    private void registerRestartAlarm(){
        Log.i("000 AlarmService" , "registerRestartAlarm" );
        Intent intent = new Intent(AlarmService.this,RestartService.class);
        intent.setAction("ACTION.RESTART.AlarmService");
        PendingIntent sender = PendingIntent.getBroadcast(AlarmService.this,0,intent,0);

        long firstTime = SystemClock.elapsedRealtime();
        firstTime += 1*1000;

        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);

        /**
         * 알람 등록
         */
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,firstTime,1*1000,sender);

    }

    /**
     * 알람 매니져에 서비스 해제
     */
    private void unregisterRestartAlarm(){
        Log.i("000 AlarmService" , "unregisterRestartAlarm" );
        Intent intent = new Intent(AlarmService.this,RestartService.class);
        intent.setAction("ACTION.RESTART.AlarmService");
        PendingIntent sender = PendingIntent.getBroadcast(AlarmService.this,0,intent,0);

        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);

        /**
         * 알람 취소
         */
        alarmManager.cancel(sender);
    }

}
