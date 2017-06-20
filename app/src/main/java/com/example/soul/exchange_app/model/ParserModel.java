package com.example.soul.exchange_app.model;

import java.util.Date;

import io.realm.RealmObject;

/**
 * Created by soul on 2017. 6. 20..
 * 파싱 시점 저장
 * - 알람 또는 데이터 갱신
 */

public class ParserModel extends RealmObject{

    private Date date;
    private String source;
    private String count;
    private String num;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }
}
