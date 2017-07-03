package com.example.soul.exchange_app.activity;


import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.soul.exchange_app.R;


/**
 * Created by soul on 2017. 6. 22..
 */

public class SettingActivity extends AppCompatActivity {

    private Toolbar toolbar;
    public static final String KEY_PREF_SYNC_CONN = "pref_syncConnectionType";

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
    public static class SettingsFragment extends
            PreferenceFragment {
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
