package com.dave.soul.exchange_app.view.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.dave.soul.exchange_app.R;
import com.dave.soul.exchange_app.databinding.FragmentTwoBinding;
import com.dave.soul.exchange_app.manager.DataManager;
import com.dave.soul.exchange_app.model.CalcuCountries;
import com.dave.soul.exchange_app.model.ExchangeRate;
import com.dave.soul.exchange_app.paser.ExchangeInfo;
import com.dave.soul.exchange_app.realm.RealmController;
import com.dave.soul.exchange_app.util.KakaoLinkUtil;
import com.dave.soul.exchange_app.util.MoneyUtil;
import com.dave.soul.exchange_app.view.adapter.DialogAdapter;
import com.dave.soul.exchange_app.view.ui.CountryDialog;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import io.realm.Realm;


/**
 * Created by soul on 2017. 2. 24..
 */

public class TwoFragment  extends Fragment {

    private Realm realm;
    private final String TAG = getClass().getSimpleName();
    private FragmentTwoBinding binding;
    private List<ExchangeRate> exchangeList;
    private double selectedPriceFirst, selectedPriceSecond;
    private CountryDialog myDialogFragment;
    private int position;
    private int selectedPrice;

    private int dataSwapFirst   = 0;
    private int dataSwapSecond  = 1;


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

        realm = Realm.getDefaultInstance();
        binding.editText.setTextDirection(View.TEXT_DIRECTION_RTL);
        binding.editText2.setTextDirection(View.TEXT_DIRECTION_RTL);

        Log.d(TAG, "realmController.getSizeOfCalcu() ?? "+ RealmController.getSizeOfCalcu(realm));
        if(RealmController.getSizeOfCalcu(realm) == 0){
            RealmController.setCalcuCountry(realm, ExchangeInfo.USD, ExchangeInfo.KRW);
        }else{
            String [] counties = RealmController.getCalcuCountriesName(realm);
            RealmController.setCalcuCountry(realm, counties[0], counties[1]);
            Log.d(TAG, "getCalcuCountriesName >> " +counties[0]+"/"+counties[1]);
        }

        CalcuCountries calcuCountries = RealmController.getCalcuCountries(realm);
        exchangeList = calcuCountries.getExchangeRates();
//        setDataofKorea(exchangeList);
        Log.d(TAG, "exchangeList size : "+ exchangeList.size());
        if(exchangeList.size() != 0){
            binding.name1.setText(exchangeList.get(dataSwapFirst).getCountryAbbr());
            binding.name2.setText(exchangeList.get(dataSwapSecond).getCountryAbbr());
            Glide.with(getContext()).load(exchangeList.get(dataSwapFirst).getThumbnail()).into(binding.flag1);
            Glide.with(getContext()).load(exchangeList.get(dataSwapSecond).getThumbnail()).into(binding.flag2);
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
                selectedPriceFirst   = exchangeList.get(dataSwapFirst).getPriceBase();
                selectedPriceSecond = exchangeList.get(dataSwapSecond).getPriceBase();
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
        builder.setItems(R.array.pref_priceOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 0-매매기준율, 1-살때, 2-팔때, 3-보낼때, 4-받을때
                Resources res = getResources();
                String[] titles= res.getStringArray(R.array.pref_priceOptions);
                selectedPrice = which;

                selectedPriceFirst  = DataManager.getInstance().getPrice(selectedPrice, exchangeList.get(dataSwapFirst));
                selectedPriceSecond = DataManager.getInstance().getPrice(selectedPrice, exchangeList.get(dataSwapSecond));

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


    // position 0 = 첫번째 나라 / 1 = 두번째 나라 선택
    private void selectCountry(int position){
        Log.d(TAG, "selectCountry");
        this.position = position;
        myDialogFragment = new CountryDialog(mListener);
        myDialogFragment.show(getFragmentManager() ,"TAG");
    }

    DialogAdapter.OnItemClickListener mListener = new DialogAdapter.OnItemClickListener() {
        @Override
        public void onItemClicked(ExchangeRate result, int itemPosition) {
            Log.d(TAG, "이벤트 리스너 받다 : "+result.getCountryAbbr());

            if(position == 0){
                binding.name1.setText(result.getCountryAbbr());
                Glide.with(getContext()).load(result.getThumbnail()).into(binding.flag1);
                selectedPriceFirst  = DataManager.getInstance().getPrice(selectedPrice, result);
                dataSwapFirst = itemPosition;
            }else{
                binding.name2.setText(result.getCountryAbbr());
                Glide.with(getContext()).load(result.getThumbnail()).into(binding.flag2);
                selectedPriceSecond = DataManager.getInstance().getPrice(selectedPrice, result);
                dataSwapSecond = itemPosition;
            }

            if(binding.editText.getText().length() != 0){
                binding.editText.setText(binding.editText.getText());
                double data = MoneyUtil.calMoney(selectedPriceFirst, selectedPriceSecond,binding.editText.getText().toString());
                binding.editText2.setText(MoneyUtil.fmt(data));
            }

            myDialogFragment.dismiss();
        }
    };

    private void clickEtc(int id){

        Log.d(TAG," clicked!");
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
            // 위아래 스왑
            case R.id.button_swap:
                int tempSwapNum = dataSwapFirst;
                dataSwapFirst = dataSwapSecond;
                dataSwapSecond = tempSwapNum;
//                if(dataSwapFirst != 0){
//                    dataSwapFirst = 0;
//                }else{
//                    dataSwapFirst = 1;
//                }
//
//                if(dataSwapSecond != 0){
//                    dataSwapSecond = 0;
//                }else{
//                    dataSwapSecond = 1;
//                }

                selectedPriceFirst   = exchangeList.get(dataSwapFirst).getPriceBase();
                selectedPriceSecond = exchangeList.get(dataSwapSecond).getPriceBase();

                if(binding.editText.getText().length() != 0){
                    binding.editText.setText(binding.editText.getText());
                    double data = MoneyUtil.calMoney(selectedPriceFirst, selectedPriceSecond,binding.editText.getText().toString());
                    binding.editText2.setText(MoneyUtil.fmt(data));
                }

                if(exchangeList.size() != 0){
                    binding.name1.setText(exchangeList.get(dataSwapFirst).getCountryAbbr());
                    binding.name2.setText(exchangeList.get(dataSwapSecond).getCountryAbbr());
                    Glide.with(getContext()).load(exchangeList.get(dataSwapFirst).getThumbnail()).into(binding.flag1);
                    Glide.with(getContext()).load(exchangeList.get(dataSwapSecond).getThumbnail()).into(binding.flag2);
                }

                break;
            case R.id.button_share:
                if(isEmptyNumber()){
                    printSnackbar("공유할 데이터를 입력해주세요.");
                    break;
                }

                // 카카오톡 사용이 가능하다면 카카오 링크로 공유
                if(KakaoLinkUtil.INSTANCE.isKakaoTalkSharingAvailable(getContext())){
                    KakaoLinkUtil.INSTANCE.sendLink(getContext(), getLinkTitle(), getLinkDiscription());
                }else{
                    Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                    intent.setType("text/plain");

                    // Set default text message
                    // 카톡, 이메일, MMS 다 이걸로 설정 가능
                    String subject = "환율 알리미 ["+getLinkTitle()+"]";
                    String discription = "\n"+getLinkDiscription().replace("-","");
                    intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                    intent.putExtra(Intent.EXTRA_TEXT, discription+"\n 자세히 보기 \n"+KakaoLinkUtil.INSTANCE.getPLAY_STORE_URL());

                    // Title of intent
                    Intent chooser = Intent.createChooser(intent, "친구에게 공유하기");
                    startActivity(chooser);
                }



                break;
            case R.id.flag1:
                selectCountry(0);
                break;
            case R.id.flag2:
                selectCountry(1);
                break;
        }
    }

    private boolean isEmptyNumber(){
        if(binding.editText.getText().length() <= 0 || binding.editText2.getText().length() <= 0){
            return true;
        }
        return false;
    }

    private String getLinkTitle(){
        return binding.selectOption.getText().toString();
    }

    private String getLinkDiscription(){
        String firstCountryValue    = binding.name1.getText().toString()+" : "+binding.editText.getText();
        String secondCountryValue   = binding.name2.getText().toString()+" : "+binding.editText2.getText();

        return firstCountryValue+"\n"+
                secondCountryValue;
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

        binding.flag1.setOnClickListener(onNumberClickListener);
        binding.flag2.setOnClickListener(onNumberClickListener);
    }

    private void printSnackbar(String msg){
        Snackbar.make(getView(), msg, Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
    }
}
