package com.example.soul.exchange_app.ui;

import android.databinding.DataBindingUtil;
import android.databinding.adapters.ToolbarBindingAdapter;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.soul.exchange_app.R;
import com.example.soul.exchange_app.adapter.DialogAdapter2;
import com.example.soul.exchange_app.databinding.DialogNotificationBinding;
import com.example.soul.exchange_app.model.ExchangeRate;
import com.example.soul.exchange_app.realm.RealmController;
import com.example.soul.exchange_app.util.MoneyUtil;

import io.realm.Realm;


/**
 * Created by soul on 2017. 6. 4..
 */

public class CustomNotiDialog extends DialogFragment implements AdapterView.OnItemSelectedListener{

    private static final String TAG = CustomNotiDialog.class.getSimpleName();
    private Realm realm;
    private RealmController realmController;
    private DialogNotificationBinding binding;


    /**
     * Create a new instance of CountryDialog, providing "num"
     * as an argument.
     */
    public static CustomNotiDialog newInstance() {
        return new CustomNotiDialog();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Pick a style based on the num.
        int style = DialogFragment.STYLE_NORMAL, theme = 0;

        realmController = RealmController.getInstance();
        realmController.setRealm();
        realm = realmController.getRealm();

        setStyle(style, theme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_notification, container, false);
        binding.setDialog(this);

        DialogAdapter2 countryAdapter               = new DialogAdapter2(realmController.getExchangeRateExceptKorea(), getActivity());
        ArrayAdapter<CharSequence> exchangeAdapter  = ArrayAdapter.createFromResource(getContext(),
                R.array.price_options, android.R.layout.simple_spinner_item);
        exchangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        binding.spinner.setAdapter(countryAdapter);
        binding.spinner2.setAdapter(exchangeAdapter);

        View view = binding.getRoot();
        setListener();
        return view;
    }

    private void setListener(){
        binding.spinner.setOnItemSelectedListener(this);
        binding.spinner2.setOnItemSelectedListener(this);

        binding.aboveOrbelowTxt.setOnClickListener(clickListener);
        binding.addAlarm.setOnClickListener(clickListener);
        binding.deleteAlarm.setOnClickListener(clickListener);
    }

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
                    Toast.makeText(getContext(), "삭제", Toast.LENGTH_SHORT).show();
                    dismiss();
                    break;
                // 환율 알림 추가
                case R.id.addAlarm:
                    String priceText = binding.alarmPriceEdit.getText().toString();
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

                    // 중복된 알람이 있는지 검증
                    if(realmController.isOverlap(exchangeRate, state, price, standardExchange)){
                        Toast.makeText(getContext(), R.string.warning_overlap_alarm, Toast.LENGTH_SHORT).show();
                        break;
                    }

                    // 알람을 Realm에 저장한다.
                    realmController.addAlarm(exchangeRate, state, price, standardExchange);
                    // 데이터가 등록되었으니 Dialog 창을 닫는다.
                    dismiss();
                    break;
            }
        }
    };


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {


    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
