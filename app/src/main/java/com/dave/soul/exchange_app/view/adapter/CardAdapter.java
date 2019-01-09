package com.dave.soul.exchange_app.view.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dave.soul.exchange_app.R;
import com.dave.soul.exchange_app.view.activity.MainActivity;
import com.dave.soul.exchange_app.view.activity.SettingActivity;
import com.dave.soul.exchange_app.model.ExchangeRate;
import com.dave.soul.exchange_app.paser.ExchangeInfo;
import com.dave.soul.exchange_app.realm.RealmController;
import com.dave.soul.exchange_app.view.ui.CustomNotificationDialog;
import com.dave.soul.exchange_app.util.MoneyUtil;


import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by soul on 2017. 2. 27..
 */


public class CardAdapter extends RealmRecyclerViewAdapter<ExchangeRate, CardAdapter.MyViewHolder> {

    private Context mContext;
    private int mExpandedPosition = -1;
    private final String TAG = getClass().getSimpleName();
    private Realm realm;

    private static final int ROTATE_0_DEGREE    = 0;
    private static final int ROTATE_180_DEGREE  = 180;

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title, price, buy, sell, send, receive, calcu, alarm;
        ImageView thumbnail, arrow, graph;
        LinearLayout details;
        WebView webView;

        MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            price = (TextView) view.findViewById(R.id.price);
            buy = (TextView) view.findViewById(R.id.buy_cash);
            sell = (TextView) view.findViewById(R.id.sell_cash);
            send = (TextView) view.findViewById(R.id.send_cash);
            receive = (TextView) view.findViewById(R.id.receive_cash);
            calcu = (TextView)view.findViewById(R.id.btn_calcu);
            alarm = (TextView)view.findViewById(R.id.btn_alarm);

            details = (LinearLayout) view.findViewById(R.id.detail_card);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            arrow = (ImageView) view.findViewById(R.id.arrow);

            webView = (WebView)view.findViewById(R.id.webview);
            graph = (ImageView)view.findViewById(R.id.image_graph);
        }
    }

    public CardAdapter(OrderedRealmCollection<ExchangeRate> data, Context context) {
        super(data, true);
        setHasStableIds(true);
        this.mContext = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        realm = Realm.getDefaultInstance();
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.exchange_card, parent, false);

        PreferenceManager.setDefaultValues(mContext, R.xml.preferences, false);
        // create a new view
        Log.d(TAG, "onCreateViewHolder");
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final ExchangeRate obj = getItem(position);

        holder.title.setText(obj.getCountryAbbr() + " " + obj.getCountryName());
        double price = obj.getPriceBase();
        // 일본의 경우 일본 100으로 값이 다르게 잡혀있어 예외처리 해줌
        if(obj.getCountryAbbr().equals(ExchangeInfo.JPY)){
            price *= 100;
        }

        String buy = MoneyUtil.addCommas(obj.getPriceBuy());
        String sell = MoneyUtil.addCommas(obj.getPriceSell());
        String send = MoneyUtil.addCommas(obj.getPriceSend());
        String receive = MoneyUtil.addCommas(obj.getPriceReceive());

        holder.price.setText(MoneyUtil.addCommas(price));
        holder.buy.setText(mContext.getResources().getString(R.string.buy_text) + buy);
        holder.sell.setText(mContext.getResources().getString(R.string.sell_text) + sell);
        holder.send.setText(mContext.getResources().getString(R.string.send_text) + send);
        holder.receive.setText(mContext.getResources().getString(R.string.receive_text) + receive);
//        holder.webView.loadUrl("https://ssl.pstatic.net/imgfinance/chart/mobile/marketindex/month3/FX_"+obj.getCountryAbbr()+"KRW_search.png");
        SharedPreferences sharedPref    = PreferenceManager.getDefaultSharedPreferences(mContext);
        String showGraphType            = sharedPref.getString(SettingActivity.KEY_PREF_SHOW_GRAPH_TYPE, "");

        Glide.with(mContext)
                .load(ExchangeInfo.GRAPH_BASE_URL+showGraphType+"/FX_"
                        +obj.getCountryAbbr()+"KRW_search.png?sidcode=1476753629698?"+makeCurrentTime(1))
                .into(holder.graph);

        // reference site : http://stackoverflow.com/questions/27203817/recyclerview-expand-collapse-items/38623873#38623873
        final boolean isExpanded = position == mExpandedPosition;

        holder.calcu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Move to second viewPager."+obj.getCountryAbbr());
                RealmController.setCalcuCountry(realm, obj.getCountryAbbr(), ExchangeInfo.KRW);
                MainActivity activity = (MainActivity)mContext;
                activity.moveViewPager(1);
            }
        });


        holder.alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity)mContext;
                CustomNotificationDialog notificationDialog = CustomNotificationDialog.newInstance(obj.getCountryAbbr());
                notificationDialog.show(((MainActivity) mContext).getSupportFragmentManager(), "dialog");
                activity.moveViewPager(2);
            }
        });


        holder.details.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        changeArrow(isExpanded, holder.arrow);
        holder.itemView.setActivated(isExpanded);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExpandedPosition = isExpanded ? -1 : position;
//                    TransitionManager.beginDelayedTransition(holder.recyclerView);
                notifyDataSetChanged();
                Log.d(TAG, "Clicked >> mExpandedPosition : " + mExpandedPosition + " / position : " + position);
            }
        });

        // loading flag cover using Glide library
        Glide.with(mContext).load(obj.getThumbnail()).into(holder.thumbnail);
    }

    // https://github.com/mikepenz/MaterialDrawer/issues/1158
    private void changeArrow(boolean isExpanded, final View view){
        if(isExpanded){
            view.animate().rotation(ROTATE_180_DEGREE).start();
        }else{
            view.animate().rotation(ROTATE_0_DEGREE).start();
        }
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }


    private String makeCurrentTime(int which){

        final String DAY            = "yyyyMMdd";
        final String HOUR           = "yyyyMMddHH";
        final String MINUTE         = "yyyyMMddHHmm";

        String selectedStr          = null;

        switch (which){
            // DAY
            case 0:
                selectedStr = DAY;
                break;

            // HOUR
            case 1:
                selectedStr = HOUR;
                break;

            // MINUTE
            case 2:
                selectedStr = MINUTE;
                break;
        }

        SimpleDateFormat formatter = new SimpleDateFormat(selectedStr);
        Date currentTime = new Date();
        String dTime = formatter.format ( currentTime );
        return dTime;
    }
}

