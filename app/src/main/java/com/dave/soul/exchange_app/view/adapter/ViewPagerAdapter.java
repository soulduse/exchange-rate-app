package com.dave.soul.exchange_app.view.adapter;

import android.os.Parcelable;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by soul on 2017. 2. 24..
 */

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private final String TAG = getClass().getSimpleName();
    private final List<Fragment> mFragmentList      = new ArrayList<>();
    private final List<String> mFragmentTitleList   = new ArrayList<>();


    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }


    @Override
    public int getItemPosition(Object object) {
        Log.d(TAG, "getItemPosition Object : "+object.toString());
        return POSITION_NONE;
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        super.restoreState(state, loader);
    }



    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:

        }
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }



    public void addFragment(Fragment fragment, String title){
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }
}
