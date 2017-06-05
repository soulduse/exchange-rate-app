package com.example.soul.exchange_app.realm;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.content.Context;
import android.util.Log;

import com.example.soul.exchange_app.model.AlarmModel;
import com.example.soul.exchange_app.model.CalcuCountries;
import com.example.soul.exchange_app.model.ExchangeRate;
import com.example.soul.exchange_app.paser.ExchangeInfo;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmModel;
import io.realm.RealmResults;


/**
 * Created by soul on 2017. 3. 21..
 */

public class RealmController {

    private final String TAG = getClass().getSimpleName();
    private static RealmController instance;
    private Realm realm;

    private enum FieldNames{
        id, countryAbbr
    }

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

    public void setRealm(){
        realm = Realm.getDefaultInstance();
    }

    public Realm getRealm() {
        return realm;
    }

    // Refresh the realm instance
    public void refresh(){
        realm.waitForChange();
    }

    public void close(){
        realm.close();
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

    // find all objects in the ExchangeRate.class
    public RealmResults<ExchangeRate> getExchangeRate(){
        return realm.where(ExchangeRate.class).findAll();
    }

    public RealmResults<ExchangeRate> getExchangeRateExceptKorea(){
        return realm.where(ExchangeRate.class).not().equalTo(FieldNames.countryAbbr.name(), ExchangeInfo.KRW).findAll();
    }

    // find single object in the ExchangeRate.class
    public ExchangeRate isExchangeRate(String keyword){
        return realm.where(ExchangeRate.class).equalTo(FieldNames.countryAbbr.name(), keyword).findFirst();
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

    public void setRealmDatas(List<ExchangeRate> exchangeRateList){
//        Log.d(TAG, "realm Size >> "+getExchangeRate().size());

        for(ExchangeRate datas : exchangeRateList){
//            Log.d(TAG, "realm object >> "+isExchangeRate(datas.getCountryAbbr()));
//            Log.d(TAG, "realm countryAbbr is null? >> "+(isExchangeRate(datas.getCountryAbbr()) != null));
            realm.beginTransaction();

            ExchangeRate exchangeRate = isExchangeRate(datas.getCountryAbbr());

            // Realm 에 데이터가 없는 상태
            if(exchangeRate == null){
                // add object
                datas.setId(getAutoIncrement(ExchangeRate.class));
                realm.copyToRealm(datas);
            }
            // realm 에 기존의 데이터가 있는 상태로 데이터를 갱신한다.
            else{
                exchangeRate.setPriceBase(datas.getPriceBase());
                exchangeRate.setPriceSell(datas.getPriceSell());
                exchangeRate.setPriceBuy(datas.getPriceBuy());
                exchangeRate.setPriceSend(datas.getPriceSend());
                exchangeRate.setPriceReceive(datas.getPriceReceive());
                exchangeRate.setPriceusExchange(datas.getPriceusExchange());
                Log.d(TAG, "changed datas in realm");
            }
            realm.commitTransaction();
        }
    }

    private int getAutoIncrement(Class<? extends RealmModel> clazz){
        Number currentIdNum = realm.where(clazz).max("id");
        int nextId;
        if(currentIdNum == null) {
            nextId = 1;
        } else {
            nextId = currentIdNum.intValue() + 1;
        }
        Log.d(TAG, "autoIncrement Number : "+nextId);
        return nextId;
    }

    // 사용자가 클릭한 CheckBox 값 바꾸기
    public void changeCheckCounties(final boolean isChecked, final String key){
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                String []keyArray = key.split(" ");
                ExchangeRate exchangeRate = realm.where(ExchangeRate.class).
                        equalTo("countryAbbr", keyArray[0]).
                        equalTo("countryName", keyArray[1]).
                        findFirst();
                exchangeRate.setCheckState(isChecked);
            }
        });
    }

    public void changeAllSelected(final boolean selected){
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                List<ExchangeRate> exchangeRateList = realm.where(ExchangeRate.class).
                        equalTo("checkState", !selected).findAll();
                for(ExchangeRate datas : exchangeRateList){
                    datas.setCheckState(selected);
                }
            }
        });
    }

    public RealmResults<ExchangeRate> getCheckedItems(){
        return realm.where(ExchangeRate.class)
                .equalTo("checkState", true)
                .not()
                .equalTo(FieldNames.countryAbbr.name(), ExchangeInfo.KRW)
                .findAll();
    }

    public int getCheckedItemSize(){
        return realm.where(ExchangeRate.class).equalTo("checkState", true).findAll().size();
    }

    // RecyclerView에 아이템중 계산하기를 눌렀을때 fragment2 화면에서는 선택한 나라와 우리나라간에 비교를 기본으로 한다.
    public void setCalcuCountry(final String countryOne, final String countryTwo){
        Log.w(TAG, "setCalcuCountry 진입");
        realm.executeTransaction(new Realm.Transaction(){

            @Override
            public void execute(Realm realm) {

                Log.w(TAG, "setCalcuCountry execute 진입");
                CalcuCountries calcuCountries;
                if(getSizeOfCalcu() == 0){
                    calcuCountries = realm.createObject(CalcuCountries.class);
                }else{
                    calcuCountries = realm.where(CalcuCountries.class).findFirst();
                }
                calcuCountries.setCalOne(countryOne);
                calcuCountries.setCalTwo(countryTwo);
                if(calcuCountries.getExchangeRates().size() > 0){
                    calcuCountries.getExchangeRates().clear();
                }
                calcuCountries.getExchangeRates().addAll(getExchangeRateEqualToAbbr(getCalcuCountriesName()));

                Log.w(TAG, "결과 >>>> "+calcuCountries.toString());
                Log.w(TAG, "결과 >>>> "+calcuCountries.getExchangeRates().get(0).getCountryAbbr()+" / "+calcuCountries.getExchangeRates().get(1).getCountryAbbr());

            }
        });
    }

    public int getSizeOfCalcu(){
        return realm.where(CalcuCountries.class).findAll().size();
    }

    public CalcuCountries getCalcuCountries(){
        CalcuCountries calcuCountries = realm.where(CalcuCountries.class).findFirst();
        return calcuCountries;
    }

    public String[] getCalcuCountriesName(){
        String[] countries = new String[2];
        CalcuCountries calcuCountries = realm.where(CalcuCountries.class).findFirst();
        countries[0] = calcuCountries.getCalOne();
        countries[1] = calcuCountries.getCalTwo();
        return countries;
    }

    private RealmResults<ExchangeRate> getExchangeRateEqualToAbbr(String[] countries){
        Log.d(TAG, "countries data : "+countries[0]+" / "+countries[1]);
        return realm.where(ExchangeRate.class)
                .equalTo("countryAbbr", countries[0])
                .or()
                .equalTo("countryAbbr", countries[1])
                .findAll();
    }


    public void addAlarm(final ExchangeRate exchangeRate, final boolean aboveOrBelow, final double price, final int standard){

        realm.executeTransaction(new Realm.Transaction() {
             @Override
             public void execute(Realm realm) {
                 AlarmModel alarmModel = realm.createObject(AlarmModel.class);
                 alarmModel.setExchangeRate(exchangeRate);
                 alarmModel.setAboveOrbelow(aboveOrBelow);
                 alarmModel.setPrice(price);
                 alarmModel.setStandardExchange(standard);
             }
        });
    }

}
