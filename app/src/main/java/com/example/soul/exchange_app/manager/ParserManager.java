package com.example.soul.exchange_app.manager;

import android.content.Context;
import android.util.Log;

import com.example.soul.exchange_app.activity.MainActivity;
import com.example.soul.exchange_app.model.ExchangeRate;
import com.example.soul.exchange_app.paser.ExchangeParser;
import com.example.soul.exchange_app.realm.RealmController;
import com.example.soul.exchange_app.util.NetworkUtil;

import java.util.List;
import java.util.concurrent.Callable;


/**
 * Created by soul on 2017. 6. 7..
 * 파싱한 데이터를
 */

public class ParserManager {

    private static final String TAG = ParserManager.class.getSimpleName();

    // data
    private DataManager dataManager;

    // Realm
    private RealmController realmController;

    private Context context;

    private ParserManager(Context context){
        this.context = context;
        realmController = RealmController.with(context);
        dataManager = new DataManager();
    }

    public static ParserManager newInstance(Context context) {
        return new ParserManager(context);
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

            dataManager.getAsyncExecutor()
                    .setCallable(callable)
                    .setCallback(callback)
                    .execute();
            return true;
        }

        return false;
    }

    private List<ExchangeRate> getParserDataList(){
        return new ExchangeParser().getParserDatas();
    }

    // 비동기로 실행된 결과를 받아 처리하는 코드
    private DataManager.AsyncCallback<List<ExchangeRate>> callback = new DataManager.AsyncCallback<List<ExchangeRate>>() {
        @Override
        public void onResult(List<ExchangeRate> result) {
            realmController.setRealmDatas(result);
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
}
