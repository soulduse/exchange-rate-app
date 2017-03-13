package com.example.soul.exchange_app.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by soul on 2017. 3. 12..
 */

public class DateUtil {

    private Context context;
    private final String TAG = getClass().getSimpleName();
    private static final String NEW_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public DateUtil(){}

    public DateUtil(Context context){
        this.context = context;
    }

    public void getCountry(){

        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = context.getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = context.getResources().getConfiguration().locale;
        }
        String strDisplayCountry = locale.getDisplayCountry();
        String strCountry = locale.getCountry();
        String strLanguage = locale.getLanguage();

        Log.d(TAG, "DisplayCountry : "+strDisplayCountry+" / Country : "+strCountry+" / Language : "+strLanguage);
    }



    public String getDate(){
        getCountry();
        Date date = new Date();
        SimpleDateFormat sdf= new SimpleDateFormat();
        sdf.applyPattern(NEW_DATE_FORMAT);
        return sdf.format(date);
    }
}
