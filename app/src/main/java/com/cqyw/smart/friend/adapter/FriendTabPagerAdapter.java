package com.cqyw.smart.friend.adapter;

import java.util.List;

import com.cqyw.smart.friend.fragment.FriendTabFragment;
import com.cqyw.smart.friend.model.FriendTab;
import com.cqyw.smart.widget.viewpaper.SlidingTabPagerAdapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;

public class FriendTabPagerAdapter extends SlidingTabPagerAdapter {

	@Override
	public int getCacheCount() {
		return FriendTab.values().length;
	}

	public FriendTabPagerAdapter(FragmentManager fm, Context context, ViewPager pager) {
		super(fm, FriendTab.values().length, context.getApplicationContext(), pager);

		for (FriendTab tab : FriendTab.values()) {
			try {
				FriendTabFragment fragment = null;

				List<Fragment> fs = fm.getFragments();
				if (fs != null) {
					for (Fragment f : fs) {
						if (f.getClass() == tab.clazz) {
							fragment = (FriendTabFragment) f;
							break;
						}
					}
				}

				if (fragment == null) {
					fragment = tab.clazz.newInstance();
				}

				fragment.setState(this);
				fragment.attachTabData(tab);

				fragments[tab.tabIndex] = fragment;
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public int getCount() {
		return FriendTab.values().length;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		FriendTab tab = FriendTab.fromTabIndex(position);

		int resId = tab != null ? tab.resId : 0;

		return resId != 0 ? context.getText(resId) : "";
	}

}