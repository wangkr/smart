package com.cqyw.smart.main.adapter;

import android.content.Context;
import android.widget.TextView;

import com.netease.nim.uikit.common.adapter.TAdapter;
import com.netease.nim.uikit.common.adapter.TAdapterDelegate;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import java.util.List;

/**
 * Created by Kairong on 2016/1/2.
 * mail:wangkrhust@gmail.com
 */
public class SimpleUserInfoAdapter extends TAdapter<NimUserInfo> {
    private OnClickEvent onClickEvent;

    public SimpleUserInfoAdapter(Context context, List<NimUserInfo> items, TAdapterDelegate delegate) {
        super(context, items, delegate);
    }

    public void setOnClickEvent(OnClickEvent onClickEvent) {
        this.onClickEvent = onClickEvent;
    }

    public OnClickEvent getOnClickEvent() {
        return onClickEvent;
    }

    public interface OnClickEvent{
        void onAvatarClick(NimUserInfo userInfo);
        void onAddFriendClick(NimUserInfo userInfo);
    }
}
