package com.netease.nim.uikit.session.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.activity.TActionBarActivity;
import com.netease.nim.uikit.common.ui.dialog.CustomAlertDialog;
import com.netease.nim.uikit.common.ui.widget.squareprogressbar.SquareProgressBar;
import com.netease.nim.uikit.common.util.media.BitmapDecoder;
import com.netease.nim.uikit.common.util.media.ImageUtil;
import com.netease.nim.uikit.joycustom.snap.SnapConstant;
import com.netease.nim.uikit.session.extension.SnapChatAttachment;
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import java.io.File;
import java.io.IOException;



/**
 * 查看阅后即焚消息原图
 */
public class WatchSnapChatSmartActivity extends TActionBarActivity {
    public static final String INTENT_EXTRA_MESSAGE = "INTENT_EXTRA_MESSAGE";

    private IMMessage message;
    private MyCounter counter;
    private static File smartfile;

    private View loadingLayout;
    private TextView counter_tv;
    private SquareProgressBar image;
    private static Bitmap srcBitmap;
    private ActionBar actionBar;
    protected CustomAlertDialog alertDialog;

    private static final long COUNT_DOWN_TIME = 5000;

    private static final long COUNT_DOWN_INT = 100;

    private static boolean destroy = false;

    public static boolean hasSeen = false;

    public static void start(Context context, IMMessage message, int requestCode) {
        Intent intent = new Intent();
        intent.putExtra(INTENT_EXTRA_MESSAGE, message);
        intent.setClass(context, WatchSnapChatSmartActivity.class);
        ((Activity)context).startActivityForResult(intent, requestCode);
    }

    public void destroy() {
        destroy = true;
        deleteSmart();
        recycle();

        Intent intent = new Intent();
        intent.putExtra(INTENT_EXTRA_MESSAGE, message);
        setResult(RESULT_OK, intent);
        finish();
    }

    private static void deleteSmart() {
        if (smartfile != null){
            smartfile.delete();
            smartfile = null;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SECURE);

        onParseIntent();
        setContentView(R.layout.activity_watch_snapchat_picture);
        findViews();

        counter = new MyCounter(COUNT_DOWN_TIME, COUNT_DOWN_INT);
        requestOriImage();

        hasSeen = false;
        destroy = false;
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (destroy) {
                return;
            }
            switch (msg.what) {
                case 0:
                    onDownloadSuccess(message);
                    break;
                case 1:
                    onDownloadFailed();
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        smartfile = null;
    }

    private void onParseIntent() {
        this.message = (IMMessage) getIntent().getSerializableExtra(INTENT_EXTRA_MESSAGE);
    }

    private void findViews() {
        alertDialog = new CustomAlertDialog(this);
        loadingLayout = findView(R.id.loading_layout);
        counter_tv = findView(R.id.counter_text);
        counter_tv.setVisibility(View.GONE);
        initSquareProgressBar();
        actionBar = getSupportActionBar();
        if (actionBar == null){
            return;
        }
        actionBar.hide();

        counter_tv.setText("5");
    }

    private void initSquareProgressBar(){
        image = (SquareProgressBar) findViewById(R.id.watch_image_view);
        image.setColorRGB(getResources().getColor(R.color.joy_theme_color));
        image.setWidth(1);
        image.setProgress(100);// 50*100ms=5s
    }

    private void requestOriImage() {
        onDownloadStart(message);
    }

    private boolean isOriginImageHasDownloaded(final IMMessage message) {
        if (message.getAttachStatus() == AttachStatusEnum.transferred) {
            return true;
        }

        return false;
    }

    /**
     * ******************************** 设置图片 *********************************
     */

    private void setThumbnail() {
        image.setImageBitmap(ImageUtil.getBitmapFromDrawableRes(getImageResOnLoading()));
    }

    private void setImageView(final IMMessage msg) {
        if (smartfile == null) {
            return;
        }
        srcBitmap = ImageUtil.getScreenFitBitmap(smartfile);
        if (srcBitmap == null) {
            Toast.makeText(this, R.string.picker_image_error, Toast.LENGTH_LONG).show();
            image.setImageBitmap(ImageUtil.getBitmapFromDrawableRes(getImageResOnFailed()));
            hasSeen = false;
        } else {
            image.setImageBitmap(srcBitmap);
            hasSeen = true;
            counter_tv.setVisibility(View.VISIBLE);
            counter.start();
        }
    }

    private int getImageResOnLoading() {
        return R.drawable.joy_cover_loading_medium;
    }

    private int getImageResOnFailed() {
        return R.drawable.joy_image_download_failed;
    }

    /**
     * ********************************* 下载 ****************************************
     */


    private void onDownloadStart(final IMMessage msg) {
        setThumbnail();
        downSmartFile(((SnapChatAttachment)msg.getAttachment()).getUrl());
    }

    private void onDownloadSuccess(final IMMessage msg) {
        loadingLayout.setVisibility(View.GONE);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!destroy) {
                    setImageView(msg);
                }
            }
        },100);

    }

    /**
     * 下载smart图
     * @param urlName
     */
    private void downSmartFile(final String urlName){
        loadingLayout.setVisibility(View.VISIBLE);
        handler.post(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int i = 2;
                        smartfile = SnapConstant.downloadSmartImage(urlName);
                        while ( !destroy && smartfile == null && i > 0) {
                            smartfile = SnapConstant.downloadSmartImage(urlName);
                            i--;
                        }
                        if (smartfile != null) {
                            try {
                                if (destroy) {
                                    deleteSmart();
                                    return;
                                } else {
                                    // 临时文件创建成功
                                    smartfile.createNewFile();
                                    handler.sendEmptyMessage(0);
                                }
                            }catch (IOException e){
                                e.getMessage();
                                deleteSmart();
                                handler.sendEmptyMessage(1);
                            }
                        } else {
                            handler.sendEmptyMessage(1);
                        }
                    }
                }).start();
            }
        });
    }

    private void onDownloadFailed() {
        loadingLayout.setVisibility(View.GONE);
        image.setImageBitmap(ImageUtil.getBitmapFromDrawableRes(getImageResOnFailed()));
        Toast.makeText(this, R.string.download_picture_fail, Toast.LENGTH_LONG).show();
    }

    public void recycle() {
        if (srcBitmap != null && !srcBitmap.isRecycled())
            srcBitmap.recycle();
        srcBitmap = null;
    }

    /* 定义一个倒计时的内部类 */
    class MyCounter extends CountDownTimer {
        private long countDownInterval = 1000;
        private float factor = 1.0f;
        public MyCounter(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            this.countDownInterval = countDownInterval;
            this.factor = (float) 100 / (millisInFuture/countDownInterval);
        }

        @Override
        public void onFinish() {
            image.setProgress(0);
            destroy();
        }

        @Override
        public void onTick(long millisUntilFinished) {
            image.setProgress(millisUntilFinished*factor/countDownInterval);
            counter_tv.setText("" + (int) Math.ceil(millisUntilFinished * 1.0 / 1000));
        }
    }

    @Override
    public void onBackPressed() {
        destroy();
    }
}
