package com.dave.soul.exchange_app.view.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dave.soul.exchange_app.R;
import com.dave.soul.exchange_app.realm.RealmController;
import com.dave.soul.exchange_app.view.adapter.SetCountryAdapter;

import io.realm.Realm;

/**
 * Created by soul on 2017. 3. 20..
 */

public class SetCountryActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();
    private RecyclerView recyclerView;
    private SetCountryAdapter adapter;
    private Realm realm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_country);

        realm = Realm.getDefaultInstance();

        // view initialization
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_second);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle(getResources().getString(R.string.set_actionbar_name));
        recyclerView = (RecyclerView)findViewById(R.id.recycler_view_frag_set);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 1);
        mLayoutManager.supportsPredictiveItemAnimations();
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new SetCountryAdapter(RealmController.getExchangeRateExceptKorea(realm), getApplicationContext());
        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_set, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.select_all:
                Log.d(TAG, "selected option item");
                RealmController.changeAllSelected(realm, true);
                break;

            case R.id.deselect_all:
                Log.d(TAG, "deselected option item");
                RealmController.changeAllSelected(realm, false);

                break;
        }
        return super.onOptionsItemSelected(item);
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
