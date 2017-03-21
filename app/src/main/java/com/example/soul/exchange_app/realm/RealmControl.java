package com.example.soul.exchange_app.realm;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;

import com.example.soul.exchange_app.model.ExchangeRate;
import com.example.soul.exchange_app.model.SetExchangeRate;

import io.realm.Realm;
import io.realm.RealmModel;
import io.realm.RealmResults;


/**
 * Created by soul on 2017. 3. 21..
 */

public class RealmControl {

    private static RealmControl instance;
    private final Realm realm;

    public RealmControl(Application application){
        realm = Realm.getDefaultInstance();
    }

    public static RealmControl with(Fragment fragment){
        if(instance == null){
            instance = new RealmControl(fragment.getActivity().getApplication());
        }
        return instance;
    }

    public static RealmControl with(Activity activity){
        if(instance == null){
            instance = new RealmControl(activity.getApplication());
        }
        return instance;
    }

    public static RealmControl with(Application application){
        if(instance == null){
            instance = new RealmControl(application);
        }
        return instance;
    }

    public static RealmControl getInstance(){
        return instance;
    }

    // Refresh the realm instance
    public void refresh(){
        realm.waitForChange();
    }

    // clear all objects from class
    public void clearAll(Class<? extends RealmModel> clazz){
        realm.beginTransaction();
        realm.delete(clazz);
        realm.commitTransaction();
    }

    // find all objects in the ?.class
    public RealmResults<? extends RealmModel> getExchangeData(Class<? extends RealmModel> clazz){
        return realm.where(clazz).findAll();
    }


    // find all objects in the SetExchangeRate.class
    public RealmResults<SetExchangeRate> getSetExchange(){
        return realm.where(SetExchangeRate.class).findAll();
    }

    // find all objects in the ExchangeRate.class
    public RealmResults<ExchangeRate> getExchangeRate(){
        return realm.where(ExchangeRate.class).findAll();
    }

    //query a single item with the given id
    public Object getSingleData(String id, Class<? extends RealmModel> clazz) {

        return realm.where(clazz).equalTo("id", id).findFirst();
    }

    //check if xx.class is empty
    public boolean hasDatas(Class<? extends RealmModel> clazz) {
        return !realm.where(clazz).findAll().isEmpty();
    }

    //query example
    public RealmResults<? extends RealmModel> queryedDatas(Class<? extends RealmModel> clazz) {

        return realm.where(clazz)
                .contains("author", "Author 0")
                .or()
                .contains("title", "Realm")
                .findAll();
    }

}
