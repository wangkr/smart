package com.netease.nim.uikit.common.media.picker.joycamera;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.media.picker.joycamera.model.CamOnLineRes;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.download.ImageDownloader;


/**
 * Created by Kairong on 2016/8/7.
 * mail:wangkrhust@gmail.com
 */
public class ThumbImageHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    private ImageView image_icn;
    private ImageView download_icn;
    private ProgressBar progressBar;

    private CamOnLineRes item;
    private int position;
    private OnThumbClickEvent onThumbClickEvent;

    public interface OnThumbClickEvent {
        void onDownload(CamOnLineRes item, int position);
        void onSelect(CamOnLineRes item);
    }
    private static DisplayImageOptions options = new DisplayImageOptions.Builder()
            .cacheOnDisk(true)
            .cacheInMemory(true)
            .showImageOnLoading(R.drawable.cam_onlineres_default)
            .build();
    public ThumbImageHolder(View itemView, OnThumbClickEvent onThumbClickEvent) {
        super(itemView);
        this.onThumbClickEvent = onThumbClickEvent;
        inflate(itemView);
    }

    @Override
    public void onClick(View v) {
        if (item == null) return;
        switch (item.getStatus()) {
            case NONE:
            case PART:
                if (onThumbClickEvent != null){
                    onThumbClickEvent.onDownload(item, position);
                }
                break;
            case DEFAULT:
            case COMPLETE:
                if (onThumbClickEvent != null) {
                    onThumbClickEvent.onSelect(item);
                }
                break;
        }
    }

    private void inflate(View view){
        image_icn = (ImageView)view.findViewById(R.id.img_list_item);
        download_icn = (ImageView)view.findViewById(R.id.download_vs);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar_pb);
        view.setOnClickListener(this);
    }

    public void refresh(final CamOnLineRes item, final int position, final OnThumbClickEvent onThumbClickEvent){
        this.item = item;
        this.position = position;
        this.onThumbClickEvent = onThumbClickEvent;
        switch (item.getStatus()){
            case DEFAULT:
            case COMPLETE:
                ImageLoader.getInstance().displayImage(ImageDownloader.Scheme.FILE.wrap(item.getIconCachePath()), image_icn, options);
                download_icn.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                break;
            case NONE:
            case PART:
                ImageLoader.getInstance().displayImage(item.getIconUrl(), image_icn, options);
                download_icn.setVisibility(View.VISIBLE);
                break;
            case DOWNLOADING:
                ImageLoader.getInstance().displayImage(item.getIconUrl(), image_icn, options);
                download_icn.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                break;
        }
    }


}
