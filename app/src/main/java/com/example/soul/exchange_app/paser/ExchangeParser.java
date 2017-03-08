package com.example.soul.exchange_app.paser;

import android.util.Log;

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

public class ExchangeParser {

    private String TAG = this.getClass().getSimpleName();
    private Document doc;
    private List<String> perConutryList;
    private List<String[]> perCountryArrList;
    private StringBuilder builder;

    private Document getParserDoc(){
        try{
            doc = Jsoup.connect(ExchangeInfo.BASE_URL).get();
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

    public List<String[]> getPerserArrList(){
        perCountryArrList = new ArrayList<>();

        Document doc = getParserDoc();

        Elements titleLinks = doc.select("thead span");
        Elements links = doc.select("tbody tr");

        Log.d(TAG, ""+titleLinks);
        for(Element link : links){
            perCountryArrList.add(link.text().split(" "));
        }

        return perCountryArrList;
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

}
