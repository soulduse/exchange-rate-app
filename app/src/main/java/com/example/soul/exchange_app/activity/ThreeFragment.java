package com.example.soul.exchange_app.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.soul.exchange_app.R;
import com.example.soul.exchange_app.adapter.AlarmAdapter;
import com.example.soul.exchange_app.adapter.SetCountryAdapter;
import com.example.soul.exchange_app.realm.RealmController;

/**
 * Created by soul on 2017. 2. 24..
 */

public class ThreeFragment  extends Fragment {

    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private RealmController realmController;

    public ThreeFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        realmController = RealmController.getInstance();
        realmController.setRealm();

        /*
        Intent intent = new Intent(getContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(),
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationBuilder = new NotificationCompat.Builder(getContext())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("오늘의 환율")
                .setContentText("미국 달러 상승")
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentIntent(pendingIntent);

//        button = (Button)view.findViewById(R.id.noti_test);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                notificationManager.notify(0, notificationBuilder.build());
//            }
//        });

        */
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_three, container, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.alarmRecyclerView);
        AlarmAdapter adapter = new AlarmAdapter(realmController.getAlarmModelList(), true, getContext());

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getContext(), 1);
        mLayoutManager.supportsPredictiveItemAnimations();
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);

        return view;
    }
}
