package com.cqyw.smart.main.adapter;

import android.content.Context;

import com.netease.nim.uikit.common.adapter.TAdapter;
import com.netease.nim.uikit.common.adapter.TAdapterDelegate;


import java.util.List;

/**
 * 用户点赞头像的适配器
 * Created by Kairong on 2015/11/14.
 * mail:wangkrhust@gmail.com
 */
public class LikeHeadImageAdapter extends TAdapter<String> {
    private int defaultIconResId;
    private boolean ifshowmore = false;

    private ViewHolderEventListener holderEventListener;


    public LikeHeadImageAdapter(Context context, List<String> items, TAdapterDelegate delegate){
        super(context, items, delegate);
    }

    public boolean ifShowMore(){
        return ifshowmore;
    }

    public void setIfshowmore(boolean ifshowmore) {
        this.ifshowmore = ifshowmore;
    }

    public void setHolderEventListener(ViewHolderEventListener holderEventListener) {
        this.holderEventListener = holderEventListener;
    }

    public ViewHolderEventListener getHolderEventListener() {
        return holderEventListener;
    }

    public interface ViewHolderEventListener {
        void onAvatarClick(String account);
    }
}
