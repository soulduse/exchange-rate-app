package com.example.soul.exchange_app.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.example.soul.exchange_app.realm.RealmController;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by soul on 2017. 5. 4..
 */

public class SetCountryAdapter extends RealmRecyclerViewAdapter<ExchangeRate, SetCountryAdapter.MyViewHolder> {

    private Context context;
    private final String TAG = getClass().getSimpleName();
    private RealmController realmController;



    public SetCountryAdapter(@Nullable OrderedRealmCollection<ExchangeRate> data, Context context) {
        super(data, true);
        setHasStableIds(true);
//        Log.d(TAG, "SetCountryAdapter : "+data.toString());
        this.context = context;
        realmController = RealmController.getInstance();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView title;
        public ImageView thumbnail;
        public CheckBox isCheck;
        public MyViewHolder(View itemView) {
            super(itemView);
            title       = (TextView)itemView.findViewById(R.id.countryName);
            thumbnail   = (ImageView)itemView.findViewById(R.id.countryPicture);
            isCheck     = (CheckBox)itemView.findViewById(R.id.countryCheckBox);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "SetCountryAdapter - onCreateViewHolder");
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.set_country_item, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        ExchangeRate obj = getItem(position);
        final String title = obj.getCountryAbbr() + " " + obj.getCountryName();
//        Log.d(TAG, "setCountyAdapter obj : "+obj.toString());
        holder.title.setText(title);
        holder.isCheck.setOnCheckedChangeListener(null);
        holder.isCheck.setChecked(obj.isCheckState());
        Glide.with(context).load(obj.getThumbnail()).into(holder.thumbnail);
        changeCheck(holder.isCheck, title);

        // itemView 클릭시 Check 되도록 추가
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.isCheck.performClick();
            }
        });
    }

    @Override
    public long getItemId(int index) {
        return getItem(index).hashCode();
    }


    private void changeCheck(final CheckBox checkBox, final String key){
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                realmController.changeCheckCounties(isChecked, key);
            }
        });
    }
}
