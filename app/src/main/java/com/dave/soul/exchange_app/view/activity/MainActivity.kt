package com.dave.soul.exchange_app.view.activity

import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import com.dave.soul.exchange_app.R
import com.dave.soul.exchange_app.manager.DataManager
import com.dave.soul.exchange_app.util.RestartAlarm
import com.dave.soul.exchange_app.view.adapter.ViewPagerAdapter
import com.dave.soul.exchange_app.view.fragment.OneFragment
import com.dave.soul.exchange_app.view.fragment.ThreeFragment
import com.dave.soul.exchange_app.view.fragment.TwoFragment
import com.dave.soul.exchange_app.view.ui.CustomNotificationDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {

    // view
    private var viewPager: ViewPager? = null
    private var tabLayout: TabLayout? = null
    private var fab: FloatingActionButton? = null
    private val TAG = javaClass.simpleName
    private var mPagerAdapter: ViewPagerAdapter? = null

    private var restartService: RestartService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initSettings()
        load()
        initBroadCast()
        initLayout()
    }

    private fun initSettings() {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
    }

    private fun initLayout() {
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        viewPager = findViewById<View>(R.id.viewpager) as ViewPager
        tabLayout = findViewById<View>(R.id.tabs) as TabLayout
        tabLayout!!.setupWithViewPager(viewPager)
        fab = findViewById<View>(R.id.fab) as FloatingActionButton
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
        startService()
    }

    // 서비스 시작
    private fun startService() {
        RestartAlarm.instance.registerRestartAlarm(this)
        Log.d(TAG, "startService()")
    }

    // 서비스 종료
    private fun stopService() {
        RestartAlarm.instance.unregisterRestartAlarm(this)
    }

    /**
     * 1. 데이터 파싱 요청을 한다.
     * 2. Network 연결 상태 체크
     * - connect    : 데이터 파싱 후 데이터를 Realm에 저장 or 갱신.
     * - disconnect : false 반환되고 realm에 저장된 데이터로 화면을 구성.
     * 3. Network & 파싱 모든 처리가 정상적으로 이루어 지면 리스너에서 setupViewPager를 해준다.
     */
    private fun load() {
        val parser = DataManager.newInstance(this).load()
        if (!parser) {
            initViewPager(false)
        }
    }

    /**
     * @param internet
     * 화면의 viewpager의 초기 설정을 도와준다.
     */
    fun initViewPager(internet: Boolean) {
        setupViewPager(viewPager!!)
        val msg =
            if (internet) getString(R.string.connect_internet) else getString(R.string.disconnect_internet)
        showSnackBar(msg)
    }

    private fun initBroadCast() {
        // 리스타트 서비스 생성
        restartService = RestartService()
        val intentFilter = IntentFilter("com.dave.soul.exchange_app.view.service.AlarmService")
        // 브로드 캐스트에 등록
        registerReceiver(restartService, intentFilter)
    }

    private fun setupViewPager(viewPager: ViewPager) {
        mPagerAdapter = ViewPagerAdapter(supportFragmentManager)
        mPagerAdapter!!.addFragment(
            OneFragment(),
            resources.getString(R.string.viewpager_tap_name_1)
        )
        mPagerAdapter!!.addFragment(
            TwoFragment(),
            resources.getString(R.string.viewpager_tap_name_2)
        )
        mPagerAdapter!!.addFragment(
            ThreeFragment(),
            resources.getString(R.string.viewpager_tap_name_3)
        )
        viewPager.offscreenPageLimit = 2
        Log.d(TAG, "viewPager.getCurrentItem() >> " + viewPager.currentItem)
        if (viewPager.currentItem == 0) {
            Log.d(TAG, "Fab button 0 ")
            fab!!.show()
            moveNextActivityFAB(0)
        }
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        Log.d(TAG, "Fab button 0 ")
                        fab!!.show()
                        moveNextActivityFAB(position)
                    }
                    1 -> {
                        Log.d(TAG, "Fab button 1 ")
                        fab!!.hide()
                    }
                    2 -> {
                        Log.d(TAG, "Fab button 2 ")
                        fab!!.show()
                        moveNextActivityFAB(position)
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })
        viewPager.adapter = mPagerAdapter
    }

    private fun moveNextActivityFAB(position: Int) {
        fab!!.setOnClickListener {
            if (position == 0) {
                stopService()
                val intent = Intent(applicationContext, SetCountryActivity::class.java)
                startActivity(intent)
            } else if (position == 2) {
                val notiDialog = CustomNotificationDialog.newInstance()
                notiDialog.show(supportFragmentManager, "dialog")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_settings) {
            val intent = Intent(applicationContext, SettingActivity::class.java)
            startActivity(intent)

            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun showSnackBar(msg: String) {
        val snackbar = Snackbar
            .make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG)
            .setAction("Action", null)
        snackbar.setActionTextColor(Color.RED)
        snackbar.show()
    }

    fun moveViewPager(position: Int) {
        viewPager!!.setCurrentItem(position, true)
        if (mPagerAdapter != null) {
            mPagerAdapter!!.notifyDataSetChanged()
            Log.d(TAG, "onResume notifyDataSetChanged!")
        }
    }

    public override fun onDestroy() {
        stopService()
        // 브로드 캐스트 해제
        unregisterReceiver(restartService)
        super.onDestroy()
    }
}
