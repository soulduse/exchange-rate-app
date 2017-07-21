package com.example.soul.exchange_app.activity;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.example.soul.exchange_app.R;


/**
 * Created by soul on 2017. 6. 22..
 */

public class SettingActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private static final String TAG = SettingActivity.class.getSimpleName();

    public static final String KEY_PREF_SHOW_GRAPH_TYPE     = "pref_showGraphType";
    public static final String KEY_PREF_REFRESH_TIME_TYPE   = "pref_refreshTimeType";
    public static final String KEY_PREF_ALARM_SWITCH        = "pref_alarmSwitch";
    public static final String KEY_PREF_ALARM_SOUND         = "pref_alarmSound";
    public static final String KEY_PREF_ALARM_VIBE          = "pref_alarmVibe";

    private SettingsFragment settingsFragment;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        toolbar = (Toolbar)findViewById(R.id.toolbar_setting);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle(getResources().getString(R.string.setting_actionbar_name));

        settingsFragment = new SettingsFragment();
        getFragmentManager().beginTransaction()
                .replace(R.id.content_preference, settingsFragment)
                .commit();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(listener);

    }


    private SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Preference connectionPref = settingsFragment.findPreference(key);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            if (sharedPreferences instanceof ListPreference) {
                String value = sharedPreferences.getString(key, "");
                editor.putString(key, value);
                connectionPref.setSummary(value);
            }else if(sharedPreferences instanceof CheckBoxPreference){
                boolean check = sharedPreferences.getBoolean(key, false);
                editor.putBoolean(key, check);
                CheckBoxPreference checkPref = (CheckBoxPreference)connectionPref;
                checkPref.setChecked(check);
            }else if(sharedPreferences instanceof SwitchPreference){
                boolean switched = sharedPreferences.getBoolean(key, false);
                editor.putBoolean(key, switched);
                SwitchPreference switchPref = (SwitchPreference)connectionPref;
                switchPref.setEnabled(sharedPreferences.getBoolean(key, switched));
            }

            editor.commit();
            /*
            if (key.equals(KEY_PREF_SHOW_GRAPH_TYPE)) {
                Log.d(TAG, "KEY_PREF_SHOW_GRAPH_TYPE 진입");
            }else if(key.equals(KEY_PREF_REFRESH_TIME_TYPE)){
                Log.d(TAG, "KEY_PREF_REFRESH_TIME_TYPE 진입");
            }else if(key.equals(KEY_PREF_ALARM_SWITCH)){
                Log.d(TAG, "KEY_PREF_ALARM_SWITCH 진입");
            }else if(key.equals(KEY_PREF_ALARM_SOUND)){
                Log.d(TAG, "KEY_PREF_ALARM_SOUND 진입");
            }else if(key.equals(KEY_PREF_ALARM_VIBE)){
                Log.d(TAG, "KEY_PREF_ALARM_VIBE 진입");
            }
            */
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        settingsFragment.getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        settingsFragment.getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(listener);
    }

    // PreferenceFragment 클래스 사용
    public class SettingsFragment extends PreferenceFragment  {

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
