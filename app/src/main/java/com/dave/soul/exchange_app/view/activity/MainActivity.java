package com.dave.soul.exchange_app.view.activity;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import com.dave.soul.exchange_app.R;
import com.dave.soul.exchange_app.view.adapter.ViewPagerAdapter;
import com.dave.soul.exchange_app.manager.DataManager;
import com.dave.soul.exchange_app.view.fragment.OneFragment;
import com.dave.soul.exchange_app.view.fragment.ThreeFragment;
import com.dave.soul.exchange_app.view.fragment.TwoFragment;
import com.dave.soul.exchange_app.view.ui.CustomNotificationDialog;
import com.dave.soul.exchange_app.util.RestartAlarm;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class MainActivity extends AppCompatActivity {

    // view
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private FloatingActionButton fab;
    private final String TAG = getClass().getSimpleName();
    private ViewPagerAdapter mPagerAdapter;

    private AdView mAdView;

    private RestartService restartService;


    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        MobileAds.initialize(getApplicationContext(), getString(R.string.banner_app_unit_id));

        viewPager = (ViewPager)findViewById(R.id.viewpager);

        tabLayout = (TabLayout)findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        mAdView = (AdView)findViewById(R.id.adView1);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        fab = (FloatingActionButton) findViewById(R.id.fab);
        load();
        initBroadCast();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        startService();
    }

    // 서비스 시작
    private void startService(){
        RestartAlarm.Companion.getInstance().registerRestartAlarm(this);
        Log.d(TAG, "startService()");
    }

    // 서비스 종료
    private void stopService(){
        RestartAlarm.Companion.getInstance().unregisterRestartAlarm(this);
    }

    /**
     * 1. 데이터 파싱 요청을 한다.
     * 2. Network 연결 상태 체크
     *      - connect    : 데이터 파싱 후 데이터를 Realm에 저장 or 갱신.
     *      - disconnect : false 반환되고 realm에 저장된 데이터로 화면을 구성.
     * 3. Network & 파싱 모든 처리가 정상적으로 이루어 지면 리스너에서 setupViewPager를 해준다.
     */
    private void load(){
        boolean parser = DataManager.newInstance(this).load();
        if(!parser){
            initViewPager(false);
        }
    }

    /**
     * @param internet
     * 화면의 viewpager의 초기 설정을 도와준다.
     */
    public void initViewPager(boolean internet){
        setupViewPager(viewPager);
        String msg = internet ? getString(R.string.connect_internet) : getString(R.string.disconnect_internet);
        showSnackBar(msg);
    }

    private void initBroadCast(){
        //리스타트 서비스 생성
        restartService = new RestartService();
        IntentFilter intentFilter = new IntentFilter("com.dave.soul.exchange_app.view.service.AlarmService");
        //브로드 캐스트에 등록
        registerReceiver(restartService,intentFilter);

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
                    stopService();
                    Intent intent = new Intent(getApplicationContext(), SetCountryActivity.class);
                    startActivity(intent);
                }else if(position == 2){
                    CustomNotificationDialog notiDialog = CustomNotificationDialog.newInstance();
                    notiDialog.show(getSupportFragmentManager(), "dialog");
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showSnackBar(String msg){
        Snackbar snackbar = Snackbar
                .make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG)
                .setAction("Action", null);
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
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        stopService();
        //브로드 캐스트 해제
        unregisterReceiver(restartService);
        super.onDestroy();
    }
}

