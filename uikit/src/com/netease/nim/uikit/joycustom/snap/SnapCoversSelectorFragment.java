package com.netease.nim.uikit.joycustom.snap;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.fragment.TFragment;

/**
 * Created by Kairong on 2015/11/3.
 * mail:wangkrhust@gmail.com
 */
public class SnapCoversSelectorFragment extends TFragment {

    private SnapCoverSeletorAdapter adapter;

    private ViewPager pager;

    private TabLayout tabLayout;

    public static int selectedPagerIndex = -1;

    public static int selectedCoverIndex = -1;

    private int currentPosition = 0;

    @Override
    public int getContainerId() {
        return R.id.snap_cover_container;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.snap_cover_selector_fragment, container, false);
        initView(view);
        return view;
    }

    private TabLayout.OnTabSelectedListener onTabReselectedListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            currentPosition = tab.getPosition();
            adapter.onPageSelected(currentPosition);
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {

        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }
    };

    public static int getSelectedCoverIndex() {
        return selectedCoverIndex;
    }

    public static int getSelectedPagerIndex() {
        return selectedPagerIndex;
    }

    private void initView(View view){
        pager = (ViewPager)view.findViewById(R.id.pager);
        adapter = new SnapCoverSeletorAdapter(getFragmentManager(), SnapConstant.getCount());
        tabLayout = (TabLayout) view.findViewById(R.id.tab);

        pager.setAdapter(adapter);
        tabLayout.setupWithViewPager(pager);
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.joy_theme_color));
        tabLayout.setTabTextColors(getResources().getColor(R.color.color_black_333333), getResources().getColor(R.color.joy_theme_color));

        tabLayout.setOnTabSelectedListener(onTabReselectedListener);
    }

    public boolean onBackPressed() {
        return false;
    }

    class SnapCoverSeletorAdapter extends FragmentPagerAdapter{
        protected SnapCoverFragment[] fragments;

        public SnapCoverSeletorAdapter(FragmentManager fm, int count){
            super(fm);
            fragments = new SnapCoverFragment[count];
            for(SnapConstant.SnapCover cover: SnapConstant.SnapCover.values()){
                fragments[cover.index] = SnapCoverFragment.newInstance(getContext(),cover.index,
                        SnapConstant.getSnapCoverIds(cover), SnapConstant.getSnapCoverCategory(cover));
            }
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position % getCount()];
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return SnapConstant.getSnapCoverCategory(position);
        }

        @Override
        public int getCount() {
            return fragments.length;
        }


        public void onPageSelected(int position) {
            SnapCoverFragment fragment = getFragmentByPosition(position);

            // INSTANCE
            if (fragment == null) {
                return;
            }

            fragment.onCurrent();
        }

        public void onPageScrolled(int position) {
            SnapCoverFragment fragment = getFragmentByPosition(position);

            // INSTANCE
            if (fragment == null) {
                return;
            }

            fragment.onCurrent();
        }

        private SnapCoverFragment getFragmentByPosition(int position) {
            // IDX
            if (position < 0 || position >= fragments.length) {
                return null;
            }
            return fragments[position];
        }

    }

}
