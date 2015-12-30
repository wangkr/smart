package com.cqyw.smart.contact.extensioninfo.ui;

import android.content.Context;

import com.cqyw.smart.contact.extensioninfo.model.GalleryPhoto;
import com.netease.nim.uikit.common.adapter.TAdapter;
import com.netease.nim.uikit.common.adapter.TAdapterDelegate;

import java.util.List;

/**
 * Created by Kairong on 2015/12/25.
 * mail:wangkrhust@gmail.com
 */
public class GalleryPhotoAdapter extends TAdapter<GalleryPhoto> {
    private OnClickEvent onClickEvent;
    public GalleryPhotoAdapter(Context context, List<GalleryPhoto> items, TAdapterDelegate delegate) {
        super(context, items, delegate);
    }

    public void setOnClickEvent(OnClickEvent onClickEvent) {
        this.onClickEvent = onClickEvent;
    }

    public OnClickEvent getOnClickEvent() {
        return onClickEvent;
    }

    public interface OnClickEvent{
        void onClick(GalleryPhoto photo);
    }
}
