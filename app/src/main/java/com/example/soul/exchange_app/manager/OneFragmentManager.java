package com.example.soul.exchange_app.manager;

import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.soul.exchange_app.paser.ExchangeDataParser;

import java.util.List;

/**
 * Created by soul on 2017. 2. 26..
 */

public class OneFragmentManager {

    private final String TAG = getClass().getSimpleName();
    private DataAsync dataAsync;
    private ExchangeDataParser exchangeDataParser;
    private List<String[]> exchangeArrList;



    public void excuteDataAsync(TextView textExchange, View viewExchange){
        dataAsync = new DataAsync();
        dataAsync.setInit(textExchange, viewExchange);
        dataAsync.execute();
    }

    private class DataAsync extends AsyncTask<String, Void, String> {

        private TextView textExchange;
        private View viewExchange;


        public void setInit(TextView textExchange, View viewExchange){
            this.textExchange = textExchange;
            this.viewExchange = viewExchange;
            Log.d(TAG, "Is exchangeDataParser null? >> "+(exchangeDataParser == null));
            if(exchangeDataParser == null){
                exchangeDataParser = new ExchangeDataParser();
            }
        }


        @Override
        protected String doInBackground(String... params) {

            exchangeArrList = exchangeDataParser.getPerserArrList();
            for(int i=0; i<exchangeArrList.size(); i++){
                Log.d(TAG, "Item Size : "+exchangeArrList.get(i).length+
                        " \n Item 0 : "+exchangeArrList.get(i)[0]+
                        " \n Item 1 : "+exchangeArrList.get(i)[1]+
                        " \n Item 2 : "+exchangeArrList.get(i)[2]+
                        " \n Item 3 : "+exchangeArrList.get(i)[3]+
                        " \n Item 4 : "+exchangeArrList.get(i)[4]+
                        " \n Item 5 : "+exchangeArrList.get(i)[5]+
                        " \n Item 6 : "+exchangeArrList.get(i)[6]+
                        " \n Item 7 : "+exchangeArrList.get(i)[7]);
            }


            return exchangeDataParser.getParserString();
        }

        @Override
        protected void onPostExecute(String s) {
            textExchange.setText(s);
            Snackbar.make(viewExchange, "data update", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }
}
