package com.example.soul.exchange_app.ui;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.soul.exchange_app.R;


/**
 * Created by soul on 2017. 6. 4..
 */

public class CustomNotiDialog extends DialogFragment {

    private String title;

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

        setStyle(style, theme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_notification, container, false);
        Spinner countrySpinner = (Spinner) v.findViewById(R.id.spinner);
        Spinner exchangeSpinner = (Spinner) v.findViewById(R.id.spinner2);
        TextView aboveOrbelow = (TextView)v.findViewById(R.id.aboveOrbelowTxt);
        TextView addBtn = (TextView)v.findViewById(R.id.)


        return v;
    }
}
