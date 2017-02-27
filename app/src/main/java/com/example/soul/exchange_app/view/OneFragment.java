package com.example.soul.exchange_app.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.soul.exchange_app.R;
import com.example.soul.exchange_app.paser.ExchangeDataParser;

/**
 * Created by soul on 2017. 2. 24..
 */

public class OneFragment extends Fragment {

    // view
    private Button parserBtn;
    private TextView reaserchTxt;

    // data
    private ExchangeDataParser exchangeDataParser;


    public OneFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_one, container, false);
        parserBtn       = (Button)view.findViewById(R.id.parser_btn);
        reaserchTxt     = (TextView)view.findViewById(R.id.reaserch_text);

        // data initialization
        exchangeDataParser = new ExchangeDataParser();

        parserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(reaserchTxt != null && !reaserchTxt.getText().equals("")){
                    reaserchTxt.setText("");
                }
                exchangeDataParser.excuteDataAsync(reaserchTxt, view);
//                reaserchTxt.setText(exchangeDataParser.getParserString());
//                List<String> perCountryList = paser.getParserList();
//                for(int i=0; i<perCountryList.size(); i++){
//                    perCountryList.get(i);
//                }
            }
        });
        return view;
    }
}
