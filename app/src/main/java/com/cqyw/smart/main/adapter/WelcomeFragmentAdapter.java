package com.cqyw.smart.main.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.cqyw.smart.main.fragment.WelcomeFragment;

/**
 * Created by Kairong on 2015/11/10.
 * mail:wangkrhust@gmail.com
 */
public class WelcomeFragmentAdapter extends FragmentPagerAdapter {

    public WelcomeFragmentAdapter(FragmentManager fm){
        super(fm);
    }

    private int mCount = WelcomeFragment.welcome_fragment_bgs.length;
    @Override
    public Fragment getItem(int position) {
        return WelcomeFragment.newInstance(position);
    }

    @Override
    public int getCount() {
        return mCount;
    }

}
