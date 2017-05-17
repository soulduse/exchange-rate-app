package com.example.soul.exchange_app.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.soul.exchange_app.R;
import com.example.soul.exchange_app.databinding.FragmentTwoBinding;
import com.example.soul.exchange_app.model.CalcuCountries;
import com.example.soul.exchange_app.model.ExchangeRate;
import com.example.soul.exchange_app.paser.ExchangeInfo;
import com.example.soul.exchange_app.realm.RealmController;
import com.example.soul.exchange_app.util.MoneyUtil;

import java.util.List;

import io.realm.Realm;

/**
 * Created by soul on 2017. 2. 24..
 */

public class TwoFragment  extends Fragment {

    private RealmController realmController;
    private Realm realm;
    private final String TAG = getClass().getSimpleName();
    private FragmentTwoBinding binding;
    private List<ExchangeRate> exchangeList;
    private double selectedPriceFirst, selectedPriceSecond;


    public TwoFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_two, container, false);
        binding.setFragment(this);

        realmController = RealmController.getInstance();
        realmController.setRealm();
        realm = realmController.getRealm();

        Log.d(TAG, "realmController.getSizeOfCalcu() ?? "+ realmController.getSizeOfCalcu());
        if(realmController.getSizeOfCalcu() == 0){
            realmController.setCalcuCountry(ExchangeInfo.USD, ExchangeInfo.KRW);
        }else{
            String [] counties = realmController.getCalcuCountriesName();
            realmController.setCalcuCountry(counties[0], counties[1]);
            Log.d(TAG, "getCalcuCountriesName >> " +counties[0]+"/"+counties[1]);
        }

        CalcuCountries calcuCountries = realmController.getCalcuCountries();
        exchangeList = calcuCountries.getExchangeRates();
//        setDataofKorea(exchangeList);
        Log.d(TAG, "exchangeList size : "+ exchangeList.size());
        if(exchangeList.size() != 0){
            binding.name1.setText(exchangeList.get(0).getCountryAbbr());
            binding.name2.setText(exchangeList.get(1).getCountryAbbr());
            Glide.with(getContext()).load(exchangeList.get(0).getThumbnail()).into(binding.flag1);
            Glide.with(getContext()).load(exchangeList.get(1).getThumbnail()).into(binding.flag2);
        }

        View view = binding.getRoot();
        setupButtons();
        return view;
    }

    @Override
    public void onDestroyView() {
        realm.close();
        super.onDestroyView();
    }

    private View.OnClickListener onNumberClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(selectedPriceFirst == 0 && selectedPriceSecond == 0){
                selectedPriceFirst   = exchangeList.get(0).getPriceBase();
                selectedPriceSecond = exchangeList.get(1).getPriceBase();
            }

            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                if(tv.getId() == R.id.select_option){       // 환율 계산기존 변경
                    selectOptions();
                }
                else if(tv.getId() == R.id.button_clean){   // 숫자 모두 지우기
                    clearNum();
                }
                else{
                    clickNum(tv.getText().toString());      // 숫자 입력
                }

            }else if(v instanceof ImageView){
                ImageView iv = (ImageView)v;
                clickEtc(iv.getId());
            }
        }
    };


    private void clickNum(String s) {
        Editable editable = binding.editText.getText();

        if((editable.length()<17 && editable.length() >= 0) && !editable.toString().contains(".") && !s.equals(".")){
            binding.editText.setText(MoneyUtil.fmt(editable+s));
            double data = MoneyUtil.calMoney(selectedPriceFirst, selectedPriceSecond,editable+s);
            binding.editText2.setText(MoneyUtil.fmt(data));
        }else if (editable.length() <= 0 && s.equals(".")){
            binding.editText.setText("0.");
        }else if((editable.length()<19 && editable.length() >= 1) && (s.equals(".")||editable.toString().contains("."))){
            if(MoneyUtil.checkNumLength(MoneyUtil.removeCommas(editable+s))){
                binding.editText.setText(editable+s);
                double data = MoneyUtil.calMoney(selectedPriceFirst, selectedPriceSecond,editable+s);
                binding.editText2.setText(MoneyUtil.fmt(data));
            }else{
                printSnackbar("소수점 이하 2자까지 입력할 수 있습니다.");
            }
        }else{
            printSnackbar("너무 큰 범위의 숫자 입니다");
        }
    }

    private void clearNum(){
        binding.editText.getText().clear();
        binding.editText2.getText().clear();
    }

    private void selectOptions(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.select_option);
        builder.setItems(R.array.price_options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                printSnackbar("selected basePrice >> "+which);
                // 0-매매기준율, 1-살때, 2-팔때, 3-보낼때, 4-받을때
                Resources res = getResources();
                String[] titles= res.getStringArray(R.array.price_options);

                switch (which){
                    case 0:
                        selectedPriceFirst   = exchangeList.get(0).getPriceBase();
                        selectedPriceSecond = exchangeList.get(1).getPriceBase();
                        break;
                    case 1:
                        selectedPriceFirst   = exchangeList.get(0).getPriceBuy();
                        selectedPriceSecond = exchangeList.get(1).getPriceBuy();
                        break;
                    case 2:
                        selectedPriceFirst   = exchangeList.get(0).getPriceSell();
                        selectedPriceSecond = exchangeList.get(1).getPriceSell();
                        break;
                    case 3:
                        selectedPriceFirst   = exchangeList.get(0).getPriceSend();
                        selectedPriceSecond = exchangeList.get(1).getPriceSend();
                        break;
                    case 4:
                        selectedPriceFirst   = exchangeList.get(0).getPriceReceive();
                        selectedPriceSecond = exchangeList.get(1).getPriceReceive();
                        break;
                }

                binding.selectOption.setText(titles[which]);
                if(binding.editText.getText().length() != 0){
                    binding.editText.setText(binding.editText.getText());
                    double data = MoneyUtil.calMoney(selectedPriceFirst, selectedPriceSecond,binding.editText.getText().toString());
                    binding.editText2.setText(MoneyUtil.fmt(data));
                }
            }
        });
        builder.show();
    }

    private void clickEtc(int id){
        switch (id){
            // 한 숫자씩 지우기
            case R.id.button_backspace:
                Editable text = binding.editText.getText();
                String msg = MoneyUtil.removeCommas(text+"");
                if(msg.length()>1){
                    msg = msg.substring(0, msg.length()-1);
                    String addedCommasNumbers = MoneyUtil.fmt(Double.parseDouble(msg));
                    Log.d(TAG, "msg : "+msg+" / addedCommasNumbers : "+addedCommasNumbers);
                    double data = MoneyUtil.calMoney(selectedPriceFirst, selectedPriceSecond,addedCommasNumbers);
                    binding.editText2.setText(MoneyUtil.fmt(data));
                    binding.editText.setText(addedCommasNumbers);
                }else if(text.length() <= 1){
                    binding.editText.getText().clear();
                    binding.editText2.getText().clear();
                }
                break;
            case R.id.button_swap:
                break;
            case R.id.button_share:
                printSnackbar("준비중인 기능입니다.");
                break;
        }
    }

    private void setupButtons() {
        // it is for calculator
        binding.buttonOne.setOnClickListener(onNumberClickListener);
        binding.buttonTwo.setOnClickListener(onNumberClickListener);
        binding.buttonThree.setOnClickListener(onNumberClickListener);
        binding.buttonFour.setOnClickListener(onNumberClickListener);
        binding.buttonFive.setOnClickListener(onNumberClickListener);
        binding.buttonSix.setOnClickListener(onNumberClickListener);
        binding.buttonSeven.setOnClickListener(onNumberClickListener);
        binding.buttonEight.setOnClickListener(onNumberClickListener);
        binding.buttonNine.setOnClickListener(onNumberClickListener);
        binding.buttonZero.setOnClickListener(onNumberClickListener);
        binding.buttonDoubleZero.setOnClickListener(onNumberClickListener);
        binding.buttonDot.setOnClickListener(onNumberClickListener);

        // etc stuffs
        binding.buttonClean.setOnClickListener(onNumberClickListener);
        binding.buttonBackspace.setOnClickListener(onNumberClickListener);
        binding.buttonSwap.setOnClickListener(onNumberClickListener);
        binding.buttonShare.setOnClickListener(onNumberClickListener);
        binding.selectOption.setOnClickListener(onNumberClickListener);
    }

    private void printSnackbar(String msg){
        Snackbar.make(getView(), msg, Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
    }
}