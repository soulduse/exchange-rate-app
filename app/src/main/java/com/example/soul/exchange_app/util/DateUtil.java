package com.example.soul.exchange_app.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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

    private static final DateUtil dataUtil = new DateUtil();

    private DateUtil(){}

    public static DateUtil getInstance() {
        return dataUtil;
    }

    public void init(Context context){
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

    public int getHourOfDay(){
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    public boolean isRangeTime(int startTime, int stopTime){
        int currentTime = getHourOfDay();
        boolean result = (currentTime >= startTime && currentTime <= stopTime);
        return result;
    }


    public Date toDate(String dateString) {
        Date date = null;
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            date = formatter.parse(dateString);

        } catch (ParseException e1) {
            e1.printStackTrace();
        }
        return date;
    }
}
