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
import com.example.soul.exchange_app.manager.ParserManager;
import com.example.soul.exchange_app.manager.DataManager;
import com.example.soul.exchange_app.realm.RealmController;
import com.example.soul.exchange_app.ui.CustomNotiDialog;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity {

    // view
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private FloatingActionButton fab;
    private final String TAG = getClass().getSimpleName();
    private ViewPagerAdapter mPagerAdapter;

    // data
    private ParserManager parserManager;

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
        parserManager = new ParserManager();

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

    @Override
    protected void onStart() {
        super.onStart();
        // start service
        startService();
    }

    // 서비스 시작
    private void startService(){
        Intent intent = new Intent(getApplicationContext(), AlarmService.class);
        startService(intent);
    }

    // 서비스 종료
    private void stopService(){
        Intent intent = new Intent(getApplicationContext(), AlarmService.class);
        stopService(intent);
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
