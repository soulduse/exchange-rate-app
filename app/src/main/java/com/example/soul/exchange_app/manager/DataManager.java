package com.example.soul.exchange_app.manager;

import android.content.Context;
import android.util.Log;

import com.example.soul.exchange_app.activity.MainActivity;
import com.example.soul.exchange_app.model.ExchangeRate;
import com.example.soul.exchange_app.paser.ExchangeParser;
import com.example.soul.exchange_app.realm.RealmControllerU;
import com.example.soul.exchange_app.util.NetworkUtil;

import java.util.List;
import java.util.concurrent.Callable;

import io.realm.Realm;


/**
 * Created by soul on 2017. 6. 7..
 * 파싱한 데이터를
 */

public class DataManager {

    private static final String TAG = DataManager.class.getSimpleName();

    // data
    private ParserManager parserManager;

    // Realm
    private Realm realm;

    private Context context;

    private ExchangeParser exchangeParser;

    private static final DataManager dataManager = new DataManager();

    private DataManager(Context context){
        this.context = context;
        parserManager = new ParserManager();
        exchangeParser = new ExchangeParser();
    }

    private DataManager(){
    }

    public static DataManager newInstance(Context context) {
        return new DataManager(context);
    }

    public static DataManager getInstance(){
        return dataManager;
    }


    /**
     네트워크 연결상태에 따른 어디서 데이터를 가져올 것인가에 대한 구분 (두 가지 경우의 수가 있다.)
     - network connect       : parsing data를 가져온다.
     - network disconnect    : Realm DB에서 내용을 가져온다.
     */
    public boolean load() {
        if(NetworkUtil.isNetworkConnected(context)){
            Callable<List<ExchangeRate>> callable = new Callable<List<ExchangeRate>>() {
                @Override
                public List<ExchangeRate> call() throws Exception {
                    return getParserDataList();
                }
            };

            Callable<String[]> callRefreshText = new Callable<String[]>() {
                @Override
                public String[] call() throws Exception {
                    return getRefreshTexts();
                }
            };

            executeAsync(callRefreshText, refreshCallback);
            executeAsync(callable, callback);

            return true;
        }

        return false;
    }

    private void executeAsync(Callable callable, ParserManager.AsyncCallback callback){
        parserManager.getAsyncExecutor()
                .setCallable(callable)
                .setCallback(callback)
                .execute();
    }

    // 환율 정보 받아오기
    private List<ExchangeRate> getParserDataList(){
        return exchangeParser.getParserDatas();
    }

    // 갱신 날짜 받아오기
    private String[] getRefreshTexts(){
        return exchangeParser.getExchangeDates();
    }


    // 비동기로 실행된 결과를 받아 처리하는 코드
    private ParserManager.AsyncCallback<String[]> refreshCallback = new ParserManager.AsyncCallback<String[]>() {
        @Override
        public void onResult(String[] result) {
            // 언제 갱신된 환율 정보인지 Realm 에 저장한다.
            realm = Realm.getDefaultInstance();
            RealmControllerU.setExchangeDate(realm, result);
        }

        @Override
        public void exceptionOccured(Exception e) {
            Log.d(TAG, "exceptionOccured : "+e.getMessage());
        }

        @Override
        public void cancelled() {
            Log.d(TAG, "cancelled");
        }
    };

    // 비동기로 실행된 결과를 받아 처리하는 코드
    private ParserManager.AsyncCallback<List<ExchangeRate>> callback = new ParserManager.AsyncCallback<List<ExchangeRate>>() {
        @Override
        public void onResult(List<ExchangeRate> result) {
            realm = Realm.getDefaultInstance();
            RealmControllerU.setRealmDatas(realm, result);

            if(context instanceof MainActivity){
                Log.d(TAG, "Parsed from MainActivity");
                MainActivity activity = (MainActivity)context;
                activity.initViewPager(true);
            }

        }

        @Override
        public void exceptionOccured(Exception e) {
            Log.d(TAG, "exceptionOccured : "+e.getMessage());
        }

        @Override
        public void cancelled() {
            Log.d(TAG, "cancelled");
        }
    };


    public double getPrice(int which, ExchangeRate data){
        double price = 0;

        switch (which){
            case 0:
                price = data.getPriceBase();
                break;

            case 1:
                price = data.getPriceBuy();
                break;

            case 2:
                price = data.getPriceSell();
                break;

            case 3:
                price = data.getPriceSend();
                break;

            case 4:
                price = data.getPriceReceive();
                break;
        }

        return price;
    }
}
