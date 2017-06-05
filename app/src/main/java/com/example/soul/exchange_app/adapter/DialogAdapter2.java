package com.example.soul.exchange_app.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.soul.exchange_app.R;
import com.example.soul.exchange_app.model.ExchangeRate;

import io.realm.RealmResults;

/**
 * Created by soul on 2017. 6. 5..
 */

public class DialogAdapter2 extends BaseAdapter {

    private Context context;
    private RealmResults<ExchangeRate> realmResults;
    private final static String TAG = DialogAdapter2.class.getSimpleName();

    public DialogAdapter2(RealmResults<ExchangeRate> realmResults, Context context) {
        this.context = context;
        this.realmResults = realmResults;
    }

    @Override
    public int getCount() {
        return realmResults.size();
    }

    @Override
    public Object getItem(int position) {
        return realmResults.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d(TAG, "SetCountryAdapter - onCreateViewHolder");
        TextView title;
        ImageView thumbnail;
        MyViewHolder myViewHolder;

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater)context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.dialog_country_item, parent, false);

            title       = (TextView)convertView.findViewById(R.id.countryName);
            thumbnail   = (ImageView)convertView.findViewById(R.id.countryPicture);

            myViewHolder            = new MyViewHolder();
            myViewHolder.title      = title;
            myViewHolder.thumbnail  = thumbnail;

            convertView.setTag(myViewHolder);
        }else{
            myViewHolder    = (MyViewHolder)convertView.getTag();
            title           = myViewHolder.title;
            thumbnail       = myViewHolder.thumbnail;
        }

        ExchangeRate exchangeRate = realmResults.get(position);

        title.setText(exchangeRate.getCountryAbbr()+" "+exchangeRate.getCountryName());
        Glide.with(context).load(exchangeRate.getThumbnail()).into(thumbnail);

        return convertView;
    }

    public class MyViewHolder{
        public TextView title;
        public ImageView thumbnail;
    }
}
