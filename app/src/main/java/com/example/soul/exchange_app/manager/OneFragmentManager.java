package com.example.soul.exchange_app.manager;

import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.example.soul.exchange_app.data.ExchangeData;
import com.example.soul.exchange_app.paser.ExchangeInfo;
import com.example.soul.exchange_app.paser.ExchangeParser;
import com.example.soul.exchange_app.view.CardAdapter;

import java.util.List;


/**
 * Created by soul on 2017. 2. 26..
 */

public class OneFragmentManager implements ExchangeInfo {

    private final String TAG = getClass().getSimpleName();
    private DataAsync dataAsync;
    private ExchangeParser exchangeParser;
    private List<ExchangeData> exchangeDataList;

    private CardAdapter adapter;

    public void excuteDataAsync(RecyclerView recyclerView, View viewExchange){
        dataAsync = new DataAsync();
        dataAsync.setInit(recyclerView, viewExchange);
        dataAsync.execute();
    }

    private class DataAsync extends AsyncTask<String, Void, List<ExchangeData>> {
        private View viewExchange;
        private RecyclerView recyclerView;


        public void setInit(RecyclerView recyclerView, View viewExchange){
            this.viewExchange = viewExchange;
            Log.d(TAG, "Is exchangeParser null? >> "+(exchangeParser == null));
            if(exchangeParser == null){
                exchangeParser = new ExchangeParser();
            }
        }





        @Override
        protected List<ExchangeData> doInBackground(String... params) {
            exchangeDataList = exchangeParser.getParserDatas();
            return exchangeDataList;
        }



        @Override
        protected void onPostExecute(List<ExchangeData> mExchangeDatas) {
            adapter = new CardAdapter(viewExchange.getContext(), exchangeParser.getParserDatas());
            recyclerView.setAdapter(adapter);
            Snackbar.make(viewExchange, "data update", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }
}
