package com.example.soul.exchange_app.util;

import android.util.Log;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Created by soul on 2017. 3. 9..
 */

public class MoneyUtil {

    private final String TAG = getClass().getSimpleName();

    public static Object changeStringToNumber(String data){
        Object result;

        String removeCommasData = removeCommas(data);

        if(removeCommasData.length() >= 11){
            result = new BigDecimal(removeCommasData);
        }else{
            result = Double.parseDouble(removeCommasData);
        }
        return result;
    }

    public static String addCommas(Object obj){
        String result = String.format("%,.2f",obj);
        return result;
    }

    public static String removeCommas(String data){
        return data.replace(",", "");
    }

    public static String fmt(Object obj)
    {
        DecimalFormat form = new DecimalFormat("#,###.##");
        return form.format(obj);
//
//        if(d == (long) d)
//            return String.format("%d",(long)d);
//        else
//            return String.format("%s",d);
    }

    /*
    private String addCommas(double data){
        NumberFormat numformat = new DecimalFormat("###,###.###");
        String result = numformat.format(data);
        return result;
    }
    */
}
