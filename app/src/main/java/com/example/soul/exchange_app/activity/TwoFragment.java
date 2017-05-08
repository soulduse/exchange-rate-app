package com.example.soul.exchange_app.activity;

//import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.soul.exchange_app.R;
//import com.example.soul.exchange_app.databinding.FragmentTwoBinding;
import com.example.soul.exchange_app.paser.ExchangeInfo;
import com.example.soul.exchange_app.realm.RealmController;

import io.realm.Realm;

/**
 * Created by soul on 2017. 2. 24..
 */

public class TwoFragment  extends Fragment {

    private RealmController realmController;
    private Realm realm;
    private final String TAG = getClass().getSimpleName();
//    private FragmentTwoBinding binding;

    public TwoFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realmController = RealmController.getInstance();
        realm = realmController.getRealm();
        if(realmController.getSizeOfCalcu() == 0){
            realmController.setCalcuCountry(ExchangeInfo.USD, ExchangeInfo.KRW);
        }else{
            String [] counties = realmController.getCalcuCountries();
            realmController.setCalcuCountry(counties[0], counties[1]);
            Log.d(TAG, "getCalcuCountries >> " +counties[0]+"/"+counties[1]);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_two, container, false);
//        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_two, container, false);
//        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}