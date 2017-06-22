package com.example.soul.exchange_app.realm;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.example.soul.exchange_app.manager.DataManager;
import com.example.soul.exchange_app.model.AlarmModel;
import com.example.soul.exchange_app.model.CalcuCountries;
import com.example.soul.exchange_app.model.ExchangeRate;
import com.example.soul.exchange_app.model.ParserModel;
import com.example.soul.exchange_app.paser.ExchangeInfo;
import com.example.soul.exchange_app.paser.ExchangeParser;
import com.example.soul.exchange_app.util.DateUtil;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
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

    public RealmController(){}

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
//                Log.d(TAG, "changed datas in realm");
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


    // 알람 등록
    public void addAlarm(final ExchangeRate exchangeRate, final boolean aboveOrBelow, final double price, final int standard, final int position){
        realm.executeTransaction(new Realm.Transaction() {
             @Override
             public void execute(Realm realm) {
                 AlarmModel alarmModel = realm.createObject(AlarmModel.class);
                 alarmModel.setExchangeRate(exchangeRate);
                 alarmModel.setPosition(position);
                 alarmModel.setAboveOrbelow(aboveOrBelow);
                 alarmModel.setPrice(price);
                 alarmModel.setStandardExchange(standard);
                 alarmModel.setAlarmSwitch(true);
             }
        });
    }

    // 알람 수정
    public void updateAlarm(final ExchangeRate exchangeRate, final boolean aboveOrBelow, final double price, final int standard, final int position, final int itemPosition){

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<AlarmModel> alarmModelList = realm.where(AlarmModel.class).findAll();
                AlarmModel alarmModel = alarmModelList.get(itemPosition);
                alarmModel.setExchangeRate(exchangeRate);
                alarmModel.setPosition(position);
                alarmModel.setAboveOrbelow(aboveOrBelow);
                alarmModel.setPrice(price);
                alarmModel.setStandardExchange(standard);
            }
        });
    }

    // 이미 값이 있는지 체크
    public boolean isOverlap(ExchangeRate exchangeRate, boolean aboveOrBelow, double price, int standard){
        AlarmModel alarmModel = realm.where(AlarmModel.class)
                .equalTo("exchangeRate.countryAbbr", exchangeRate.getCountryAbbr())
                .equalTo("aboveOrbelow", aboveOrBelow)
                .equalTo("price", price)
                .equalTo("standardExchange", standard)
                .findFirst();

        return alarmModel != null;
    }

    public RealmResults<AlarmModel> getAlarmModelList(){
        return realm.where(AlarmModel.class).findAll();
    }

    public void deleteAlarm(final int position){
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<AlarmModel> realmResults = realm.where(AlarmModel.class).findAll();
                realmResults.get(position).deleteFromRealm();
            }
        });
    }

    public void turnAlarm(final boolean alarmSwitch, final int position){
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                AlarmModel alarmModel = realm.where(AlarmModel.class).findAll().get(position);
                alarmModel.setAlarmSwitch(alarmSwitch);
            }
        });
    }

    public RealmList<AlarmModel> getAlarms(final Realm inputRealm){
        final RealmList<AlarmModel> realmList = new RealmList<>();

        inputRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                // 알람 설정이 켜져 있는 데이터만 가져온다. (켜져 있는 데이터를 기반으로 알람을 울릴 것인지 판단 한다)
                List<AlarmModel> alarmModelList = inputRealm.where(AlarmModel.class)
                        .equalTo("alarmSwitch", true)
                        .findAll();

                for(AlarmModel alarmModel : alarmModelList) {
                    double setPrice = alarmModel.getPrice();
                    boolean isAbove = alarmModel.isAboveOrbelow();
                    double currentPrice = DataManager.getInstance()
                            .getPrice(alarmModel.getStandardExchange(), alarmModel.getExchangeRate());

                    boolean addAbove = setPrice <= currentPrice;
                    boolean addBelow = setPrice >= currentPrice;

                    // 사용자가 이상을 선택 했고 사용자가 입력한 금액보다 현재 환율금액이 더 큰 경우. (현재가 > 사용자 입력가)
                    if (isAbove && addAbove) {
                        realmList.add(alarmModel);
                    }
                    // 사용자가 이하를 선택 했고 사용자가 입력한 금액보다 현재 환율금액이 더 작은 경우.(현재가 < 사용자 입력가)
                    else if (!isAbove && addBelow) {
                        realmList.add(alarmModel);
                    }
                }
            }
        });

        return realmList;
    }


    public void setExchangeDate(final String [] exchangeDatas){
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                ParserModel parserModel = realm.where(ParserModel.class).findFirst();
                Log.d(TAG, "setExchangeDate parserModel"+parserModel);
                if(parserModel == null) {
                    parserModel = realm.createObject(ParserModel.class);
                }
                parserModel.setDate(DateUtil.getInstance().toDate(exchangeDatas[0]));
                parserModel.setSource(exchangeDatas[1]);
                parserModel.setCount(exchangeDatas[2]);
                parserModel.setNum(Integer.parseInt(exchangeDatas[3]));
            }
        });
    }

    public String getExchangeDate(){
        String resultString = null;
        ParserModel parserModel = realm.where(ParserModel.class).findFirst();
        Log.d(TAG, "parserModel ?? "+parserModel);
        if(parserModel!= null){
            resultString = DateUtil.getInstance().getDate(parserModel.getDate())
                    +" "+parserModel.getSource()+" | 고시회차 "+parserModel.getNum()+"회";
        }

        return resultString;
    }



}
