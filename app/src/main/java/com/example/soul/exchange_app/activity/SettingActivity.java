package com.example.soul.exchange_app.activity;


import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

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

        FrameLayout frameLayout = (FrameLayout)findViewById(R.id.content_preference);

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

}
