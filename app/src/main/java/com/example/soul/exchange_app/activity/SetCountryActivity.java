package com.example.soul.exchange_app.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.example.soul.exchange_app.R;
import com.example.soul.exchange_app.adapter.SetCountryAdapter;
import com.example.soul.exchange_app.realm.RealmController;

import io.realm.Realm;

/**
 * Created by soul on 2017. 3. 20..
 */

public class SetCountryActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();
    private RecyclerView recyclerView;
    private SetCountryAdapter adapter;
    private Realm realm;
    private RealmController realmController;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_country);

        realm = Realm.getDefaultInstance();
        realmController = RealmController.getInstance();

        // view initialization
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_second);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        recyclerView = (RecyclerView)findViewById(R.id.recycler_view_frag_set);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 1);
        mLayoutManager.supportsPredictiveItemAnimations();
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new SetCountryAdapter(realmController.getExchangeRate(), getApplicationContext());
        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
