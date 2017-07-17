package com.example.soul.exchange_app.ui;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.soul.exchange_app.R;
import com.example.soul.exchange_app.adapter.DialogAdapter2;
import com.example.soul.exchange_app.databinding.DialogNotificationBinding;
import com.example.soul.exchange_app.model.AlarmModel;
import com.example.soul.exchange_app.model.ExchangeRate;
import com.example.soul.exchange_app.realm.RealmControllerU;
import com.example.soul.exchange_app.util.MoneyUtil;

import io.realm.Realm;
import io.realm.RealmResults;


/**
 * Created by soul on 2017. 6. 4..
 */

public class CustomNotiDialog extends DialogFragment{

    private static final String TAG = CustomNotiDialog.class.getSimpleName();
    private static final int FLAG_PRICE_BASE    = 0;
    private static final int FLAG_PRICE_BUY     = 1;
    private static final int FLAG_PRICE_SELL    = 2;
    private static final int FLAG_PRICE_SEND    = 3;
    private static final int FLAG_PRICE_RECEIVE = 4;

    private Realm realm;
    private DialogNotificationBinding binding;
    private AlarmModel alarmModel;
    private int position = -1;
    private OnChangeDataListener onChangeDataListener;

    private CustomNotiDialog(){}
    private CustomNotiDialog(int position){
        this.position = position;
    }

    public static CustomNotiDialog newInstance() {
        return new CustomNotiDialog();
    }

    public static CustomNotiDialog newInstance(int position) {
        return new CustomNotiDialog(position);
    }

    public static CustomNotiDialog newInstance(AlarmModel alarmModel, int position) {
        CustomNotiDialog customDialog = new CustomNotiDialog();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putSerializable("alarm", alarmModel);
        args.putInt("position", position);
        customDialog.setArguments(args);

        return customDialog;
    }

    public void setPosition(int position){
        this.position = position;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if(getArguments() != null && !getArguments().isEmpty()){
            alarmModel = (AlarmModel) getArguments().getSerializable("alarm");
            position = getArguments().getInt("position");
        }



        // Pick a style based on the num.
        int style = DialogFragment.STYLE_NORMAL, theme = 0;

        realm = Realm.getDefaultInstance();

        setStyle(style, theme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_notification, container, false);
        binding.setDialog(this);

        final RealmResults<ExchangeRate> realmResults = RealmControllerU.getExchangeRateExceptKorea(realm);

        DialogAdapter2 countryAdapter               = new DialogAdapter2(realmResults, getActivity());
        final ArrayAdapter<CharSequence> exchangeAdapter  = ArrayAdapter.createFromResource(getContext(),
                R.array.pref_priceOptions, android.R.layout.simple_spinner_item);
        exchangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        binding.spinner.setAdapter(countryAdapter);
        binding.spinner2.setAdapter(exchangeAdapter);

        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ExchangeRate exchangeRate = realmResults.get(position);
                int spinnerTwoPosition = binding.spinner2.getSelectedItemPosition();
                String price = getSelectedPrice(spinnerTwoPosition, exchangeRate);
                binding.alarmPriceEdit.setHint(price);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ExchangeRate exchangeRate = realmResults.get(binding.spinner.getSelectedItemPosition());
                String price = getSelectedPrice(position, exchangeRate);
                binding.alarmPriceEdit.setHint(price);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        if(alarmModel!=null){
            binding.alarmPriceEdit.setText(MoneyUtil.fmt(alarmModel.getPrice()));
            String text = !alarmModel.isAboveOrbelow()
                    ? getString(R.string.below) : getString(R.string.above);
            binding.aboveOrbelowTxt.setText(text);
            binding.spinner.setSelection(alarmModel.getPosition());
            binding.spinner2.setSelection(alarmModel.getStandardExchange());
            binding.deleteAlarm.setVisibility(View.VISIBLE);
            binding.deleteAlarm.setOnClickListener(clickListener);
            binding.alarmPriceEdit.setHint(MoneyUtil.fmt(alarmModel.getPrice()));
        }

        if(alarmModel == null && position!=-1){
            binding.spinner.setSelection(position);
        }


        View view = binding.getRoot();
        setListener();
        return view;
    }

    private String getSelectedPrice(int position, ExchangeRate exchangeRate){
        String price = null;
        switch (position){
            case FLAG_PRICE_BASE:
                price = MoneyUtil.fmt(exchangeRate.getPriceBase());
                break;
            case FLAG_PRICE_BUY:
                price = MoneyUtil.fmt(exchangeRate.getPriceBuy());
                break;
            case FLAG_PRICE_SELL:
                price = MoneyUtil.fmt(exchangeRate.getPriceSell());
                break;
            case FLAG_PRICE_SEND:
                price = MoneyUtil.fmt(exchangeRate.getPriceSend());
                break;
            case FLAG_PRICE_RECEIVE:
                price = MoneyUtil.fmt(exchangeRate.getPriceReceive());
                break;
        }

        return "기준환율 : "+MoneyUtil.fmt(price);
    }

    private void setListener(){
        binding.aboveOrbelowTxt.setOnClickListener(clickListener);
        binding.addAlarm.setOnClickListener(clickListener);
        binding.deleteAlarm.setOnClickListener(clickListener);
        binding.alarmPriceEdit.addTextChangedListener(watcher);
    }

    private TextWatcher watcher = new TextWatcher() {

        String result = "";

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            Log.d(TAG, "beforeTextChanged charSequence : "+s.toString()+" / start : "+start+" / count : "+count+" / after : "+after);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            Log.d(TAG, "onTextChanged charSequence : "+s.toString()+" / start : "+start+" / count : "+count);
            if(!s.toString().equals(result) && !s.toString().isEmpty()){     // StackOverflow를 막기위해,
                if(s.toString().length() == 1 && ".".equals(s.toString())){
                    return;
                }
                result = MoneyUtil.fmt(s.toString());
                binding.alarmPriceEdit.setText(result);                 // 결과 텍스트 셋팅.
                binding.alarmPriceEdit.setSelection(result.length());   // 커서를 제일 끝으로 보냄.
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            Log.d(TAG, "afterTextChanged Editable : "+s.toString());
        }
    };

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                // 이상 이하 swtch
                case R.id.aboveOrbelowTxt:
                    String text = binding.aboveOrbelowTxt.getText().equals(getString(R.string.above))
                            ? getString(R.string.below) : getString(R.string.above);
                    binding.aboveOrbelowTxt.setText(text);
                    break;

                // 환율 알림 제거
                case R.id.deleteAlarm:
                    RealmControllerU.deleteAlarm(realm, position);
                    onChangeDataListener.eventListener();
                    dismiss();
                    break;
                // 환율 알림 추가
                case R.id.addAlarm:
                    String priceText = MoneyUtil.removeCommas(binding.alarmPriceEdit.getText().toString());
                    if(priceText == null || priceText.isEmpty()){
                        Toast.makeText(getContext(), R.string.warning_empty_price, Toast.LENGTH_SHORT).show();
                        break;
                    }
                    ExchangeRate exchangeRate = (ExchangeRate) binding.spinner.getSelectedItem();
                    // true = 이상, false = 이하
                    boolean state = binding.aboveOrbelowTxt.getText().toString().equals("이상") ? true : false;
                    // price가 숫자인지에 대한 검증은 안해도된다 왜냐하면 EditText의 데이터 타입을 숫자로 설정 해놓았기 때문.
                    double price = Double.parseDouble(priceText);
                    int standardExchange = binding.spinner2.getSelectedItemPosition();
                    int countryPosition = binding.spinner.getSelectedItemPosition();

                    // 중복된 알람이 있는지 검증
                    if(RealmControllerU.isOverlap(realm, exchangeRate, state, price, standardExchange)){
                        Toast.makeText(getContext(), R.string.warning_overlap_alarm, Toast.LENGTH_SHORT).show();
                        break;
                    }

                    if(alarmModel != null){
                        RealmControllerU.updateAlarm(realm, exchangeRate, state, price, standardExchange, countryPosition, position);
                        dismiss();
                        break;
                    }
                    // 알람을 Realm에 저장한다.
                    RealmControllerU.addAlarm(realm, exchangeRate, state, price, standardExchange, countryPosition);
                    // 데이터가 등록되었으니 Dialog 창을 닫는다.
                    dismiss();
                    break;
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    public interface OnChangeDataListener {
        void eventListener();
    }

    public void setOnChangeDataListener(OnChangeDataListener listener){
        this.onChangeDataListener = listener;
    }
}
