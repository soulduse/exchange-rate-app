package com.example.soul.exchange_app.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.soul.exchange_app.R;
import com.example.soul.exchange_app.model.ExchangeRate;
import com.example.soul.exchange_app.realm.RealmController;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by soul on 2017. 5. 18..
 */

public class DialogAdapter extends RealmRecyclerViewAdapter<ExchangeRate, DialogAdapter.MyViewHolder> {

    private final String TAG = getClass().getSimpleName();
    private RealmController realmController;
    private Context context;

    public DialogAdapter(@Nullable OrderedRealmCollection<ExchangeRate> data, Context context) {
        super(data, true);
        setHasStableIds(true);
        realmController = RealmController.getInstance();
        this.context    = context;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "SetCountryAdapter - onCreateViewHolder");
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.set_country_item, parent, false);

        return new DialogAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        ExchangeRate obj = getItem(position);
        final String title = obj.getCountryAbbr() + " " + obj.getCountryName();
        holder.title.setText(title);
        Glide.with(context).load(obj.getThumbnail()).into(holder.thumbnail);

        // itemView 클릭시 Check 되도록 추가
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
}