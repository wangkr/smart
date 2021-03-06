package com.cqyw.smart.contact.localcontact;

import android.content.Context;

import com.netease.nim.uikit.common.adapter.TAdapter;
import com.netease.nim.uikit.common.adapter.TAdapterDelegate;

import java.util.List;

/**
 * Created by Kairong on 2015/11/28.
 * mail:wangkrhust@gmail.com
 */
public class LocalContactAdapter extends TAdapter<LocalContact> {
    private OnClickEvent onClickEvent;

    public LocalContactAdapter(Context context, List<LocalContact> items, TAdapterDelegate delegate) {
        super(context, items, delegate);
    }

    public void setOnClickEvent(OnClickEvent onClickEvent) {
        this.onClickEvent = onClickEvent;
    }

    public OnClickEvent getOnClickEvent() {
        return onClickEvent;
    }

    public interface OnClickEvent{
        void onAvatarClick(LocalContact localContact);
    }
}
