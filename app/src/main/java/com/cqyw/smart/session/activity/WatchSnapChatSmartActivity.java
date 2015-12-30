package com.cqyw.smart.session.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.cqyw.smart.JActionBarActivity;
import com.cqyw.smart.R;
import com.cqyw.smart.session.extension.MySnapChatAttachment;
import com.cqyw.smart.widget.squareprogressbar.SquareProgressBar;
import com.netease.nim.uikit.common.ui.dialog.CustomAlertDialog;
import com.netease.nim.uikit.common.util.media.BitmapDecoder;
import com.netease.nim.uikit.common.util.media.ImageUtil;
import com.netease.nim.uikit.joycustom.snap.SnapConstant;
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;



/**
 * 查看阅后即焚消息原图
 */
public class WatchSnapChatSmartActivity extends JActionBarActivity {
    private static final String INTENT_EXTRA_IMAGE = "INTENT_EXTRA_MESSAGE";

    private IMMessage message;
    private MyCounter counter;
    private File smartfile;

    private View loadingLayout;
    private TextView counter_tv;
    private SquareProgressBar image;
    private ActionBar actionBar;
    protected CustomAlertDialog alertDialog;

    private static Bitmap srcBitmap;

    private static WatchSnapChatSmartActivity instance;

    private static final long COUNT_DOWN_TIME = 5000;

    private static final long COUNT_DOWN_INT = 200;

    public static boolean hasSeen = false;

    public static void start(Context context, IMMessage message) {
        Intent intent = new Intent();
        intent.putExtra(INTENT_EXTRA_IMAGE, message);
        intent.setClass(context, WatchSnapChatSmartActivity.class);
        context.startActivity(intent);
    }

    public static void destroy() {
        if (instance != null) {
            if (instance.smartfile != null){
                instance.smartfile.delete();
            }
            instance.finish();
            instance = null;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onParseIntent();
        setContentView(R.layout.activity_watch_snap_picture);
        findViews();

        counter = new MyCounter(COUNT_DOWN_TIME, COUNT_DOWN_INT);
        requestOriImage();

        instance = this;
        hasSeen = false;
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
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
    protected void initStyle() {
        super.initStyle();
        Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SECURE);
    }
    
    @Override
    protected void initData() {
    }
    
    @Override
    protected void initView() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        instance = null;
        smartfile = null;
    }

    private void onParseIntent() {
        this.message = (IMMessage) getIntent().getSerializableExtra(INTENT_EXTRA_IMAGE);
    }

    private void findViews() {
        alertDialog = new CustomAlertDialog(this);
        loadingLayout = findView(R.id.loading_layout);
        counter_tv = findView(R.id.counter_text);
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
        image.setColorRGB(getResources().getColor(R.color.theme_color));
        image.setWidth(2);
        image.setProgress(100);// 50*100ms=5s
    }

    private void requestOriImage() {
        if (isOriginImageHasDownloaded(message)) {
            downSmartFile(((MySnapChatAttachment)message.getAttachment()).getUrl());
            return;
        }

        // async download original image
//        SnapConstant.downloadIMMessageAttachment(message);
        onDownloadStart(message);
    }

    private boolean isOriginImageHasDownloaded(final IMMessage message) {
        if (message.getAttachStatus() == AttachStatusEnum.transferred) {
            return true;
        }

        return false;
    }

    public void recycle() {
        if (srcBitmap != null && !srcBitmap.isRecycled())
            srcBitmap.recycle();
        srcBitmap = null;
    }

    /**
     * ******************************** 设置图片 *********************************
     */

    private void setThumbnail() {
        String path = ((MySnapChatAttachment) message.getAttachment()).getThumbPath();
        if (!TextUtils.isEmpty(path)) {
            Bitmap bitmap = BitmapDecoder.decodeSampledForDisplay(path);
            bitmap = ImageUtil.rotateBitmapInNeeded(path, bitmap);
            if (bitmap != null) {
                image.setImageBitmap(bitmap);
                return;
            }
        }

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
            msg.setStatus(MsgStatusEnum.read);
            counter.start();
        }
    }

    private int getImageResOnLoading() {
        return R.drawable.nim_image_default;
    }

    private int getImageResOnFailed() {
        return R.drawable.nim_image_download_failed;
    }

    /**
     * ********************************* 下载 ****************************************
     */


    private void onDownloadStart(final IMMessage msg) {
        setThumbnail();
        if(TextUtils.isEmpty(((MySnapChatAttachment) msg.getAttachment()).getPath())){
            loadingLayout.setVisibility(View.VISIBLE);
        } else {
            loadingLayout.setVisibility(View.GONE);
        }

        downSmartFile(((MySnapChatAttachment) msg.getAttachment()).getUrl());
    }

    private void onDownloadSuccess(final IMMessage msg) {
        loadingLayout.setVisibility(View.GONE);

        handler.post(new Runnable() {

                @Override
                public void run() {
                    setImageView(msg);
                }
            });
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
                        while ( smartfile == null && i > 0) {
                            smartfile = SnapConstant.downloadSmartImage(urlName);
                            i--;
                        }
                        if (smartfile != null) {
                            try {
                                // 临时文件创建成功
                                if (smartfile.createNewFile()) {
                                    handler.sendEmptyMessage(0);
                                }
                            }catch (IOException e){
                                e.getMessage();
                                smartfile.delete();
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
            recycle();
            if (!isDestroyed())
            destroy();
        }

        @Override
        public void onTick(long millisUntilFinished) {
            image.setProgress(millisUntilFinished*factor/countDownInterval);
            counter_tv.setText("" + (int)Math.ceil(millisUntilFinished*1.0/1000));
        }
    }
}
