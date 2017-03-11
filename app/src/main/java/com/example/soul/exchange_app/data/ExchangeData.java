package com.example.soul.exchange_app.data;

/**
 * Created by soul on 2017. 2. 27..
 */

public class ExchangeData {

    private String countryName;
    private String countryAbbr;
    private double priceBase;
    private double priceBuy;
    private double priceSell;
    private double priceSend;
    private double priceReceive;
    private double priceusExchange;
    private String thumbnail;

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
