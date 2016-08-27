package com.netease.nim.uikit.common.media.picker.joycamera.activity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.activity.TActionBarActivity;
import com.netease.nim.uikit.common.media.picker.joycamera.Constant;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.imageview.CropImageView;
import com.netease.nim.uikit.common.util.media.BitmapDecoder;
import com.netease.nim.uikit.common.util.media.ImageUtil;
import com.netease.nim.uikit.common.util.storage.StorageType;
import com.netease.nim.uikit.common.util.storage.StorageUtil;
import com.netease.nim.uikit.common.util.string.StringUtil;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.netease.nim.uikit.session.constant.Extras;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Kairong on 2016/8/12.
 * mail:wangkrhust@gmail.com
 */
public class CropImageActivity extends TActionBarActivity{
    private static final int SAVE_IMAGE_SUCCESS = 0;
    private static final int SAVE_IMAGE_ERROR = 1;

    private boolean showCropMenu = false;
    private boolean showRotateMenu = false;
    private volatile boolean isAnimating = false;

    private Bitmap mBitmap;

    private LinearLayout crop_child_menu_crop_ll;
    private LinearLayout crop_child_menu_rotate_ll;

    private String path;

    private CropImageView cropImageView;

    public static void start(Context context, String path, Class<?> callClass){
        Intent intent = new Intent();
        intent.setClass(context, CropImageActivity.class);
        intent.putExtra(Constant.IMAGE_PATH, path);
        intent.putExtra(Extras.EXTRA_CALL_CLASS, callClass);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.activity_crop_image);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initViews();

        Intent intent = getIntent();
        path = intent.getStringExtra(Constant.IMAGE_PATH);

        cropImageView.setOutput(ScreenUtil.screenWidth, ScreenUtil.screenWidth);
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mBitmap == null) {
                    //判断图片是不是旋转，是的话就进行纠正。
                    if (path != null) {
                        mBitmap = BitmapDecoder.decodeSampledForDisplay(path);
                        mBitmap = ImageUtil.rotateBitmapInNeeded(path, mBitmap);
                    }
                }

                if (mBitmap == null) {
                    finish();
                } else {
                    cropImageView.setImageBitmap(mBitmap);
                    cropImageView.invalidate();
                }
            }
        });
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case SAVE_IMAGE_SUCCESS:
                    String path = (String)msg.obj;
                    PicturePreviewActivity.start(CropImageActivity.this, path, (Class)getIntent().getSerializableExtra(Extras.EXTRA_CALL_CLASS));
                    break;
                case SAVE_IMAGE_ERROR:
                    Toast.makeText(CropImageActivity.this, "出错了", Toast.LENGTH_SHORT).show();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    /**
     * 初始化视图
     *
     * @return void
     * @Title: initViews
     * @date 2012-12-14 上午10:41:23
     */
    private void initViews() {
        crop_child_menu_crop_ll = (LinearLayout) findViewById(R.id.crop_crop_child_menu_ll);
        crop_child_menu_rotate_ll = (LinearLayout) findViewById(R.id.crop_rotate_child_menu_ll);
        cropImageView = (CropImageView)findViewById(R.id.crop_image_view);

        initShowChildMenuAnimation();
    }

    public void onViewClick(View v) {
        if (isAnimating) {
            return;
        }
        int id = v.getId();

        if (id == R.id.crop_bar_cancell) {
            // 取消
            finish();
        } else if (id == R.id.crop_bar_ok) {
            // 完成
            onSaveClicked();
        } else if (id == R.id.crop_bar_rotate_menu || id == R.id.crop_bar_crop_menu
                || id == R.id.crop_child_menu_crop_back || id == R.id.crop_child_menu_rotate_back) {
            showChildMenu(v.getId());
        } else if (id == R.id.crop_child_menu_rotate) {
            Matrix m = new Matrix();
            m.postRotate(90);
            mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), m, true);
            cropImageView.setImageBitmap(mBitmap);
            cropImageView.invalidate();
        } else if (id == R.id.crop_child_menu_fanzhuan) {
            Matrix m = new Matrix();
            m.postScale(-1, 1);
            mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), m, true);
            cropImageView.setImageBitmap(mBitmap);
            cropImageView.invalidate();
        } else if (id == R.id.crop_child_menu_4_3) {
            if (mBitmap != null) {
                int w = mBitmap.getWidth();
                cropImageView.updateSelection(w, (int)(w * 0.75));
                cropImageView.invalidate();
            }
        } else if (id == R.id.crop_child_menu_3_4) {
            if (mBitmap != null) {
                int w = mBitmap.getWidth();
                cropImageView.updateSelection(w, (int)(w / 0.75));
                cropImageView.invalidate();
            }
        } else if (id == R.id.crop_child_menu_1_1) {
            if (mBitmap != null) {
                int w = mBitmap.getWidth();
                cropImageView.updateSelection(w, w);
                cropImageView.invalidate();
            }
        }
    }


    TranslateAnimation showAni;
    TranslateAnimation hideAni;

    /**
     * 显示子菜单
     *
     * @param Id 子菜单资源id
     */
    private void showChildMenu(int Id) {
        if (Id == R.id.crop_bar_crop_menu) {
            showCropMenu = true;
            crop_child_menu_crop_ll.startAnimation(showAni);
            isAnimating = true;
        } else if (Id == R.id.crop_bar_rotate_menu) {
            showRotateMenu = true;
            crop_child_menu_rotate_ll.startAnimation(showAni);
            isAnimating = true;
        } else if (Id == R.id.crop_child_menu_crop_back) {
            crop_child_menu_crop_ll.startAnimation(hideAni);
            isAnimating = true;
        } else if (Id == R.id.crop_child_menu_rotate_back) {
            crop_child_menu_rotate_ll.startAnimation(hideAni);
            isAnimating = true;
        }
    }

    // 初始化子菜单弹出动画
    private void initShowChildMenuAnimation(){
        showAni = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF,0);
        hideAni = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF,0);
        showAni.setDuration(600);
        hideAni.setDuration(600);
        showAni.setInterpolator(new DecelerateInterpolator(2));
        hideAni.setInterpolator(new DecelerateInterpolator(2));
        showAni.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (showCropMenu) {
                    crop_child_menu_crop_ll.clearAnimation();
                    crop_child_menu_crop_ll.setVisibility(View.VISIBLE);
                }
                if (showRotateMenu) {
                    crop_child_menu_rotate_ll.clearAnimation();
                    crop_child_menu_rotate_ll.setVisibility(View.VISIBLE);
                }
                isAnimating = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        hideAni.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if(showCropMenu){
                    crop_child_menu_crop_ll.clearAnimation();
                    crop_child_menu_crop_ll.setVisibility(View.GONE);
                    showCropMenu = false;
                }
                if(showRotateMenu){
                    crop_child_menu_rotate_ll.clearAnimation();
                    crop_child_menu_rotate_ll.setVisibility(View.GONE);
                    showRotateMenu = false;
                }
                isAnimating = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }


    /**
     * 点击保存的处理，这里保存成功回传的是一个Uri，系统默认传回的是一个bitmap图，
     * 如果传回的bitmap图比较大的话就会引起系统出错。会报这样一个异常：
     * android.os.transactiontoolargeexception。为了规避这个异常，
     * 采取了传回Uri的方法。
     * @Title: onSaveClicked
     * @return void
     * @date 2012-12-14 上午10:32:38
     */
    private void onSaveClicked() {
        final String cropPath = StorageUtil.getWritePath(StringUtil.makeMd5(System.currentTimeMillis() + ""), StorageType.TYPE_TEMP);
        if(cropImageView.saveCroppedIamge(cropPath)){
            handler.obtainMessage(SAVE_IMAGE_SUCCESS, cropPath).sendToTarget();
        } else {
            handler.obtainMessage(SAVE_IMAGE_ERROR).sendToTarget();
        }
    }
    /**
     * 将Bitmap放入缓存，
     * @Title: saveDrawableToCache
     * @param bitmap
     * @param filePath
     * @return void
     * @date 2012-12-14 上午9:27:38
     */
    private void saveDrawableToCache(Bitmap bitmap, String filePath) throws IOException{
        File file = new File(filePath);

        if(file.createNewFile()) {
            OutputStream outStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.flush();
            outStream.close();
        }
    }

    /**
     *
     * @param src
     * @param watermark
     * @return
     */
    private Bitmap createBitmap(Bitmap src, Bitmap watermark){
        if(src == null) {
            return null;
        }
        int w = src.getWidth();
        int h = src.getHeight();
        int ww = watermark.getWidth();
        int wh = watermark.getHeight();

        // 缩放图片的尺寸
        float scaleWidth = (float) w / ww;
        float scaleHeight = (float) h / wh;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 产生缩放后的Bitmap对象
        Bitmap resizeBitmap = Bitmap.createBitmap(watermark, 0, 0, ww, wh, matrix, false);


        //create the new blank bitmap
        Bitmap newb = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        //创建一个新的和SRC长度宽度一样的位图
        Canvas cv = new Canvas(newb);
        //draw src into
        cv.drawBitmap(src, 0, 0, null);//在 0，0坐标开始画入src
        //draw watermark into
        cv.drawBitmap(resizeBitmap, 0, 0, null);//在src的右下角画入水印
        // saveAll all clip
        cv.save(Canvas.ALL_SAVE_FLAG);//保存
        //store
        cv.restore();//存储

        resizeBitmap.recycle();
        return newb;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBitmap.recycle();
        mBitmap = null;
    }
}
