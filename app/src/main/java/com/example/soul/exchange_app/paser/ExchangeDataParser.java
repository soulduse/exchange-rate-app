package com.example.soul.exchange_app.paser;

import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by soul on 2017. 2. 21..
 */

public class ExchangeDataParser {
    private String TAG = this.getClass().getSimpleName();
    private Document doc;
    private final String BASE_URL = "http://info.finance.naver.com/marketindex/exchangeList.nhn";
    private final String FLAG_IMG_URL = "http://imgfinance.naver.net/nfinance/flag/flag_.png";
    private List<String> perConutryList;
    private StringBuilder builder;
    private DataAsync dataAsync;

    private Document getParserDoc(){
        try{
            doc = Jsoup.connect(BASE_URL).get();
        }catch (IOException ioe){
            ioe.printStackTrace();
        }
        return doc;

    }

    public List<String> getParserList(){
        perConutryList = new ArrayList();

        Document doc = getParserDoc();

        Elements titleLinks = doc.select("thead span");
        Elements links = doc.select("tbody tr");

        Log.d(TAG, ""+titleLinks);
        for(Element link : links){
//                String perCountryData = builder.append(link.text()).append("\n").toString();
            perConutryList.add(link.text());
        }
        Log.d(TAG, "Elements size 1: "+titleLinks.size()+" / size 2: "+perConutryList.size());

        for(int i=0; i<perConutryList.size(); i++){
            Log.d(TAG, perConutryList.get(i));
        }
        return perConutryList;
    }

    public String getParserString(){
        Document doc = getParserDoc();
        builder = new StringBuilder();
        Elements links = doc.select("tbody tr");

        for(Element link : links){
            builder.append(link.text()).append("\n").toString();
        }
        return builder.toString();
    }

    public void excuteDataAsync(TextView textExchange, View viewExchange){
        dataAsync = new DataAsync();
        dataAsync.setInit(textExchange, viewExchange);
        dataAsync.execute();
    }

    private class DataAsync extends AsyncTask<String, Void, String>{

        private TextView textExchange;
        private View viewExchange;


        public void setInit(TextView textExchange, View viewExchange){
            this.textExchange = textExchange;
            this.viewExchange = viewExchange;
        }


        @Override
        protected String doInBackground(String... params) {
            return getParserString();
        }

        @Override
        protected void onPostExecute(String s) {
            textExchange.setText(s);
            Snackbar.make(viewExchange, "data update", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }
}
