package com.example.soul.exchange_app.paser;

import android.util.Log;

import com.example.soul.exchange_app.data.ExchangeData;
import com.example.soul.exchange_app.util.MoneyUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by soul on 2017. 2. 21..
 *
 *
 * 추가해야될 항목 2017. 03. 09
    - 예외처리 : 네트워크가 끊어졌을 경우 AsyncTask 부분에서 예외처리 추가 필요 ( )
 *
 */

public class ExchangeParser implements ExchangeInfo{

    private String TAG = this.getClass().getSimpleName();
    private Document doc;
    private List<String> perConutryList;
    private List<String[]> perCountryArrList;
    private List<ExchangeData> perCountDats;
    private List<String[]> exchangeArrList;
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
            String [] parserArr = errorCheckAndRemoveArray(link.text().split(" "));
            perCountryArrList.add(parserArr);
        }

        return perCountryArrList;
    }

    public List<ExchangeData> getParserDatas(){
        perCountDats = new ArrayList<>();
        ExchangeData exchangeData;

        exchangeArrList = getPerserArrList();
        for(int i=0; i<exchangeArrList.size(); i++) {
            exchangeData = new ExchangeData();
            exchangeData.setCountryName(exchangeArrList.get(i)[COUNTRY_NAME]);
            exchangeData.setCountryAbbr(exchangeArrList.get(i)[COUNTRY_ABBR]);
            exchangeData.setPriceBase((Double) MoneyUtil.changeStringToNumber(exchangeArrList.get(i)[PRICE_BASE]));
            exchangeData.setPriceBuy((Double) MoneyUtil.changeStringToNumber(exchangeArrList.get(i)[PRICE_BUY]));
            exchangeData.setPriceSell((Double) MoneyUtil.changeStringToNumber(exchangeArrList.get(i)[PRICE_SELL]));
            exchangeData.setPriceSend((Double) MoneyUtil.changeStringToNumber(exchangeArrList.get(i)[PRICE_SEND]));
            exchangeData.setPriceReceive((Double) MoneyUtil.changeStringToNumber(exchangeArrList.get(i)[PRICE_RECEIVE]));
            exchangeData.setPriceusExchange((Double) MoneyUtil.changeStringToNumber(exchangeArrList.get(i)[PRICE_US_EXCHANGE]));
            exchangeData.setThumbnail(combineThumbnailUrl(exchangeArrList.get(i)[COUNTRY_ABBR]));

            Log.w(
                    TAG,
                    "COUNTRY_NAME       : "+exchangeArrList.get(i)[COUNTRY_NAME]+" :: length : "+exchangeArrList.get(i).length+"\n"+
                    "COUNTRY_ABBR       : "+exchangeArrList.get(i)[COUNTRY_ABBR]+"\n"+
                    "PRICE_BASE         : "+exchangeArrList.get(i)[PRICE_BASE]+"\n"+
                    "PRICE_BUY          : "+exchangeArrList.get(i)[PRICE_BUY]+"\n"+
                    "PRICE_SELL         : "+exchangeArrList.get(i)[PRICE_SELL]+"\n"+
                    "PRICE_SEND         : "+exchangeArrList.get(i)[PRICE_SEND]+"\n"+
                    "PRICE_RECEIVE      : "+exchangeArrList.get(i)[PRICE_RECEIVE]+"\n"+
                    "PRICE_US_EXCHANGE  : "+exchangeArrList.get(i)[PRICE_US_EXCHANGE]
            );


            perCountDats.add(exchangeData);
        }

        return perCountDats;
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

    // 예외처리 - HTML 파싱시 특정 나라의 값이 일정하게 들어오지 않음. 따라서 별도의 예외처리가 필요.
    private String[] errorCheckAndRemoveArray(String[] arr){
        String [] copyArr;
        String [] resultArr;
        final String exceptionStr1 = "공화국";
        final String exceptionStr2 = "ZAR";

        if(arr.length >= 9){
            Log.d(TAG, "errorCheckAndRemoveArray >> 비정상");
            copyArr = java.util.Arrays.copyOf(arr, arr.length);

            for(int i=2; i<copyArr.length-1; i++){
                copyArr[i] = copyArr[i+1];
                if(copyArr[1].equals(exceptionStr1)){
                    copyArr[0] = copyArr[0]+" "+exceptionStr1;
                    copyArr[1] = exceptionStr2;
                }
            }

            resultArr = new String[copyArr.length-1];
            for(int i=0; i<copyArr.length-1; i++){
                resultArr[i] = copyArr[i];
                Log.e(TAG, "resultArr["+i+"] : "+resultArr[i]);
            }
            return resultArr;

        }else{
            Log.d(TAG, "errorCheckAndRemoveArray >> 정상");
            return arr;
        }
    }

    private String combineThumbnailUrl(String flag){
        String preUrl = FLAG_IMG_URL.substring(0,FLAG_IMG_URL.length()-4);
        String sufUrl = FLAG_IMG_URL.substring(FLAG_IMG_URL.length()-4, FLAG_IMG_URL.length());

        return preUrl+flag+sufUrl;
    }
}

