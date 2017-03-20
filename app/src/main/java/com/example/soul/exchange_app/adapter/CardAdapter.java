package com.example.soul.exchange_app.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.soul.exchange_app.R;
import com.example.soul.exchange_app.data.ExchangeData;
import com.example.soul.exchange_app.util.MoneyUtil;
import com.example.soul.exchange_app.util.SlideInItemAnimator;

import java.util.List;

/**
 * Created by soul on 2017. 2. 27..
 */


public class CardAdapter extends RecyclerView.Adapter<CardAdapter.MyViewHolder> {

    private Context             mContext;
    private List<ExchangeData>  exchangeDataList;
    private ExchangeData        exchangeData;
    private int                 mExpandedPosition = -1;
    private CommentAnimator     commentAnimator;
    private final String TAG    = getClass().getSimpleName();

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, price, buy, sell, send, receive;
        public ImageView thumbnail;
        public LinearLayout details;

        public MyViewHolder(View view) {
            super(view);
            title   = (TextView) view.findViewById(R.id.title);
            price   = (TextView) view.findViewById(R.id.price);
            buy     = (TextView) view.findViewById(R.id.buy_cash);
            sell    = (TextView) view.findViewById(R.id.sell_cash);
            send    = (TextView) view.findViewById(R.id.send_cash);
            receive = (TextView) view.findViewById(R.id.receive_cash);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            details = (LinearLayout)view.findViewById(R.id.detail_card);
        }
    }


    public CardAdapter(Context mContext, List<ExchangeData> exchangeDataList) {
        this.mContext = mContext;
        this.exchangeDataList = exchangeDataList;
        commentAnimator = new CommentAnimator();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.exchange_card, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        exchangeData = exchangeDataList.get(position);
        holder.title.setText(exchangeData.getCountryAbbr()+" "+exchangeData.getCountryName());
        holder.price.setText(MoneyUtil.addCommas(exchangeData.getPriceBase()));
        holder.buy.setText(mContext.getResources().getString(R.string.buy_text)+MoneyUtil.addCommas(exchangeData.getPriceBuy()));
        holder.sell.setText(mContext.getResources().getString(R.string.sell_text)+MoneyUtil.addCommas(exchangeData.getPriceSell()));
        holder.send.setText(mContext.getResources().getString(R.string.send_text)+MoneyUtil.addCommas(exchangeData.getPriceSend()));
        holder.receive.setText(mContext.getResources().getString(R.string.receive_text)+MoneyUtil.addCommas(exchangeData.getPriceReceive()));

        // reference site : http://stackoverflow.com/questions/27203817/recyclerview-expand-collapse-items/38623873#38623873
        final boolean isExpanded = position == mExpandedPosition;
        holder.details.setVisibility(isExpanded?View.VISIBLE:View.GONE);
        holder.itemView.setActivated(isExpanded);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExpandedPosition = isExpanded ? -1: position;
                TransitionManager.beginDelayedTransition(holder.details);
                notifyDataSetChanged();
                Log.d(TAG, "Clicked >> mExpandedPosition : "+mExpandedPosition+" / position : "+position);
            }
        });

        // loading flag cover using Glide library
        Glide.with(mContext).load(exchangeData.getThumbnail()).into(holder.thumbnail);


        holder.thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animation = AnimationUtils.loadAnimation(mContext,R.anim.rotate);
                holder.thumbnail.startAnimation(animation);
            }
        });

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


    /**
     * A {@link RecyclerView.ItemAnimator} which allows disabling move animations. RecyclerView
     * does not like animating item height changes. {@link android.transition.ChangeBounds} allows
     * this but in order to simultaneously collapse one item and expand another, we need to run the
     * Transition on the entire RecyclerView. As such it attempts to move views around. This
     * custom item animator allows us to stop RecyclerView from trying to handle this for us while
     * the transition is running.
     */
    static class CommentAnimator extends SlideInItemAnimator {

        private boolean animateMoves = false;

        CommentAnimator() {
            super();
        }

        void setAnimateMoves(boolean animateMoves) {
            this.animateMoves = animateMoves;
        }

        @Override
        public boolean animateMove(
                RecyclerView.ViewHolder holder, int fromX, int fromY, int toX, int toY) {
            if (!animateMoves) {
                dispatchMoveFinished(holder);
                return false;
            }
            return super.animateMove(holder, fromX, fromY, toX, toY);
        }
    }
}