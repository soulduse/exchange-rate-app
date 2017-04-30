package com.example.soul.exchange_app.realm;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.content.Context;

import com.example.soul.exchange_app.model.ExchangeRate;
import com.example.soul.exchange_app.model.SetExchangeRate;

import io.realm.Realm;
import io.realm.RealmModel;
import io.realm.RealmResults;


/**
 * Created by soul on 2017. 3. 21..
 */

public class RealmController {

    private static RealmController instance;
    private final Realm realm;



    public RealmController(Context context){
        Realm.init(context);
        realm = Realm.getDefaultInstance();
    }

    public static RealmController with(Context context){
        if(instance == null){
            instance = new RealmController(context);
        }
        return instance;
    }

    public static RealmController with(Fragment fragment){
        if(instance == null){
            instance = new RealmController(fragment.getActivity());
        }
        return instance;
    }

    public static RealmController with(Activity activity){
        if(instance == null){
            instance = new RealmController(activity.getApplication());
        }
        return instance;
    }

    public static RealmController with(Application application){
        if(instance == null){
            instance = new RealmController(application);
        }
        return instance;
    }

    public static RealmController getInstance(){
        return instance;
    }

    public Realm getRealm() {
        return realm;
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
    public Object getSingleData(long id, Class<? extends RealmModel> clazz) {

        return realm.where(clazz).equalTo("id", id).findFirst();
    }

    public RealmResults<ExchangeRate> checkExchangeRate(long id, String countryAbbr){
        return realm.where(ExchangeRate.class)
                .equalTo("id", id)
                .equalTo("countryAbbr", countryAbbr)
                .findAll();
    }

    public ExchangeRate isExchangeRate(long id, String countryAbbr){
        return realm.where(ExchangeRate.class)
                .not()
                .beginGroup()
                    .equalTo("id", id)
                    .equalTo("countryAbbr", countryAbbr)
                .endGroup()
                .findFirst();
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

    public long getNextKey(Class<? extends RealmModel> clazz)
    {
        try {
            return realm.where(clazz).max("id").longValue() + 1;
        } catch (ArrayIndexOutOfBoundsException e) {
            return 0;
        } catch (NullPointerException e){
            return 0;
        }
    }

}
