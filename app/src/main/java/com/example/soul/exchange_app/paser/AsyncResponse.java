package com.example.soul.exchange_app.paser;

import com.example.soul.exchange_app.data.ExchangeData;

import java.util.List;

/**
 * Created by soul on 2017. 3. 20..
 */

public interface AsyncResponse {
    void processFinish(List<ExchangeData> mExchangeDatas);
}
