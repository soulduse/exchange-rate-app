package com.example.soul.exchange_app.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.soul.exchange_app.R;
import com.example.soul.exchange_app.model.ExchangeRate;
import com.example.soul.exchange_app.model.SetExchangeRate;

import java.util.ArrayList;
import java.util.List;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by soul on 2017. 3. 20..
 */

public class SetCountryAdapter extends RealmRecyclerViewAdapter<SetExchangeRate, SetCountryAdapter.SetCountryViewHolder> {

    private List<ExchangeRate>  exchangeRateList;
    private ExchangeRate        exchangeRate;
    private Context             mContext;
    private final String TAG = getClass().getSimpleName();

    private ArrayList<SetExchangeRate> myItems = new ArrayList<>();

    public SetCountryAdapter(@Nullable OrderedRealmCollection<SetExchangeRate> data, boolean autoUpdate) {
        super(data, autoUpdate);
        setHasStableIds(true);
    }

//    public SetCountryAdapter(Context mContext, List<ExchangeRate> exchangeRateList) {
//        this.mContext = mContext;
//        this.exchangeRateList = exchangeRateList;
//    }

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
    public void onBindViewHolder(final SetCountryViewHolder holder, int position) {
        final SetExchangeRate objIncome = myItems.get(position);
        exchangeRate = exchangeRateList.get(position);
        Glide.with(mContext).load(exchangeRate.getThumbnail()).into(holder.thumbnail);
        holder.title.setText(exchangeRate.getCountryAbbr() + " " + exchangeRate.getCountryName());

        //in some cases, it will prevent unwanted situations
        holder.check.setOnCheckedChangeListener(null);

        //if true, your checkbox will be selected, else unselected
        holder.check.setChecked(holder.check.isSelected());

        holder.check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //set your object's last status
                holder.check.setSelected(isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return exchangeRateList.size();
    }


}
