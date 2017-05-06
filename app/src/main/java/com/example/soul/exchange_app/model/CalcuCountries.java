package com.example.soul.exchange_app.model;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by soul on 2017. 5. 5..
 */

public class CalcuCountries extends RealmObject {

    private String calOne;
    private String calTwo;
    private RealmList<ExchangeRate> exchangeRates;

    public String getCalOne() {
        return calOne;
    }

    public void setCalOne(String calOne) {
        this.calOne = calOne;
    }

    public String getCalTwo() {
        return calTwo;
    }

    public void setCalTwo(String calTwo) {
        this.calTwo = calTwo;
    }

    public RealmList<ExchangeRate> getExchangeRates() {
        return exchangeRates;
    }

    public void setExchangeRates(RealmList<ExchangeRate> exchangeRates) {
        this.exchangeRates = exchangeRates;
    }
}
