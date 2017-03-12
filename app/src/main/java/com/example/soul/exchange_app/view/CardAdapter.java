package com.example.soul.exchange_app.view;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.soul.exchange_app.R;
import com.example.soul.exchange_app.data.ExchangeData;
import com.example.soul.exchange_app.util.MoneyCommas;

import java.util.List;

/**
 * Created by soul on 2017. 2. 27..
 */


public class CardAdapter extends RecyclerView.Adapter<CardAdapter.MyViewHolder> {

    private Context             mContext;
    private List<ExchangeData>  exchangeDataList;
    private ExchangeData        exchangeData;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, price;
        public ImageView thumbnail;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            price = (TextView) view.findViewById(R.id.price);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
        }
    }


    public CardAdapter(Context mContext, List<ExchangeData> exchangeDataList) {
        this.mContext = mContext;
        this.exchangeDataList = exchangeDataList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.exchange_card, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        exchangeData = exchangeDataList.get(position);
        holder.title.setText(exchangeData.getCountryAbbr()+" "+exchangeData.getCountryName());
        holder.price.setText(MoneyCommas.addCommas(exchangeData.getPriceBase()));


        // loading flag cover using Glide library
        Glide.with(mContext).load(exchangeData.getThumbnail()).into(holder.thumbnail);

//        holder.overflow.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                showPopupMenu(holder.overflow);
//            }
//        });
    }

    /**
     * Showing popup menu when tapping on 3 dots
     */
//    private void showPopupMenu(View view) {
//        // inflate menu
//        PopupMenu popup = new PopupMenu(mContext, view);
//        MenuInflater inflater = popup.getMenuInflater();
//        inflater.inflate(R.menu.menu_album, popup.getMenu());
//        popup.setOnMenuItemClickListener(new MyMenuItemClickListener());
//        popup.show();
//    }

    /**
     * Click listener for popup menu items
     */
//    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
//
//        public MyMenuItemClickListener() {
//        }
//
//        @Override
//        public boolean onMenuItemClick(MenuItem menuItem) {
//            switch (menuItem.getItemId()) {
//                case R.id.action_add_favourite:
//                    Toast.makeText(mContext, "Add to favourite", Toast.LENGTH_SHORT).show();
//                    return true;
//                case R.id.action_play_next:
//                    Toast.makeText(mContext, "Play next", Toast.LENGTH_SHORT).show();
//                    return true;
//                default:
//            }
//            return false;
//        }
//    }

    @Override
    public int getItemCount() {
        return exchangeDataList.size();
    }
}