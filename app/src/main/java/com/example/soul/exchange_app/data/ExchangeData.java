package com.example.soul.exchange_app.data;

/**
 * Created by soul on 2017. 2. 27..
 */

public class ExchangeData {

    private String countryName;
    private String countryAbbr;
    private float priceBase;
    private float priceBuy;
    private float priceSell;
    private float priceSend;
    private float priceReceive;
    private float priceusExchange;

    public ExchangeData() {
    }

    public ExchangeData(String countryName, String countryAbbr, float priceBase, float priceBuy, float priceSell, float priceSend, float priceReceive, float priceusExchange) {
        this.countryName = countryName;
        this.countryAbbr = countryAbbr;
        this.priceBase = priceBase;
        this.priceBuy = priceBuy;
        this.priceSell = priceSell;
        this.priceSend = priceSend;
        this.priceReceive = priceReceive;
        this.priceusExchange = priceusExchange;
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

    public float getPriceBase() {
        return priceBase;
    }

    public void setPriceBase(float priceBase) {
        this.priceBase = priceBase;
    }

    public float getPriceBuy() {
        return priceBuy;
    }

    public void setPriceBuy(float priceBuy) {
        this.priceBuy = priceBuy;
    }

    public float getPriceSell() {
        return priceSell;
    }

    public void setPriceSell(float priceSell) {
        this.priceSell = priceSell;
    }

    public float getPriceSend() {
        return priceSend;
    }

    public void setPriceSend(float priceSend) {
        this.priceSend = priceSend;
    }

    public float getPriceReceive() {
        return priceReceive;
    }

    public void setPriceReceive(float priceReceive) {
        this.priceReceive = priceReceive;
    }

    public float getPriceusExchange() {
        return priceusExchange;
    }

    public void setPriceusExchange(float priceusExchange) {
        this.priceusExchange = priceusExchange;
    }
}
