package com.example.soul.exchange_app.util;

import android.util.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Created by soul on 2017. 3. 9..
 */

public class MoneyUtil {

    private static String TAG = "MoneyUtil.class";

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

    public static boolean checkNumLength(Object obj){
        String pattern = "\\d*(\\.\\d{0,2})?";
        Log.d(TAG, obj.toString() +" / result : "+obj.toString().matches(pattern));

        return obj.toString().matches(pattern);
    }

    public static String fmt(Object obj)
    {
        DecimalFormat form = new DecimalFormat("#,###.##");
        form.setRoundingMode(RoundingMode.DOWN);
        Double d = Double.parseDouble(removeCommas(obj.toString()));
        return form.format(d);
    }

    /*
    private String addCommas(double data){
        NumberFormat numformat = new DecimalFormat("###,###.###");
        String result = numformat.format(data);
        return result;
    }
    */
}
