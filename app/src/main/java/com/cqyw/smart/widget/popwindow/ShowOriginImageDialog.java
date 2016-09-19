package com.cqyw.smart.widget.popwindow;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.cqyw.smart.R;
import com.cqyw.smart.main.model.PublicSnapMessage;
import com.netease.nim.uikit.common.ui.imageview.MultiTouchZoomableImageView;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.netease.nim.uikit.joycustom.upyun.JoyImageUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;

/**
 * Created by Kairong on 2016/9/18.
 * mail:wangkrhust@gmail.com
 */
public class ShowOriginImageDialog extends Dialog {
    private static DisplayImageOptions imageOptions = new DisplayImageOptions.Builder()
            .showImageOnFail(R.drawable.joy_image_download_failed)
            .cacheInMemory(false)
            .cacheOnDisk(true)
            .build();
    private MultiTouchZoomableImageView biggerHead_iv;
    private PublicSnapMessage message;
    private ImageView thumbnail;
    private ProgressBar progressBar;
    private ImageView temp;

    private Bitmap bitmap;

    private Context context;

    private View contentView;

    private boolean isLoadingImage = false;

    private long lastClickTime = 0;
    private long lastDownTime = 0;
    private float x, y;

    private Handler handler = new Handler();

    public ShowOriginImageDialog(Context context) {
        super(context, R.style.Dialog_fullscreen);
        this.context = context;
    }

    public void setMessage(PublicSnapMessage message) {
        this.message = message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contentView = LayoutInflater.from(context).inflate(R.layout.activity_watch_origin_image, null, false);
        setContentView(contentView);
        initView();
    }


    private Runnable dismissRunnable = new Runnable() {
        @Override
        public void run() {
            dismiss();
        }
    };

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getPointerCount() == 1 && ev.getAction() == MotionEvent.ACTION_DOWN) {
            lastDownTime = System.currentTimeMillis();
            x = ev.getX();y = ev.getY();
            handler.removeCallbacks(dismissRunnable);
        } else if(ev.getPointerCount() == 1 && (ev.getAction() == MotionEvent.ACTION_CANCEL || ev.getAction() == MotionEvent.ACTION_UP)) {
            if (System.currentTimeMillis() - lastDownTime <= 200){
                float cx = ev.getX(), cy = ev.getY();
                double dis = Math.sqrt((cx-x)*(cx-x)+(cy-y)*(cy-y));
                LogUtil.d("Dialog", dis+"");
                if (dis < 10) {
                    if (System.currentTimeMillis() - lastClickTime > 500)handler.postDelayed(dismissRunnable, 300);
                    lastClickTime = System.currentTimeMillis();
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    protected void initView() {
        biggerHead_iv = findView(R.id.woi_origin_image_iv);
        thumbnail = findView(R.id.woi_thumbnail_iv);
        progressBar = findView(R.id.woi_progress_bar_pb);

        final File file = ImageLoader.getInstance().getDiskCache().get(JoyImageUtil.getCoverAbsUrl(message.getCover(), JoyImageUtil.ImageType.V_COVERSOURCE));
        if (file != null && file.exists()){
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    bitmap = BitmapFactory.decodeFile(file.getPath());
                    biggerHead_iv.setImageBitmap(bitmap);
                }
            });
        } else {
            thumbnail.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            ImageLoader.getInstance().displayImage(JoyImageUtil.getCoverAbsUrl(message.getCover(), JoyImageUtil.ImageType.V_4TO3), thumbnail);
            temp = new ImageView(context);
            isLoadingImage = true;
            ImageLoader.getInstance().displayImage(JoyImageUtil.getCoverAbsUrl(message.getCover(), JoyImageUtil.ImageType.V_COVERSOURCE), temp,
                    imageOptions, new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            thumbnail.setVisibility(View.GONE);
                            progressBar.setVisibility(View.GONE);
                            biggerHead_iv.setImageBitmap(loadedImage);
                            isLoadingImage = false;
                        }
                    });
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();

        if (isLoadingImage) {
            ImageLoader.getInstance().cancelDisplayTask(temp);
        }

        if (bitmap != null && !bitmap.isRecycled()){
            bitmap.recycle();
            bitmap = null;
        }
    }

    @Override
    public void show() {
        super.show();

        //set the dialog fullscreen
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) contentView
                .getLayoutParams();
        layoutParams.width = ScreenUtil.screenWidth;
        layoutParams.height = ScreenUtil.screenHeight;
        contentView.setLayoutParams(layoutParams);
    }

    protected <T extends View> T findView(int resId) {
        return (T) (contentView.findViewById(resId));
    }
}
