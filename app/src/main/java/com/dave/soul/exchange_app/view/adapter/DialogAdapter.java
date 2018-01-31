package com.dave.soul.exchange_app.view.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dave.soul.exchange_app.R;
import com.dave.soul.exchange_app.model.ExchangeRate;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by soul on 2017. 5. 18..
 */

public class DialogAdapter extends RealmRecyclerViewAdapter<ExchangeRate, DialogAdapter.MyViewHolder> {

    private final String TAG = getClass().getSimpleName();
    private Context context;
    private OnItemClickListener mListener;

    public DialogAdapter(@Nullable OrderedRealmCollection<ExchangeRate> data, Context context, OnItemClickListener mListener) {
        super(data, true);
        setHasStableIds(true);
        this.context    = context;
        this.mListener  = mListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "SetCountryAdapter - onCreateViewHolder");
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.dialog_country_item, parent, false);

        return new DialogAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final ExchangeRate obj = getItem(position);
        final String title = obj.getCountryAbbr() + " " + obj.getCountryName();
        holder.title.setText(title);
        Glide.with(context).load(obj.getThumbnail()).into(holder.thumbnail);


        // itemView 클릭시 Check 되도록 추가
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemClicked(obj, position);
            }
        });

    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView title;
        public ImageView thumbnail;
        public MyViewHolder(View itemView) {
            super(itemView);
            title       = (TextView)itemView.findViewById(R.id.countryName);
            thumbnail   = (ImageView)itemView.findViewById(R.id.countryPicture);
        }
    }

    public interface OnItemClickListener {
        void onItemClicked(ExchangeRate result, int position);
    }
}
