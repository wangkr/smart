package com.cqyw.smart.contact.extensioninfo.ui;

import android.content.Context;

import com.cqyw.smart.contact.extensioninfo.model.GalleryPhoto;
import com.cqyw.smart.contact.extensioninfo.model.GalleryPhotoTemp;
import com.netease.nim.uikit.common.adapter.TAdapter;
import com.netease.nim.uikit.common.adapter.TAdapterDelegate;

import java.util.List;

/**
 * Created by Kairong on 2015/12/26.
 * mail:wangkrhust@gmail.com
 */
public class TempGalleryPhotoAdapter extends TAdapter<GalleryPhotoTemp> {
    private OnGalPhotoClickEvent onGalPhotoClickEvent;

    public TempGalleryPhotoAdapter(Context context, List<GalleryPhotoTemp> items, TAdapterDelegate delegate) {
        super(context, items, delegate);
    }

    public void setOnGalPhotoClickEvent(OnGalPhotoClickEvent onGalPhotoClickEvent) {
        this.onGalPhotoClickEvent = onGalPhotoClickEvent;
    }

    public OnGalPhotoClickEvent getOnGalPhotoClickEvent() {
        return onGalPhotoClickEvent;
    }

    public interface OnGalPhotoClickEvent{
        void onChangeClick(GalleryPhotoTemp photo);
        void onDeleteClick(GalleryPhotoTemp photo);
        void onAddPhoto(Context context);
    }
}
