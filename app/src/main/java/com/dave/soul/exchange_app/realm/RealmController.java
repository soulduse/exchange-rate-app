package com.dave.soul.exchange_app.realm;

import android.util.Log;

import com.dave.soul.exchange_app.manager.DataManager;
import com.dave.soul.exchange_app.model.AlarmModel;
import com.dave.soul.exchange_app.model.CalcuCountries;
import com.dave.soul.exchange_app.model.ExchangeRate;
import com.dave.soul.exchange_app.model.ParserModel;
import com.dave.soul.exchange_app.paser.ExchangeInfo;
import com.dave.soul.exchange_app.util.DateUtil;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmResults;


/**
 * Created by soul on 2017. 3. 21..
 */

public class RealmController {

    private static final String TAG = RealmController.class.getSimpleName();

    private enum FieldNames{
        id, countryAbbr
    }

    // find all objects in the ExchangeRate.class
    public static RealmResults<ExchangeRate> getExchangeRate(Realm realm){
        return realm.where(ExchangeRate.class).findAll();
    }

    public static RealmResults<ExchangeRate> getExchangeRateExceptKorea(Realm realm){
        return realm.where(ExchangeRate.class).not().equalTo(FieldNames.countryAbbr.name(), ExchangeInfo.KRW).findAll();
    }

    public static long findSelectedPositionByAbbr(Realm realm, String abbr){
        return realm.where(ExchangeRate.class)
                .equalTo(FieldNames.countryAbbr.name(), abbr)
                .findFirst().getId();
    }

    // find single object in the ExchangeRate.class
    public static ExchangeRate findExchangeRateByCountryAbbr(Realm realm, String keyword){
        return realm.where(ExchangeRate.class).equalTo(FieldNames.countryAbbr.name(), keyword).findFirst();
    }

    public static RealmResults<ExchangeRate> checkExchangeRate(Realm realm, long id, String countryAbbr){
        return realm.where(ExchangeRate.class)
                .equalTo("id", id)
                .equalTo("countryAbbr", countryAbbr)
                .findAll();
    }

    public static void setRealmDatas(Realm realm, List<ExchangeRate> exchangeRateList){
//        Log.d(TAG, "realm Size >> "+getExchangeRate().size());

        for(ExchangeRate datas : exchangeRateList){
//            Log.d(TAG, "realm object >> "+findExchangeRateByCountryAbbr(datas.getCountryAbbr()));
//            Log.d(TAG, "realm countryAbbr is null? >> "+(findExchangeRateByCountryAbbr(datas.getCountryAbbr()) != null));
            realm.beginTransaction();

            ExchangeRate exchangeRate = findExchangeRateByCountryAbbr(realm, datas.getCountryAbbr());

            // Realm 에 데이터가 없는 상태
            if(exchangeRate == null){
                // add object
                datas.setId(getAutoIncrement(realm, ExchangeRate.class));
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

    private static int getAutoIncrement(Realm realm, Class<? extends RealmModel> clazz){
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
    public static void changeCheckCounties(Realm realm, final boolean isChecked, final String key){
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

    public static void changeAllSelected(Realm realm, final boolean selected){
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

    public static RealmResults<ExchangeRate> getCheckedItems(Realm realm){
        return realm.where(ExchangeRate.class)
                .equalTo("checkState", true)
                .not()
                .equalTo(FieldNames.countryAbbr.name(), ExchangeInfo.KRW)
                .findAll();
    }

    public static int getCheckedItemSize(Realm realm){
        return realm.where(ExchangeRate.class).equalTo("checkState", true).findAll().size();
    }

    // RecyclerView에 아이템중 계산하기를 눌렀을때 fragment2 화면에서는 선택한 나라와 우리나라간에 비교를 기본으로 한다.
    public static void setCalcuCountry(Realm realm, final String countryOne, final String countryTwo){
        Log.w(TAG, "setCalcuCountry 진입");
        realm.executeTransaction(new Realm.Transaction(){

            @Override
            public void execute(Realm realm) {

                Log.w(TAG, "setCalcuCountry execute 진입");
                CalcuCountries calcuCountries;
                if(getSizeOfCalcu(realm) == 0){
                    calcuCountries = realm.createObject(CalcuCountries.class);
                }else{
                    calcuCountries = realm.where(CalcuCountries.class).findFirst();
                }
                calcuCountries.setCalOne(countryOne);
                calcuCountries.setCalTwo(countryTwo);
                if(calcuCountries.getExchangeRates().size() > 0){
                    calcuCountries.getExchangeRates().clear();
                }
                calcuCountries.getExchangeRates().addAll(getExchangeRateEqualToAbbr(realm, getCalcuCountriesName(realm)));

                Log.w(TAG, "결과 >>>> "+calcuCountries.toString());
                Log.w(TAG, "결과 >>>> "+calcuCountries.getExchangeRates().get(0).getCountryAbbr()+" / "+calcuCountries.getExchangeRates().get(1).getCountryAbbr());
                Log.e(TAG, "결과 >>>> "+calcuCountries.getExchangeRates().get(0).getPriceBase()+" / "+calcuCountries.getExchangeRates().get(1).getPriceBase());
            }
        });
    }

    public static int getSizeOfCalcu(Realm realm){
        return realm.where(CalcuCountries.class).findAll().size();
    }

    public static CalcuCountries getCalcuCountries(Realm realm){
        CalcuCountries calcuCountries = realm.where(CalcuCountries.class).findFirst();
        return calcuCountries;
    }

    public static String[] getCalcuCountriesName(Realm realm){
        String[] countries = new String[2];
        CalcuCountries calcuCountries = realm.where(CalcuCountries.class).findFirst();
        countries[0] = calcuCountries.getCalOne();
        countries[1] = calcuCountries.getCalTwo();
        return countries;
    }

    private static RealmResults<ExchangeRate> getExchangeRateEqualToAbbr(Realm realm, String[] countries){
        Log.d(TAG, "countries data : "+countries[0]+" / "+countries[1]);
        return realm.where(ExchangeRate.class)
                .equalTo("countryAbbr", countries[0])
                .or()
                .equalTo("countryAbbr", countries[1])
                .findAll();
    }


    // 알람 등록
    public static void addAlarm(Realm realm, final ExchangeRate exchangeRate, final boolean aboveOrBelow, final double price, final int standard, final int position){
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
    public static void updateAlarm(Realm realm, final ExchangeRate exchangeRate, final boolean aboveOrBelow, final double price, final int standard, final int position, final int itemPosition){

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
    public static boolean isOverlap(Realm realm, ExchangeRate exchangeRate, boolean aboveOrBelow, double price, int standard){
        AlarmModel alarmModel = realm.where(AlarmModel.class)
                .equalTo("exchangeRate.countryAbbr", exchangeRate.getCountryAbbr())
                .equalTo("aboveOrbelow", aboveOrBelow)
                .equalTo("price", price)
                .equalTo("standardExchange", standard)
                .findFirst();

        return alarmModel != null;
    }

    public static RealmResults<AlarmModel> getAlarmModelList(Realm realm){
        return realm.where(AlarmModel.class).findAll();
    }

    public static void deleteAlarm(Realm realm, final int position){
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<AlarmModel> realmResults = realm.where(AlarmModel.class).findAll();
                realmResults.get(position).deleteFromRealm();
            }
        });
    }

    public static void turnAlarm(Realm realm, final boolean alarmSwitch, final int position){
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                AlarmModel alarmModel = realm.where(AlarmModel.class).findAll().get(position);
                alarmModel.setAlarmSwitch(alarmSwitch);
            }
        });
    }

    public static RealmList<AlarmModel> getAlarms(final Realm inputRealm){
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


    public static void setExchangeDate(Realm realm, final String [] exchangeDatas){
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

    public static String getExchangeDate(Realm realm){
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
