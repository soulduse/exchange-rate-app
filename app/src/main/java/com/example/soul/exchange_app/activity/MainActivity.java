package com.example.soul.exchange_app.activity;

import android.content.Intent;
import android.graphics.Color;
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
import com.example.soul.exchange_app.adapter.ViewPagerAdapter;
import com.example.soul.exchange_app.manager.DataManager;
import com.example.soul.exchange_app.model.ExchangeRate;
import com.example.soul.exchange_app.paser.ExchangeParser;
import com.example.soul.exchange_app.realm.RealmController;
import com.example.soul.exchange_app.ui.CustomNotiDialog;
import com.example.soul.exchange_app.util.NetworkUtil;

import java.util.List;
import java.util.concurrent.Callable;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity {

    // view
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private FloatingActionButton fab;
    private final String TAG = getClass().getSimpleName();
    private ViewPagerAdapter mPagerAdapter;

    // data
    private DataManager dataManager;

    // Realm
    private Realm realm;
    private RealmController realmController;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // view initialization
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewPager = (ViewPager)findViewById(R.id.viewpager);
        dataManager = new DataManager();

        realmController = RealmController.with(getApplicationContext());
        realm = realmController.getRealm();

        if(realm.isClosed()){
            realmController.setRealm();
            realm = realmController.getRealm();
        }

        tabLayout = (TabLayout)findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        load();

    }

    private void setupViewPager(ViewPager viewPager){

        mPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mPagerAdapter.addFragment(new OneFragment(), getResources().getString(R.string.viewpager_tap_name_1));
        mPagerAdapter.addFragment(new TwoFragment(), getResources().getString(R.string.viewpager_tap_name_2));
        mPagerAdapter.addFragment(new ThreeFragment(), getResources().getString(R.string.viewpager_tap_name_3));
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
        viewPager.setAdapter(mPagerAdapter);
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
                    CustomNotiDialog notiDialog = CustomNotiDialog.newInstance();
                    notiDialog.show(getSupportFragmentManager(), "dialog");
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

    /**
     네트워크 연결상태에 따른 어디서 데이터를 가져올 것인가에 대한 구분 (두 가지 경우의 수가 있다.)
     - network connect       : parsing data를 가져온다.
     - network disconnect    : Realm DB에서 내용을 가져온다.
     */
    public void load() {
        if(NetworkUtil.isNetworkConnected(getApplicationContext())){
            Callable<List<ExchangeRate>> callable = new Callable<List<ExchangeRate>>() {
                @Override
                public List<ExchangeRate> call() throws Exception {
                    return getParserDataList();
                }
            };

            dataManager.getAsyncExecutor()
                    .setCallable(callable)
                    .setCallback(callback)
                    .execute();
        }else{
            setupViewPager(viewPager);
            showSnackBar("No internet connection!");
        }
        // 비동기로 실행될 코드List<ExchangeRate> mExchangeDatas
    }

    private List<ExchangeRate> getParserDataList(){
        return new ExchangeParser().getParserDatas();
    }

    // 비동기로 실행된 결과를 받아 처리하는 코드
    private DataManager.AsyncCallback<List<ExchangeRate>> callback = new DataManager.AsyncCallback<List<ExchangeRate>>() {
        @Override
        public void onResult(List<ExchangeRate> result) {
            realmController.setRealmDatas(result);
            setupViewPager(viewPager);
            Log.d(TAG, "realmController.getExchangeRate() : "+realmController.getExchangeRate().toString());
            showSnackBar("Update success!");
        }

        @Override
        public void exceptionOccured(Exception e) {
            Log.d(TAG, "exceptionOccured : "+e.getMessage());
        }

        @Override
        public void cancelled() {
            Log.d(TAG, "cancelled");
        }
    };

    private void showSnackBar(String msg){
        Snackbar snackbar = Snackbar
                .make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG)
                .setAction("Action", null);
        // Changing message text color
        snackbar.setActionTextColor(Color.RED);
        snackbar.show();
    }

    public void moveViewPager(int position){
        viewPager.setCurrentItem(position, true);
        if (!(mPagerAdapter == null)) {
            mPagerAdapter.notifyDataSetChanged();
            Log.d(TAG, "onResume notifyDataSetChanged!");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null) {
            realm.close();
            realm = null;
        }
    }
}
