package com.example.soul.exchange_app.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.example.soul.exchange_app.R;
import com.example.soul.exchange_app.model.ExchangeRate;
import com.example.soul.exchange_app.model.SetExchangeRate;
import com.example.soul.exchange_app.realm.RealmController;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by soul on 2017. 3. 20..
 */

public class SetCountryActivity extends Activity {

    private final String TAG = getClass().getSimpleName();
    private RecyclerView recyclerView;
    private Realm realm;

    private SetExchangeRate setExchangeRate;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_country);

        realm = RealmController.with(this).getRealm();
        recyclerView = (RecyclerView)findViewById(R.id.recycler_view_frag_set);

        initRealm();
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
        RealmController.getInstance().clearAll(SetExchangeRate.class);
        RealmResults<ExchangeRate> rates = RealmController.getInstance().getExchangeRate();
        Log.d(TAG, "realm ExchangeRate data size : "+rates.size());

        for(int i=0; i<rates.size(); i++){
            long idx = RealmController.getInstance().getNextKey(SetExchangeRate.class);
            ExchangeRate results = RealmController.getInstance().isExchangeRate(idx, rates.get(i).getCountryAbbr());
            setExchangeRate = new SetExchangeRate();
            setExchangeRate.setId(idx);
            setExchangeRate.setCountryAbbr(rates.get(i).getCountryAbbr());
            setExchangeRate.setCountryName(rates.get(i).getCountryName());
            setExchangeRate.setThumbnail(rates.get(i).getThumbnail());

            realm.beginTransaction();
            if(results != null){
                realm.copyToRealm(setExchangeRate);
            }else{
                setExchangeRate.setId(setExchangeRate.getId());
                setExchangeRate.setCountryAbbr(setExchangeRate.getCountryAbbr());
                setExchangeRate.setCountryName(setExchangeRate.getCountryName());
                setExchangeRate.setThumbnail(setExchangeRate.getCountryName());
            }
            realm.commitTransaction();
        }

        RealmResults<SetExchangeRate> setRates = RealmController.getInstance().getSetExchange();
        Log.e(TAG, "realm SetExchangeRate data size : "+setRates.size());
    }
}
