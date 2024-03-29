package com.dave.soul.exchange_app.view.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dave.soul.exchange_app.R;
import com.dave.soul.exchange_app.realm.RealmController;
import com.dave.soul.exchange_app.view.adapter.AlarmAdapter;

import io.realm.Realm;

/**
 * Created by soul on 2017. 2. 24..
 */

public class ThreeFragment  extends Fragment {

    private static final String TAG = ThreeFragment.class.getSimpleName();
    private Realm realm;
    private TextView textNoneAlarm;
    private AlarmAdapter adapter;

    public ThreeFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        realm = Realm.getDefaultInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_three, container, false);

        textNoneAlarm = (TextView)view.findViewById(R.id.text_notice_none_alarms);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.alarmRecyclerView);
        adapter = new AlarmAdapter(RealmController.getAlarmModelList(realm), true, getContext(), getFragmentManager());

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getContext(), 1);
        mLayoutManager.supportsPredictiveItemAnimations();
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);
        visibleTextNoneItems();
        return view;
    }

    private void visibleTextNoneItems(){
        if(adapter.getItemCount() == 0){
            textNoneAlarm.setVisibility(View.VISIBLE);
        }else{
            textNoneAlarm.setVisibility(View.GONE);
        }

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                if(itemCount > 0){
                    textNoneAlarm.setVisibility(View.GONE);
                }
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                if(adapter.getItemCount() == 0){
                    textNoneAlarm.setVisibility(View.VISIBLE);
                }
            }
        });

        Log.d(TAG, "end visibleTextNoneItems");
    }

    @Override
    public void onDestroyView() {
        realm.close();
        super.onDestroyView();
    }
}
