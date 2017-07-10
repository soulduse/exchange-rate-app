package com.example.soul.exchange_app.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.soul.exchange_app.R;
import com.example.soul.exchange_app.adapter.AlarmAdapter;
import com.example.soul.exchange_app.realm.RealmController;

import io.realm.Realm;

/**
 * Created by soul on 2017. 2. 24..
 */

public class ThreeFragment  extends Fragment {

    private static final String TAG = ThreeFragment.class.getSimpleName();
    private RealmController realmController;
    private Realm realm;

    public ThreeFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        realmController = RealmController.getInstance();
        realmController.setRealm();
        realm = realmController.getRealm();
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

    @Override
    public void onDestroyView() {
        realm.close();
        super.onDestroyView();
    }
}
