package com.example.soul.exchange_app.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.soul.exchange_app.R;
import com.example.soul.exchange_app.model.AlarmModel;
import com.example.soul.exchange_app.model.ExchangeRate;
import com.example.soul.exchange_app.util.MoneyUtil;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by soul on 2017. 6. 5..
 */

public class AlarmAdapter extends RealmRecyclerViewAdapter<AlarmModel, AlarmAdapter.MyViewHolder> {

    private static final String TAG = AlarmAdapter.class.getSimpleName();
    private Context context;

    public AlarmAdapter(@Nullable OrderedRealmCollection data, boolean autoUpdate, Context context) {
        super(data, autoUpdate);
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.dialog_alarm_item, parent, false);

        return new AlarmAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final AlarmModel obj = getItem(position);
        ExchangeRate exchangeRate = obj.getExchangeRate();
        Resources res = context.getResources();
        String[] titles= res.getStringArray(R.array.price_options);
        final String title = exchangeRate.getCountryAbbr() + " " + exchangeRate.getCountryName();
        holder.title.setText(title);

        holder.price.setText(titles[obj.getStandardExchange()]+" "+ MoneyUtil.fmt(obj.getPrice()));
        Glide.with(context).load(exchangeRate.getThumbnail()).into(holder.flag);

        // 이상일 경우 up arrow, 이하 일 경우 down arrow 표시
        int arrowImage = obj.isAboveOrbelow() == true ?
                R.drawable.ic_arrow_drop_up_red_500_18dp : R.drawable.ic_arrow_drop_down_red_500_18dp;
        Glide.with(context).load(arrowImage).into(holder.arrow);

    }


    public class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView alarmIcon, flag, arrow;
        TextView title, price;

        public MyViewHolder(View itemView) {
            super(itemView);
            alarmIcon   = (ImageView)itemView.findViewById(R.id.notificationIcon);
            flag        = (ImageView)itemView.findViewById(R.id.alarmFlag);
            arrow       = (ImageView)itemView.findViewById(R.id.alarmArrow);
            title       = (TextView)itemView.findViewById(R.id.alarmTitle);
            price       = (TextView)itemView.findViewById(R.id.alarmPrice);
        }
    }
}
