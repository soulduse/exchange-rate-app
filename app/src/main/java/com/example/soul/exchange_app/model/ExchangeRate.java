package com.example.soul.exchange_app.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by soul on 2017. 2. 27..
 */

public class ExchangeRate extends RealmObject{

    @PrimaryKey
    private int id;
    private String countryName;
    private String countryAbbr;
    private double priceBase;
    private double priceBuy;
    private double priceSell;
    private double priceSend;
    private double priceReceive;
    private double priceusExchange;
    private String thumbnail;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getCountryAbbr() {
        return countryAbbr;
    }

    public void setCountryAbbr(String countryAbbr) {
        this.countryAbbr = countryAbbr;
    }

    public double getPriceBase() {
        return priceBase;
    }

    public void setPriceBase(double priceBase) {
        this.priceBase = priceBase;
    }

    public double getPriceBuy() {
        return priceBuy;
    }

    public void setPriceBuy(double priceBuy) {
        this.priceBuy = priceBuy;
    }

    public double getPriceSell() {
        return priceSell;
    }

    public void setPriceSell(double priceSell) {
        this.priceSell = priceSell;
    }

    public double getPriceSend() {
        return priceSend;
    }

    public void setPriceSend(double priceSend) {
        this.priceSend = priceSend;
    }

    public double getPriceReceive() {
        return priceReceive;
    }

    public void setPriceReceive(double priceReceive) {
        this.priceReceive = priceReceive;
    }

    public double getPriceusExchange() {
        return priceusExchange;
    }

    public void setPriceusExchange(double priceusExchange) {
        this.priceusExchange = priceusExchange;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
}
