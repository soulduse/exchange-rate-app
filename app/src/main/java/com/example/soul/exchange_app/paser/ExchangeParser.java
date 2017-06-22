package com.example.soul.exchange_app.paser;

import android.util.Log;

import com.example.soul.exchange_app.model.ExchangeRate;
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
    private List<String[]> perCountryArrList;
    private List<ExchangeRate> perCountDats;
    private List<String[]> exchangeArrList;

    private Document getParserDoc(String url){
        try{
            doc = Jsoup.connect(url).get();
        }catch (IOException ioe){
            ioe.printStackTrace();
        }
        return doc;
    }

    public List<String[]> getPerserArrList(){
        perCountryArrList = new ArrayList<>();

        Document doc = getParserDoc(ExchangeInfo.BASE_URL);

//        Log.d(TAG, "getParserDoc : "+doc.toString());

        Elements titleLinks = doc.select("thead span");
        Elements links = doc.select("tbody tr");

//        Log.d(TAG, ""+titleLinks);
        for(Element link : links){
            String [] parserArr = errorCheckAndRemoveArray(link.text().split(" "));
            perCountryArrList.add(parserArr);
        }

        return perCountryArrList;
    }

    public List<ExchangeRate> getParserDatas(){
        perCountDats = new ArrayList<>();
        ExchangeRate exchangeRate;

        exchangeArrList = getPerserArrList();

        // 한국 데이터는 없기 때문에 임의로 하나 넣어줌.
//        Log.d(TAG, "Create Korean Datas");
        exchangeRate = new ExchangeRate();
        exchangeRate.setThumbnail(ExchangeInfo.KOREA_FLAG);
        exchangeRate.setCountryAbbr(ExchangeInfo.KRW);
        exchangeRate.setCountryName(ExchangeInfo.KNAME);
        exchangeRate.setCheckState(false);
        exchangeRate.setPriceBase(1);
        exchangeRate.setPriceBuy(1);
        exchangeRate.setPriceSell(1);
        exchangeRate.setPriceSend(1);
        exchangeRate.setPriceReceive(1);

        perCountDats.add(exchangeRate);

        for(int i=0; i<exchangeArrList.size(); i++) {
            exchangeRate = new ExchangeRate();
            exchangeRate.setCountryName(exchangeArrList.get(i)[COUNTRY_NAME]);
            exchangeRate.setCountryAbbr(exchangeArrList.get(i)[COUNTRY_ABBR]);
            // 일본 데이터가 일본 100 으로 수치가 다르게 표시되기 때문에 / 100을 해줘야 정상적으로 계산값이 맞아 떨어진다.
            if(exchangeArrList.get(i)[COUNTRY_ABBR].equals(ExchangeInfo.JPY)){
                exchangeRate.setPriceBase((Double) (MoneyUtil.changeStringToNumber(exchangeArrList.get(i)[PRICE_BASE]))/100);
                exchangeRate.setPriceBuy((Double) (MoneyUtil.changeStringToNumber(exchangeArrList.get(i)[PRICE_BUY]))/100);
                exchangeRate.setPriceSell((Double) (MoneyUtil.changeStringToNumber(exchangeArrList.get(i)[PRICE_SELL]))/100);
                exchangeRate.setPriceSend((Double) (MoneyUtil.changeStringToNumber(exchangeArrList.get(i)[PRICE_SEND]))/100);
                exchangeRate.setPriceReceive((Double) (MoneyUtil.changeStringToNumber(exchangeArrList.get(i)[PRICE_RECEIVE]))/100);
                exchangeRate.setPriceusExchange((Double) (MoneyUtil.changeStringToNumber(exchangeArrList.get(i)[PRICE_US_EXCHANGE]))/100);
            }else{
                exchangeRate.setPriceBase((Double) MoneyUtil.changeStringToNumber(exchangeArrList.get(i)[PRICE_BASE]));
                exchangeRate.setPriceBuy((Double) MoneyUtil.changeStringToNumber(exchangeArrList.get(i)[PRICE_BUY]));
                exchangeRate.setPriceSell((Double) MoneyUtil.changeStringToNumber(exchangeArrList.get(i)[PRICE_SELL]));
                exchangeRate.setPriceSend((Double) MoneyUtil.changeStringToNumber(exchangeArrList.get(i)[PRICE_SEND]));
                exchangeRate.setPriceReceive((Double) MoneyUtil.changeStringToNumber(exchangeArrList.get(i)[PRICE_RECEIVE]));
                exchangeRate.setPriceusExchange((Double) MoneyUtil.changeStringToNumber(exchangeArrList.get(i)[PRICE_US_EXCHANGE]));
                exchangeRate.setThumbnail(combineThumbnailUrl(exchangeArrList.get(i)[COUNTRY_ABBR]));
            }



            /*
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
            */
            perCountDats.add(exchangeRate);
        }

        return perCountDats;
    }

    // 예외처리 - HTML 파싱시 특정 나라의 값이 일정하게 들어오지 않음. 따라서 별도의 예외처리가 필요.
    private String[] errorCheckAndRemoveArray(String[] arr){
        String [] copyArr;
        String [] resultArr;
        final String exceptionStr1 = "공화국";
        final String exceptionStr2 = "ZAR";

        if(arr.length >= 9){
//            Log.d(TAG, "errorCheckAndRemoveArray >> 비정상");
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
//                Log.e(TAG, "resultArr["+i+"] : "+resultArr[i]);
            }
            return resultArr;

        }else{
//            Log.d(TAG, "errorCheckAndRemoveArray >> 정상");
            return arr;
        }
    }

    private String combineThumbnailUrl(String flag){
        String preUrl = FLAG_IMG_URL.substring(0,FLAG_IMG_URL.length()-4);
        String sufUrl = FLAG_IMG_URL.substring(FLAG_IMG_URL.length()-4, FLAG_IMG_URL.length());

        return preUrl+flag+sufUrl;
    }

    public String getExchangeDate(){
        Document doc = getParserDoc(ExchangeInfo.SECOND_URL);
        Elements titleLinks = doc.select(".graph_info");
        Element element = titleLinks.get(0);
        return element.text();
    }

    public String[] getExchangeDates(){
        String[] result = new String[4];
        Document doc = getParserDoc(ExchangeInfo.SECOND_URL);
        Elements link = doc.select(".graph_info");
        Element element = link.get(0);
        String time     = element.select(".time").text();
        String source   = element.select(".source").text();
        String count    = element.select(".count").text();
        String num      = element.select(".num").text();

        result[0] = time;
        result[1] = source;
        result[2] = count;
        result[3] = num;

        return result;
    }
}


