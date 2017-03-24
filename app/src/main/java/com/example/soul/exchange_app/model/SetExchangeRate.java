package com.example.soul.exchange_app.model;


import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by soul on 2017. 3. 21..
 */

public class SetExchangeRate extends RealmObject {

    @PrimaryKey
    private long id;
    private String thumbnail;
    private String countryName;
    private String countryAbbr;
    private boolean checkState;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
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

    public boolean isCheckState() {
        return checkState;
    }

    public void setCheckState(boolean checkState) {
        this.checkState = checkState;
    }
}
