package com.cqyw.smart.contact.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.cqyw.smart.JActionBarActivity;
import com.cqyw.smart.R;
import com.netease.nim.uikit.cache.NimUserInfoCache;
import com.netease.nim.uikit.common.ui.imageview.MultiTouchZoomableImageView;
import com.netease.nim.uikit.joycustom.upyun.JoyImageUtil;
import com.netease.nim.uikit.session.constant.Extras;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

/**
 * Created by Kairong on 2015/12/15.
 * mail:wangkrhust@gmail.com
 */
public class WatchBiggerHeadImageActivity extends JActionBarActivity implements View.OnClickListener{
    private MultiTouchZoomableImageView biggerHead_iv;
    private NimUserInfo userInfo;
    private ImageView temp;
    public static void start(Context context, String account) {
        Intent intent = new Intent();
        intent.setClass(context, WatchBiggerHeadImageActivity.class);
        intent.putExtra(Extras.EXTRA_ACCOUNT, account);
        context.startActivity(intent);
    }
    @Override
    protected void initStyle() {
        super.initStyle();
        setContentView(R.layout.activity_watchbigger_headimage);
    }

    @Override
    protected void initData() {
        String account = getIntent().getStringExtra(Extras.EXTRA_ACCOUNT);
        userInfo = NimUserInfoCache.getInstance().getUserInfo(account);
    }

    @Override
    protected void initView() {
        biggerHead_iv = findView(R.id.biggerhead_imageview);
        temp = new ImageView(this);
        ImageLoader.getInstance().displayImage(JoyImageUtil.getHeadImageAbsUrl(userInfo.getAvatar(), JoyImageUtil.ImageType.V_ORG), temp ,
                new SimpleImageLoadingListener(){
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                biggerHead_iv.setImageBitmap(loadedImage);
            }
        });

        biggerHead_iv.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onBackPressed();
    }
}
