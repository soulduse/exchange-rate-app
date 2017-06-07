package com.example.soul.exchange_app.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.soul.exchange_app.R;
import com.example.soul.exchange_app.adapter.AlarmAdapter;
import com.example.soul.exchange_app.manager.DataManager;
import com.example.soul.exchange_app.model.ExchangeRate;
import com.example.soul.exchange_app.paser.ExchangeParser;
import com.example.soul.exchange_app.realm.RealmController;
import com.example.soul.exchange_app.util.NetworkUtil;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;


/**
 * Created by soul on 2017. 6. 6..
 */

public class AlarmService extends Service {

    private static final String TAG = AlarmAdapter.class.getSimpleName();
    private Timer jobScheduler;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private RealmController realmController;

    // data
    private DataManager dataManager;

    public AlarmService() {
        super();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        realmController = RealmController.getInstance();
        realmController.setRealm();
        dataManager = new DataManager();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ScheduledJob job = new ScheduledJob();
        jobScheduler = new Timer();
        jobScheduler.scheduleAtFixedRate(job, 1000, 10000);

        Intent goMain = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, goMain, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("오늘의 환율")
                .setContentText("미국 달러 상승")
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentIntent(pendingIntent);

        // 조건문 필요
//        notificationManager.notify(0, notificationBuilder.build());


        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        jobScheduler.cancel();
        jobScheduler = null;
        realmController.close();
    }

    class ScheduledJob extends TimerTask {

        public void run() {
            Log.d(TAG, new Date().toString());
            load();
        }


        /**
         네트워크 연결상태에 따른 어디서 데이터를 가져올 것인가에 대한 구분 (두 가지 경우의 수가 있다.)
         - network connect       : parsing data를 가져온다.
         - network disconnect    : Realm DB에서 내용을 가져온다.
         */
        public void load() {
            if(NetworkUtil.isNetworkConnected(getApplicationContext())){
                Callable<List<ExchangeRate>> callable = new Callable<List<ExchangeRate>>() {
                    @Override
                    public List<ExchangeRate> call() throws Exception {
                        return getParserDataList();
                    }
                };

                dataManager.getAsyncExecutor()
                        .setCallable(callable)
                        .setCallback(callback)
                        .execute();
            }
        }

        private List<ExchangeRate> getParserDataList(){
            return new ExchangeParser().getParserDatas();
        }


        // 비동기로 실행된 결과를 받아 처리하는 코드
        private DataManager.AsyncCallback<List<ExchangeRate>> callback = new DataManager.AsyncCallback<List<ExchangeRate>>() {
            @Override
            public void onResult(List<ExchangeRate> result) {
                realmController.setRealmDatas(result);
                Log.d(TAG, "realmController.getExchangeRate() : "+realmController.getExchangeRate().toString());
            }

            @Override
            public void exceptionOccured(Exception e) {
                Log.d(TAG, "exceptionOccured : "+e.getMessage());
            }

            @Override
            public void cancelled() {
                Log.d(TAG, "cancelled");
            }
        };
    }
}
