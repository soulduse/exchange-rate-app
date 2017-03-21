package com.example.soul.exchange_app.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.soul.exchange_app.R;
import com.example.soul.exchange_app.data.ExchangeData;

import java.util.List;

/**
 * Created by soul on 2017. 3. 20..
 */

public class SetCountryAdapter extends RecyclerView.Adapter<SetCountryAdapter.SetCountryViewHolder>{

    private List<ExchangeData>  exchangeDataList;
    private ExchangeData        exchangeData;
    private Context             mContext;
    private final String TAG = getClass().getSimpleName();

    public SetCountryAdapter(Context mContext, List<ExchangeData> exchangeDataList) {
        this.mContext = mContext;
        this.exchangeDataList = exchangeDataList;
    }

    public class SetCountryViewHolder extends RecyclerView.ViewHolder {
        public CheckBox check;
        public ImageView thumbnail;
        public TextView title;

        public SetCountryViewHolder(View view) {
            super(view);
            check = (CheckBox) view.findViewById(R.id.check_country);
            thumbnail = (ImageView) view.findViewById(R.id.image_set_flag);
            title = (TextView) view.findViewById(R.id.text_country_title);
        }
    }

    @Override
    public SetCountryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.set_country_item, parent, false);
        return new SetCountryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SetCountryViewHolder holder, int position) {
        exchangeData = exchangeDataList.get(position);
        Glide.with(mContext).load(exchangeData.getThumbnail()).into(holder.thumbnail);
        holder.title.setText(exchangeData.getCountryAbbr() + " " + exchangeData.getCountryName());
    }

    @Override
    public int getItemCount() {
        return exchangeDataList.size();
    }


}