package com.cqyw.smart.contact.extensioninfo.ui;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cqyw.smart.R;
import com.cqyw.smart.contact.extensioninfo.model.GalleryPhoto;
import com.netease.nim.uikit.common.adapter.TViewHolder;
import com.netease.nim.uikit.joycustom.upyun.JoyImageUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by Kairong on 2015/12/25.
 * mail:wangkrhust@gmail.com
 */
public class GalPhotoViewHolder extends TViewHolder {
    // view
    private ImageView galphoto_iv;

    // data
    private GalleryPhoto photo;

    private View.OnClickListener onClickListener;
    @Override
    protected void inflate() {
        galphoto_iv = (ImageView) view.findViewById(R.id.image_view);
    }

    @Override
    protected void refresh(Object item) {
        photo = (GalleryPhoto) item;
        ImageLoader.getInstance().displayImage(JoyImageUtil.getGalPhotosAbsUrl(photo.getUrl(), JoyImageUtil.ImageType.V_300), galphoto_iv, TempGalPhotoViewHolder.localImageOpt);
        setListener();
    }

    private void setListener() {
        if (onClickListener == null) {
            onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getAdapter().getOnClickEvent()!=null) {
                        getAdapter().getOnClickEvent().onClick(photo);
                    }
                }
            };
        }

        galphoto_iv.setOnClickListener(onClickListener);
    }

    public void refreshCurrentItem() {
        if (photo != null) {
            refresh(photo);
        }
    }

    @Override
    protected GalleryPhotoAdapter getAdapter() {
        return (GalleryPhotoAdapter) adapter;
    }

    @Override
    protected int getResId() {
        return R.layout.layout_galphoto_item;
    }
}
