package com.example.soul.exchange_app.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
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
import com.example.soul.exchange_app.data.ExchangeData;
import com.example.soul.exchange_app.util.MoneyUtil;

import java.util.List;

/**
 * Created by soul on 2017. 2. 27..
 */


public class CardAdapter extends RecyclerView.Adapter<CardAdapter.MyViewHolder> {

    private Context mContext;
    private List<ExchangeData> exchangeDataList;
    private ExchangeData exchangeData;
    private RecyclerView mRecyclerView;
    private int mExpandedPosition = -1;
    private final String TAG = getClass().getSimpleName();

    private static final int ANIMATION_DURATION = 300;
    private static final int ROTATE_180_DEGREE = 180;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, price, buy, sell, send, receive;
        public ImageView thumbnail, arrow;
        public LinearLayout details;
        public RecyclerView recyclerView;
        public CardView cardView;

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
            recyclerView = mRecyclerView;
        }
    }


    public CardAdapter(Context mContext, List<ExchangeData> exchangeDataList, RecyclerView mRecyclerView) {
        this.mContext = mContext;
        this.exchangeDataList = exchangeDataList;
        this.mRecyclerView = mRecyclerView;
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
        exchangeData = exchangeDataList.get(position);
        holder.title.setText(exchangeData.getCountryAbbr() + " " + exchangeData.getCountryName());
        holder.price.setText(MoneyUtil.addCommas(exchangeData.getPriceBase()));
        holder.buy.setText(mContext.getResources().getString(R.string.buy_text) + MoneyUtil.addCommas(exchangeData.getPriceBuy()));
        holder.sell.setText(mContext.getResources().getString(R.string.sell_text) + MoneyUtil.addCommas(exchangeData.getPriceSell()));
        holder.send.setText(mContext.getResources().getString(R.string.send_text) + MoneyUtil.addCommas(exchangeData.getPriceSend()));
        holder.receive.setText(mContext.getResources().getString(R.string.receive_text) + MoneyUtil.addCommas(exchangeData.getPriceReceive()));

        // reference site : http://stackoverflow.com/questions/27203817/recyclerview-expand-collapse-items/38623873#38623873
        final boolean isExpanded = position == mExpandedPosition;

        holder.details.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.itemView.setActivated(isExpanded);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExpandedPosition = isExpanded ? -1 : position;
                Log.w(TAG, "recyclerView.getChildCount() : "+holder.recyclerView.getChildCount());
//                    TransitionManager.beginDelayedTransition(holder.recyclerView);
                changeArrow(isExpanded, holder.arrow);
                notifyDataSetChanged();
                Log.d(TAG, "Clicked >> mExpandedPosition : " + mExpandedPosition + " / position : " + position);
            }
        });

        // loading flag cover using Glide library
        Glide.with(mContext).load(exchangeData.getThumbnail()).into(holder.thumbnail);

    }

    private void changeArrow(boolean isExpanded, final View view){
        if(isExpanded){
            view.animate().rotation(0).start();
        }else{
            view.animate().rotation(ROTATE_180_DEGREE).start();
        }
    }


    @Override
    public int getItemCount() {
        return exchangeDataList.size();
    }

    @Override
    public long getItemId(int position) {
        return exchangeDataList.get(position).hashCode();
    }
}