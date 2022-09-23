package com.dave.soul.exchange_app.util;

import android.util.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by soul on 2017. 3. 9..
 */

public class MoneyUtil {

    private static String TAG = "MoneyUtil.class";

    public static Object changeStringToNumber(String data){
        Object result;

        String removeCommasData = removeCommas(data);

        try {
            if(removeCommasData.length() >= 11){
                result = new BigDecimal(removeCommasData);
            }else{
                result = Double.parseDouble(removeCommasData);
            }
        } catch (NumberFormatException e) {
            result = 0.0;
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
        return form.format(makeDouble(obj.toString()));
    }

    public static String fmt(double val)
    {
        DecimalFormat form = new DecimalFormat("#,###.##");
        return form.format(val);
    }

    private static double makeDouble(String num){
        Double d = Double.parseDouble(removeCommas(num));
        return d;
    }

    public static double calMoney(double base1, double base2, String money){
        Log.d(TAG, "base1("+base1+") * money("+makeDouble(money)+") = "+base1 * makeDouble(money)+" / base2 : "+base2);
        if(base2 <= 0){
            Log.d(TAG, "base1("+base1+") * money("+makeDouble(money)+") = "+base1 * makeDouble(money));
            return base1 * makeDouble(money);
        }else{
            return (base1/base2) * makeDouble(money);
        }
    }


    public static boolean isNumber(String s){
        Pattern p = Pattern.compile("(^[0-9]*$)");
        Matcher m = p.matcher(s);
        return m.find();
    }

}
