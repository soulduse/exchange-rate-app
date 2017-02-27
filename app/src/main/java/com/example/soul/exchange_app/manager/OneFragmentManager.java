package com.example.soul.exchange_app.manager;

import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import com.example.soul.exchange_app.paser.ExchangeDataParser;

/**
 * Created by soul on 2017. 2. 26..
 */

public class OneFragmentManager {

    private DataAsync dataAsync;
    private ExchangeDataParser exchangeDataParser;


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
            exchangeDataParser = new ExchangeDataParser();
        }


        @Override
        protected String doInBackground(String... params) {
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
