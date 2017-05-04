package com.example.soul.exchange_app.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.example.soul.exchange_app.R;
import com.example.soul.exchange_app.adapter.SetCountryAdapter;
import com.example.soul.exchange_app.model.ExchangeRate;
import com.example.soul.exchange_app.model.SetExchangeRate;
import com.example.soul.exchange_app.realm.RealmController;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by soul on 2017. 3. 20..
 */

public class SetCountryActivity extends Activity {

    private final String TAG = getClass().getSimpleName();
    private RecyclerView recyclerView;
    private SetCountryAdapter adapter;
    private Realm realm;
    private RealmController realmController;
    private List<SetExchangeRate> setExchangeRateList;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_country);

        realm = Realm.getDefaultInstance();
        realmController = RealmController.getInstance();

        recyclerView = (RecyclerView)findViewById(R.id.recycler_view_frag_set);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 1);
        mLayoutManager.supportsPredictiveItemAnimations();
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new SetCountryAdapter(realmController.getSetExchange(), getApplicationContext());
        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);
//        initRealm();
//        setAdapter();
    }


    /**
     * [ realm 관련 사항 ]
     *
     * 해결 해야될 문제
     * 1. setExchangeRate 데이터의 경우 한번만 주입 하면된다.
     * 2. 중복값일 경우 추가하지 않도록 변경
     *
     * 현재 문제점
     * 1. 임의로 데이터를 모두 삭제한 뒤 다시 insert하는 형식이라 비효율 적임.
     */
    private void initRealm(){

//        Log.e(TAG, "realm setExchangeRateList data size : "+setExchangeRateList.size());
    }


    private void setAdapter(){
        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
