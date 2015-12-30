package com.cqyw.smart.contact.extensioninfo.ui;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cqyw.smart.R;
import com.cqyw.smart.contact.extensioninfo.model.GalleryPhotoTemp;
import com.netease.nim.uikit.common.adapter.TViewHolder;
import com.netease.nim.uikit.joycustom.upyun.JoyImageUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.download.ImageDownloader;

/**
 * Created by Kairong on 2015/12/26.
 * mail:wangkrhust@gmail.com
 */
public class TempGalPhotoViewHolder extends TViewHolder {
    public static DisplayImageOptions localImageOpt = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.joy_cover_loading_medium)
            .showImageOnFail(R.drawable.joy_image_download_failed_medium)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .build();
    // view
    private ImageView galphoto_iv;
    private LinearLayout menu_ll;
    private ImageView chang_menu_iv;
    private ImageView delete_menu_iv;

    // data
    private GalleryPhotoTemp photoTemp;

    private View.OnClickListener onClickListener;
    private View.OnClickListener onChangeListener;
    private View.OnClickListener onDeleteListener;
    @Override
    protected void inflate() {
        menu_ll = (LinearLayout) view.findViewById(R.id.menu_layout);
        chang_menu_iv = (ImageView)menu_ll.findViewById(R.id.chang_picture);
        delete_menu_iv = (ImageView)menu_ll.findViewById(R.id.delete_picture);
        menu_ll.setVisibility(View.GONE);

        galphoto_iv = (ImageView) view.findViewById(R.id.image_view);
    }

    @Override
    protected void refresh(Object item) {
        photoTemp = (GalleryPhotoTemp) item;

        photoTemp.setShownMenu(false);
        menu_ll.setVisibility(View.GONE);

        setImageView();
        setListener();
    }

    public void refreshCurrentItem() {
        if (photoTemp != null) {
            refresh(photoTemp);
        }
    }

    private String wrapThumbPath(String path) {
        if (!TextUtils.isEmpty(path)) {
            if (path.charAt(0) == '/') {
                return ImageDownloader.Scheme.FILE.wrap(path);
            } else {
                return "file:///"+path;
            }
        }

        return null;
    }

    private void setImageView() {
        if (photoTemp.isAddBtn()) {
            ImageLoader.getInstance().displayImage("drawable://"+R.drawable.add_galphoto_icn, galphoto_iv);
        } else {
            if (photoTemp.isLocal()) {
                ImageLoader.getInstance().displayImage(wrapThumbPath(photoTemp.getThumbPath()), galphoto_iv, localImageOpt);
            } else {
                // 显示缩略图
                ImageLoader.getInstance().displayImage(JoyImageUtil.getGalPhotosAbsUrl(photoTemp.getUrl(), JoyImageUtil.ImageType.V_300), galphoto_iv, TempGalPhotoViewHolder.localImageOpt);
            }
        }
    }

    private void setListener() {
        if (onClickListener == null) {
            onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getAdapter().getOnGalPhotoClickEvent() != null) {
                        if (photoTemp.isAddBtn()) {
                            getAdapter().getOnGalPhotoClickEvent().onAddPhoto(context);
                        } else {
                            menu_ll.setVisibility(photoTemp.isShownMenu() ? View.GONE : View.VISIBLE);
                            photoTemp.setShownMenu(!photoTemp.isShownMenu());
                        }
                    }
                }
            };
        }
        galphoto_iv.setOnClickListener(onClickListener);

        if (onChangeListener == null) {
            onChangeListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getAdapter().getOnGalPhotoClickEvent() != null) {
                        getAdapter().getOnGalPhotoClickEvent().onChangeClick(photoTemp);
                    }
                }
            };
        }
        chang_menu_iv.setOnClickListener(onChangeListener);

        if (onDeleteListener == null) {
            onDeleteListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getAdapter().getOnGalPhotoClickEvent() != null) {
                        getAdapter().getOnGalPhotoClickEvent().onDeleteClick(photoTemp);
                    }
                }
            };
        }
        delete_menu_iv.setOnClickListener(onDeleteListener);

    }

    @Override
    protected TempGalleryPhotoAdapter getAdapter() {
        return (TempGalleryPhotoAdapter) adapter;
    }

    @Override
    protected int getResId() {
        return R.layout.layout_temp_galphoto_item;
    }
}
