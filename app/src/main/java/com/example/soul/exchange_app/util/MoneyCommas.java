package com.example.soul.exchange_app.util;

import android.util.Log;

/**
 * Created by soul on 2017. 3. 9..
 */

public class MoneyCommas {

    private final String TAG = getClass().getSimpleName();

    public float getFloatVal(String data){
        float number = Float.parseFloat(data.replace(",", ""));
        Log.w(TAG, "Float Data >> "+number);
        return number;
    }
}
