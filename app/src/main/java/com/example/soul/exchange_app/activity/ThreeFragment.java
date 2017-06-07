package com.example.soul.exchange_app.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.soul.exchange_app.R;
import com.example.soul.exchange_app.adapter.AlarmAdapter;
import com.example.soul.exchange_app.adapter.SetCountryAdapter;
import com.example.soul.exchange_app.realm.RealmController;

import java.util.List;

/**
 * Created by soul on 2017. 2. 24..
 */

public class ThreeFragment  extends Fragment {

    private RealmController realmController;
    private static final String TAG = ThreeFragment.class.getSimpleName();

    public ThreeFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        realmController = RealmController.getInstance();
        realmController.setRealm();


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_three, container, false);

//        TextView noticeTextView = (TextView)view.findViewById(R.id.noticeText);
//        noticeTextView.setText(R.string.notice_add_alarm);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.alarmRecyclerView);
        AlarmAdapter adapter = new AlarmAdapter(realmController.getAlarmModelList(), true, getContext(), getFragmentManager());
//        if(adapter.getItemCount() <= 0){
//            noticeTextView.setVisibility(View.VISIBLE);
//        }else{
//            noticeTextView.setVisibility(View.INVISIBLE);
//        }

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getContext(), 1);
        mLayoutManager.supportsPredictiveItemAnimations();
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);

        return view;
    }
}
