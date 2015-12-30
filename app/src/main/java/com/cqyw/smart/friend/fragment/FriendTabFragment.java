package com.cqyw.smart.friend.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cqyw.smart.friend.model.FriendTab;
import com.netease.nim.uikit.common.fragment.TabFragment;
import com.cqyw.smart.R;


public abstract class FriendTabFragment extends TabFragment {

    private boolean loaded = false;

    private FriendTab tabData;

    protected abstract void onInit();

    protected boolean inited() {
        return loaded;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_container_friend_tab, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void attachTabData(FriendTab tabData) {
        this.tabData = tabData;
    }

    @Override
    public void onCurrent() {
        super.onCurrent();
        
        if (!loaded && loadRealLayout()) {
            loaded = true;
            onInit();
        }
    }

    private boolean loadRealLayout() {
        ViewGroup root = (ViewGroup) getView();
        if (root != null) {
            root.removeAllViewsInLayout();
            View.inflate(root.getContext(), tabData.layoutId, root);
        }
        return root != null;
    }
}
