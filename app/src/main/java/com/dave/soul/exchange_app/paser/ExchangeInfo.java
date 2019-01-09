package com.dave.soul.exchange_app.paser;

/**
 * Created by soul on 2017. 2. 26..
 */

public interface ExchangeInfo {

    String BASE_URL         = "https://finance.naver.com/marketindex/exchangeList.nhn";
    String SECOND_URL       = "https://finance.naver.com/marketindex/";
    String FLAG_IMG_URL     = "http://imgfinance.naver.net/nfinance/flag/flag_.png";
    String KOREA_FLAG       = "https://upload.wikimedia.org/wikipedia/commons/thumb/0/09/Flag_of_South_Korea.svg/50px-Flag_of_South_Korea.svg.png";
    String GRAPH_BASE_URL   = "https://ssl.pstatic.net/imgfinance/chart/mobile/marketindex/";

    String USD              = "USD";
    String KRW              = "KRW";
    String JPY              = "JPY";
    String KNAME            = "대한민국";

    int COUNTRY_NAME        = 0;
    int COUNTRY_ABBR        = 1; // Abbreviations
    int PRICE_BASE          = 2;
    int PRICE_BUY           = 3;
    int PRICE_SELL          = 4;
    int PRICE_SEND          = 5;
    int PRICE_RECEIVE       = 6;
    int PRICE_US_EXCHANGE   = 7;


}
