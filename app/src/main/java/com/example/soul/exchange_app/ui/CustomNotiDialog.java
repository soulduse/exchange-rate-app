package com.example.soul.exchange_app.ui;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.soul.exchange_app.R;
import com.example.soul.exchange_app.adapter.DialogAdapter;
import com.example.soul.exchange_app.adapter.DialogAdapter2;
import com.example.soul.exchange_app.realm.RealmController;

import io.realm.Realm;


/**
 * Created by soul on 2017. 6. 4..
 */

public class CustomNotiDialog extends DialogFragment implements AdapterView.OnItemSelectedListener{

    private Realm realm;
    private RealmController realmController;
    private DialogAdapter2 adapter;

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
        View v = inflater.inflate(R.layout.dialog_notification, container, false);
        Spinner countrySpinner = (Spinner) v.findViewById(R.id.spinner);
        Spinner exchangeSpinner = (Spinner) v.findViewById(R.id.spinner2);
        TextView aboveOrbelow = (TextView)v.findViewById(R.id.aboveOrbelowTxt);
        TextView addAlarm = (TextView)v.findViewById(R.id.addAlarm);
        TextView cancelAlarm = (TextView)v.findViewById(R.id.cancelAlarm);
        EditText alarmPrice = (EditText)v.findViewById(R.id.alarmPrice);

        //setadapter
        adapter = new DialogAdapter2(realmController.getExchangeRateExceptKorea(), getActivity());

        countrySpinner.setOnItemSelectedListener(this);
        countrySpinner.setAdapter(adapter);
        exchangeSpinner.setOnItemSelectedListener(this);


        return v;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        Toast.makeText(getContext(), "", Toast.LENGTH_SHORT).show();
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
