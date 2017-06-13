package com.example.soul.exchange_app.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;


/**
 * Created by soul on 2017. 6. 6..
 */

public class AlarmService extends Service {

    private static final String TAG = AlarmAdapter.class.getSimpleName();
    private Timer jobScheduler;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;
    private NotificationCompat.InboxStyle inboxStyle;
    private RealmController realmController;
    private String[] titles = null;
    private Realm realm;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        Resources res = getResources();
        titles = res.getStringArray(R.array.price_options);

        mBuilder    = createNotification();
        inboxStyle  = new NotificationCompat.InboxStyle();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        realmController = new RealmController();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ScheduledExecutorService reloadScheduler = Executors.newSingleThreadScheduledExecutor();
        reloadScheduler.scheduleAtFixedRate(scheduleJob, 0, 30, TimeUnit.SECONDS);

//        ScheduledJob job = new ScheduledJob();
//        jobScheduler = new Timer();
//        jobScheduler.scheduleAtFixedRate(job, 1000, 10000);


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
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(icon)
                .setContentTitle("StatusBar Title")
                .setContentText("StatusBar subTitle")
                .setSmallIcon(R.mipmap.ic_launcher/*스와이프 전 아이콘*/)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_ALL);
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
        jobScheduler.cancel();
        jobScheduler = null;
        realm.close();
    }

    Runnable scheduleJob = new Runnable() {
        @Override
        public void run() {
            // 데이터 갱신
//            DataManager.newInstance(getApplicationContext()).load();
            realm = Realm.getDefaultInstance();
            try{
                List<AlarmModel> alarmModelList = realmController.getAlarms(realm);
                int alarmSize = alarmModelList.size();
                String[] events = new String[alarmSize];

                for(int i=0; i<alarmSize; i++){
                    AlarmModel alarmModel = alarmModelList.get(i);
                    String abbr             = alarmModel.getExchangeRate().getCountryAbbr();
                    String standard         = titles[alarmModel.getStandardExchange()];
                    double currentPrice     = DataManager.newInstance().getPrice(alarmModel.getStandardExchange(), alarmModel.getExchangeRate());
                    String aboveOrBelow     = alarmModel.isAboveOrbelow() ? getString(R.string.compare_above) : getString(R.string.compare_below);

                    events[i] = abbr+" "+standard+" : "+currentPrice+"원 - ("+aboveOrBelow+")";
                }


                inboxStyle.setBigContentTitle("Event tracker details:");
                inboxStyle.setSummaryText("Events summary");
                for (String str : events) {
                    inboxStyle.addLine(str);
                }
                //스타일 추가
                mBuilder.setStyle(inboxStyle);
                mBuilder.setContentIntent(createPendingIntent());


                mNotificationManager.notify(1, mBuilder.build());
            }finally {
                realm.close();
            }
        }
    };
    
}
