package com.example.soul.exchange_app.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.soul.exchange_app.R;
import com.example.soul.exchange_app.model.ExchangeRate;
import com.example.soul.exchange_app.model.SetExchangeRate;
import com.example.soul.exchange_app.realm.RealmController;
import com.example.soul.exchange_app.util.MoneyUtil;


import java.util.List;

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
    private RealmController realmController;

    private static final int ROTATE_0_DEGREE    = 0;
    private static final int ROTATE_180_DEGREE  = 180;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, price, buy, sell, send, receive;
        public ImageView thumbnail, arrow;
        public LinearLayout details;
        public RecyclerView recyclerView;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            price = (TextView) view.findViewById(R.id.price);
            buy = (TextView) view.findViewById(R.id.buy_cash);
            sell = (TextView) view.findViewById(R.id.sell_cash);
            send = (TextView) view.findViewById(R.id.send_cash);
            receive = (TextView) view.findViewById(R.id.receive_cash);
            details = (LinearLayout) view.findViewById(R.id.detail_card);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            arrow = (ImageView) view.findViewById(R.id.arrow);
        }
    }

    public CardAdapter(OrderedRealmCollection<ExchangeRate> data, Context context) {
        super(data, true);
        setHasStableIds(true);
        this.mContext = context;
        realmController = RealmController.getInstance();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.exchange_card, parent, false);
        // create a new view
        Log.d(TAG, "onCreateViewHolder");
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        ExchangeRate obj = getItem(position);

        List<SetExchangeRate> setExchangeRateList = realmController.getCheckedItems();
        holder.title.setText(obj.getCountryAbbr() + " " + obj.getCountryName());
        holder.price.setText(MoneyUtil.addCommas(obj.getPriceBase()));
        holder.buy.setText(mContext.getResources().getString(R.string.buy_text) + MoneyUtil.addCommas(obj.getPriceBuy()));
        holder.sell.setText(mContext.getResources().getString(R.string.sell_text) + MoneyUtil.addCommas(obj.getPriceSell()));
        holder.send.setText(mContext.getResources().getString(R.string.send_text) + MoneyUtil.addCommas(obj.getPriceSend()));
        holder.receive.setText(mContext.getResources().getString(R.string.receive_text) + MoneyUtil.addCommas(obj.getPriceReceive()));

        // reference site : http://stackoverflow.com/questions/27203817/recyclerview-expand-collapse-items/38623873#38623873
        final boolean isExpanded = position == mExpandedPosition;

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
}