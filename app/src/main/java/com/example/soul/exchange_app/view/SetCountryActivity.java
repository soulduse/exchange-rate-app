package com.example.soul.exchange_app.view;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import com.example.soul.exchange_app.R;

/**
 * Created by soul on 2017. 3. 20..
 */

public class SetCountryActivity extends Activity {

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_country);

        recyclerView = (RecyclerView)findViewById(R.id.recycler_view_frag_set);


    }
}
