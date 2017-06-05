package com.example.soul.exchange_app.model;

import io.realm.RealmObject;

/**
 * Created by soul on 2017. 6. 5..
 */

public class AlarmModel extends RealmObject{
    private ExchangeRate exchangeRate;
    private int standardExchange;
    private double price;
    private boolean aboveOrbelow;
    private boolean alarmSwitch;

    public ExchangeRate getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(ExchangeRate exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public int getStandardExchange() {
        return standardExchange;
    }

    public void setStandardExchange(int standardExchange) {
        this.standardExchange = standardExchange;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isAboveOrbelow() {
        return aboveOrbelow;
    }

    public void setAboveOrbelow(boolean aboveOrbelow) {
        this.aboveOrbelow = aboveOrbelow;
    }

    public boolean isAlarmSwitch() {
        return alarmSwitch;
    }

    public void setAlarmSwitch(boolean alarmSwitch) {
        this.alarmSwitch = alarmSwitch;
    }
}
