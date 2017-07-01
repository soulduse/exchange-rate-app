package com.example.soul.exchange_app.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.soul.exchange_app.R;
import com.example.soul.exchange_app.model.AlarmModel;
import com.example.soul.exchange_app.model.ExchangeRate;
import com.example.soul.exchange_app.realm.RealmController;
import com.example.soul.exchange_app.ui.CustomNotiDialog;
import com.example.soul.exchange_app.util.MoneyUtil;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by soul on 2017. 6. 5..
 */

public class AlarmAdapter extends RealmRecyclerViewAdapter<AlarmModel, AlarmAdapter.MyViewHolder> {

    private static final String TAG = AlarmAdapter.class.getSimpleName();
    private Context context;
    private FragmentManager fragmentManager;
    private RealmController realmController;

    public AlarmAdapter(@Nullable OrderedRealmCollection data, boolean autoUpdate, Context context, FragmentManager fragmentManager) {
        super(data, autoUpdate);
        this.context = context;
        this.fragmentManager = fragmentManager;
        realmController = RealmController.getInstance();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.dialog_alarm_item, parent, false);
        return new AlarmAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final AlarmModel obj = getItem(position);
        ExchangeRate exchangeRate = obj.getExchangeRate();
        Resources res = context.getResources();
        String[] titles= res.getStringArray(R.array.pref_priceOptions);
        final String title = exchangeRate.getCountryAbbr() + " " + exchangeRate.getCountryName();
        holder.title.setText(title);

        holder.price.setText(titles[obj.getStandardExchange()]+" "+ MoneyUtil.fmt(obj.getPrice()));
        Glide.with(context).load(exchangeRate.getThumbnail()).into(holder.flag);

        // 이상일 경우 up arrow, 이하 일 경우 down arrow 표
        int arrowImage = obj.isAboveOrbelow() ?
                R.drawable.ic_arrow_drop_up_red_500_18dp : R.drawable.ic_arrow_drop_down_red_500_18dp;
        Glide.with(context).load(arrowImage).into(holder.arrow);

        // 알람 버튼을 누를때 마다 switch 되는 형식으로 껐다 켰다 해준다.
        final boolean alarmSwitch = obj.isAlarmSwitch();
        int alarmIcon = alarmSwitch ? R.drawable.ic_notifications_black_36dp : R.drawable.ic_notifications_off_black_36dp;
        Glide.with(context).load(alarmIcon).into(holder.alarmIcon);

        // 알람 해제/등록
        holder.alarmIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                realmController.turnAlarm(!alarmSwitch, position);
            }
        });

        // 등록된 알람중 수정/삭제 하기 위함
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomNotiDialog notiDialog = CustomNotiDialog.newInstance(obj, position);
                notiDialog.show(fragmentManager , "dialog");
                // 삭제가 이루어지고 난 후 데이터 셋을 변경시켜줘야 한다. 이 작업을 안하면 삭제가 이루어지면서 중간에 데이터 position에 올바른 값이 들어가지 않아 에러출력
                notiDialog.setOnChangeDataListener(new CustomNotiDialog.OnChangeDataListener() {
                    @Override
                    public void eventListener() {
                        notifyDataSetChanged();
                    }
                });
            }
        });
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView alarmIcon, flag, arrow;
        TextView title, price;

        public MyViewHolder(View itemView) {
            super(itemView);
            alarmIcon   = (ImageView)itemView.findViewById(R.id.notificationIcon);
            flag        = (ImageView)itemView.findViewById(R.id.alarmFlag);
            arrow       = (ImageView)itemView.findViewById(R.id.alarmArrow);
            title       = (TextView)itemView.findViewById(R.id.alarmTitle);
            price       = (TextView)itemView.findViewById(R.id.alarmPrice);
        }
    }
}
