package com.example.soul.exchange_app.activity;

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

import com.example.soul.exchange_app.R;
import com.example.soul.exchange_app.databinding.FragmentTwoBinding;
import com.example.soul.exchange_app.paser.ExchangeInfo;
import com.example.soul.exchange_app.realm.RealmController;
import com.example.soul.exchange_app.util.MoneyUtil;

import io.realm.Realm;

/**
 * Created by soul on 2017. 2. 24..
 */

public class TwoFragment  extends Fragment {

    private RealmController realmController;
    private Realm realm;
    private final String TAG = getClass().getSimpleName();
    private FragmentTwoBinding binding;
    private boolean lastDot = false;

    public TwoFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realmController = RealmController.getInstance();
        realm = realmController.getRealm();
        if(realmController.getSizeOfCalcu() == 0){
            realmController.setCalcuCountry(ExchangeInfo.USD, ExchangeInfo.KRW);
        }else{
            String [] counties = realmController.getCalcuCountries();
            realmController.setCalcuCountry(counties[0], counties[1]);
            Log.d(TAG, "getCalcuCountries >> " +counties[0]+"/"+counties[1]);
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_two, container, false);
        binding.setFragment(this);

        View view = binding.getRoot();
        setupButtons();
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
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
    }

    private View.OnClickListener onNumberClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                if(!tv.getText().toString().equalsIgnoreCase("C")){
                    clickNum(tv.getText().toString());
                }
                else{
                    clearNum();
                }
            }else if(v instanceof ImageView){
                ImageView iv = (ImageView)v;
                clickEtc(iv.getId());
            }
        }
    };

    private void clickNum(String s) {
        if(binding.editText.getText().length()<19){
            String msg = MoneyUtil.removeCommas(binding.editText.getText()+s);
            String addedCommasNumbers = null;
            if(!s.equals(".")){
                addedCommasNumbers = MoneyUtil.fmt(Double.parseDouble(msg));
            }else{
                if(!MoneyUtil.fmt(Double.parseDouble(msg)).contains(".")){
                    addedCommasNumbers = MoneyUtil.fmt(Double.parseDouble(msg))+s;
                }
            }
            binding.editText.setText(addedCommasNumbers);
        }else{
            printSnackbar("너무 큰 범위의 숫자 입니다");
        }
    }

    private void clearNum(){
        binding.editText.setText("0");
    }

    private void clickEtc(int id){
        switch (id){
            case R.id.button_backspace:
                Editable text = binding.editText.getText();

                String msg = MoneyUtil.removeCommas(text+"");


                if(msg.length()>1){
                    msg = msg.substring(0, msg.length()-1);
                    String addedCommasNumbers = MoneyUtil.fmt(Double.parseDouble(msg));
                    Log.d(TAG, "msg : "+msg+" / addedCommasNumbers : "+addedCommasNumbers);
                    binding.editText.setText(addedCommasNumbers);
                }else if(text.length() <= 1){
                    binding.editText.setText("0");
                }
                break;
            case R.id.button_swap:
                break;
            case R.id.button_share:
                printSnackbar("준비중인 기능입니다.");
                break;
        }

    }

    private void backSpace(String s){

    }


    private void printSnackbar(String msg){
        Snackbar.make(getView(), msg, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }
}