package com.dave.soul.exchange_app.util;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by soul on 2017. 3. 12..
 */

public class DateUtil {

    private Context context;
    private final String TAG = getClass().getSimpleName();
    private static final String NEW_DATE_FORMAT = "yyyy.MM.dd HH:mm";
    static final String DATE_PATTERN_YEAR_MONTH_DAY_ADD_DASH = "yyyy-MM-dd";

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
        DateFormat formatter = new SimpleDateFormat("yyyy.MM.dd hh:mm");
        try {
            date = formatter.parse(dateString);

        } catch (ParseException e1) {
            e1.printStackTrace();
        }
        return date;
    }


    public String getDate(Date date){
        SimpleDateFormat sdf= new SimpleDateFormat(NEW_DATE_FORMAT);
        return sdf.format(date);
    }
}
