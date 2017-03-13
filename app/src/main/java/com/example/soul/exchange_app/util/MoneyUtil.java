package com.example.soul.exchange_app.util;

import android.util.Log;

import java.math.BigDecimal;

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

    private static String removeCommas(String data){
        return data.replace(",", "");
    }

    /*
    private String addCommas(double data){
        NumberFormat numformat = new DecimalFormat("###,###.###");
        String result = numformat.format(data);
        return result;
    }
    */
}
