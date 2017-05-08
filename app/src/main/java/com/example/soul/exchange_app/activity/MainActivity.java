package com.example.soul.exchange_app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.soul.exchange_app.R;
import com.example.soul.exchange_app.adapter.CardAdapter;
import com.example.soul.exchange_app.adapter.ViewPagerAdapter;
import com.example.soul.exchange_app.manager.ViewPagerManager;
import com.example.soul.exchange_app.model.ExchangeRate;
import com.example.soul.exchange_app.paser.AsyncResponse;

import java.util.List;

public class MainActivity extends AppCompatActivity implements AsyncResponse {

    // view
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private FloatingActionButton fab;
    private final String TAG = getClass().getSimpleName();

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // view initialization
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewPager = (ViewPager)findViewById(R.id.viewpager);


        tabLayout = (TabLayout)findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        setupViewPager(viewPager);

        ViewPagerManager vpm = new ViewPagerManager();
        vpm.setOnEventListener(new ViewPagerManager.EventListener() {
            @Override
            public void onReceivedEvent(int position) {
                viewPager.setCurrentItem(position, true);
            }
        });
    }

    private void setupViewPager(ViewPager viewPager){

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new OneFragment(), getResources().getString(R.string.viewpager_tap_name_1));
        adapter.addFragment(new TwoFragment(), getResources().getString(R.string.viewpager_tap_name_2));
        adapter.addFragment(new ThreeFragment(), getResources().getString(R.string.viewpager_tap_name_3));
        viewPager.setOffscreenPageLimit(2);
        Log.d(TAG, "viewPager.getCurrentItem() >> "+viewPager.getCurrentItem());
        if(viewPager.getCurrentItem() == 0){
            Log.d(TAG, "Fab button 0 ");
            fab.show();
            moveNextActivityFAB(0);
        }
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position){
                    case 0:
                        Log.d(TAG, "Fab button 0 ");
                        fab.show();
                        moveNextActivityFAB(position);
                        break;
                    case 1:
                        Log.d(TAG, "Fab button 1 ");
                        fab.hide();
                        break;
                    case 2:
                        Log.d(TAG, "Fab button 2 ");
                        fab.show();
                        moveNextActivityFAB(position);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        viewPager.setAdapter(adapter);
    }

    private void moveNextActivityFAB(final int position){
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(position == 0){
                    Snackbar.make(view, "First Page!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    Intent intent = new Intent(getApplicationContext(), SetCountryActivity.class);
                    startActivity(intent);
                }else if(position == 2){
                    Snackbar.make(view, "Third Page!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void processFinish(List<ExchangeRate> mExchangeRates) {
        Log.d("MainActivity", "Data 들어와라"+ mExchangeRates.isEmpty());


    }

//    public void moveViewPager(int position){
//        viewPager.setCurrentItem(position, true);
//    }





}
