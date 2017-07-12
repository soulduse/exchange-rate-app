package com.example.soul.exchange_app.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.IBinder;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.example.soul.exchange_app.R;
import com.example.soul.exchange_app.adapter.AlarmAdapter;
import com.example.soul.exchange_app.manager.DataManager;
import com.example.soul.exchange_app.model.AlarmModel;
import com.example.soul.exchange_app.realm.RealmController;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;


/**
 * Created by soul on 2017. 6. 6..
 */

public class AlarmService extends Service {

    private static final String TAG = AlarmAdapter.class.getSimpleName();

    private ScheduledExecutorService reloadScheduler;

    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;
    private NotificationCompat.InboxStyle inboxStyle;
    private RealmController realmController;
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
        realmController = new RealmController();

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
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
        int repeatTime = 0;
        try{
            repeatTime = Integer.parseInt(refreshTime);
        }catch (Exception e){
            Resources res = getApplicationContext().getResources();

            String [] prefGraphArr      = res.getStringArray(R.array.pref_graphOption);
            String [] prefGraphArrValueArr   = res.getStringArray(R.array.pref_refreshOption_values);

            String [] prefRefreshArr    = res.getStringArray(R.array.pref_refreshOption);
            String [] prefRefreshValueArr    = res.getStringArray(R.array.pref_refreshOption_values);


            int foundItemPosition = -1;
            for(int i=0; i<prefRefreshArr.length; i++){
                if(prefRefreshArr[i].equals(refreshTime)){
                    foundItemPosition = i;
                    break;
                }
            }

            repeatTime = Integer.parseInt(prefRefreshValueArr[foundItemPosition]);
        }


        mBuilder    = createNotification();
        reloadScheduler = Executors.newSingleThreadScheduledExecutor();
        reloadScheduler.scheduleAtFixedRate(scheduleJob, 0, repeatTime, TimeUnit.MINUTES);



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
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSmallIcon(R.mipmap.ic_launcher/*스와이프 전 아이콘*/)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis());

        if(alarmSound && alarmVibe) {
            builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        }else if(alarmSound){
//            builder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
            builder.setDefaults(Notification.DEFAULT_SOUND);
        }else if(alarmVibe){
            builder.setDefaults(Notification.DEFAULT_VIBRATE);
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
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
//        realm.close();
    }

    private Runnable scheduleJob = new Runnable() {
        @Override
        public void run() {

            // 데이터 갱신
            DataManager.newInstance(getApplicationContext()).load();

            if(!alarmSwitch){
                return;
            }

            realm = Realm.getDefaultInstance();
            try{
                List<AlarmModel> alarmModelList = realmController.getAlarms(realm);
                int alarmSize = alarmModelList.size();
                Log.d(TAG, "alarm Size : "+alarmSize);

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
                    mBuilder.setContentIntent(createPendingIntent());

                    mNotificationManager.notify(1, mBuilder.build());
                }
            }finally {
                realm.close();
            }
        }
    };

}
