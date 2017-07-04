package com.example.soul.exchange_app.activity;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.soul.exchange_app.R;


/**
 * Created by soul on 2017. 6. 22..
 */

public class SettingActivity extends AppCompatActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        toolbar = (Toolbar)findViewById(R.id.toolbar_setting);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle(getResources().getString(R.string.setting_actionbar_name));

        getFragmentManager().beginTransaction()
                .replace(R.id.content_preference, new SettingsFragment())
                .commit();

    }

    // PreferenceFragment 클래스 사용
    public class SettingsFragment extends
            PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        public static final String KEY_PREF_SHOW_GRAPH_TYPE     = "pref_showGraphType";
        public static final String KEY_PREF_REFRESH_TIME_TYPE   = "pref_refreshTimeType";
        public static final String KEY_PREF_ALARM_SWITCH        = "pref_alarmSwitch";
        public static final String KEY_PREF_ALARM_SOUND         = "pref_alarmSound";
        public static final String KEY_PREF_ALARM_VIBE          = "pref_alarmVibe";

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

//            setOnPreferenceChange(findPreference("pref_showGraphType"));
//            setOnPreferenceChange(findPreference("pref_refreshTimeType"));
//            setOnPreferenceChange(findPreference("pref_alarmSwitch"));
//            setOnPreferenceChange(findPreference("pref_alarmSound"));
//            setOnPreferenceChange(findPreference("pref_alarmVibe"));
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(KEY_PREF_SHOW_GRAPH_TYPE)) {
                Preference connectionPref = findPreference(key);
                // Set summary to be the user-description for the selected value
                connectionPref.setSummary(sharedPreferences.getString(key, ""));
            }else if(key.equals(KEY_PREF_REFRESH_TIME_TYPE)){

            }else if(key.equals(KEY_PREF_ALARM_SWITCH)){

            }else if(key.equals(KEY_PREF_ALARM_SOUND)){

            }else if(key.equals(KEY_PREF_ALARM_VIBE)){

            }

        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void setOnPreferenceChange(Preference mPreference) {
        mPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);

        onPreferenceChangeListener.onPreferenceChange(mPreference,
                PreferenceManager.getDefaultSharedPreferences(mPreference.getContext()).getString(mPreference.getKey(), ""));
    }

    private Preference.OnPreferenceChangeListener onPreferenceChangeListener = new Preference.OnPreferenceChangeListener() {

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = newValue.toString();

            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
            }
            return true;
        }
    };
}
