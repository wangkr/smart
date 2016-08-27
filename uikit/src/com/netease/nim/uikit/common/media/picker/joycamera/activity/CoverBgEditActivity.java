package com.netease.nim.uikit.common.media.picker.joycamera.activity;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.activity.TActionBarActivity;
import com.netease.nim.uikit.common.media.picker.joycamera.CameraSharedPreference;
import com.netease.nim.uikit.common.media.picker.joycamera.Constant;
import com.netease.nim.uikit.common.media.picker.joycamera.cover.TouchMoveImageView;
import com.netease.nim.uikit.common.media.picker.joycamera.model.CamOnLineRes;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.util.storage.StorageType;
import com.netease.nim.uikit.common.util.storage.StorageUtil;
import com.netease.nim.uikit.common.util.string.StringUtil;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.netease.nim.uikit.session.constant.Extras;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.download.ImageDownloader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @ClassName: CoverBgEditActivity
 * @Description: The activity can crop specific region of interest from an image.图片裁剪界面
 * 					裁剪方式是移动缩放图片
 * @author shark
 * @date 2014年11月26日 上午10:53:25
 * @mail wangkrhust@gmail.com
 * @modified Kairong Wang
 *
 */
public class CoverBgEditActivity extends TActionBarActivity implements SeekBar.OnSeekBarChangeListener {

    private String TAG = "CoverBgEditActivity";

    private static final int SAVE_IMAGE_SUCCESS = 0;
    private static final int SAVE_IMAGE_ERROR = 1;

    public boolean mSaving = false;
    public boolean isRadiusChanged = false;
    private boolean showBlurMenu = false;
    private boolean showRotateMenu = false;
    private volatile boolean isAnimating = false;

    private int rotateDeg = 0;
    private int coverIdx;

    private Bitmap mBitmap;
    private TouchMoveImageView mImageView;
    private SeekBar blurRadiusBar;
    private SeekBar blurPenBar;

    private LinearLayout crop_child_menu_blur_ll;
    private LinearLayout crop_child_menu_rotate_ll;
    private LinearLayout pop_dialog_blur_radius_ll;
    private RelativeLayout pop_dialog_blurPenBar_rl;

    Uri targetUri;

    private ContentResolver mContentResolver;

    private int width;
    private int height;
    private int sampleSize = 1;

    private RelativeLayout mCrViewLayout;

    public static void start(Context context, Uri targetUri, int coverIdx, Class<?> callClass){
        Intent intent = new Intent();
        intent.setClass(context, CoverBgEditActivity.class);
        intent.putExtra(Constant.IMAGE_URI, targetUri);
        intent.putExtra(Constant.COVER_INDEX, coverIdx);
        intent.putExtra(Extras.EXTRA_CALL_CLASS, callClass);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.activity_coverbg_edit);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initViews();

        Intent intent = getIntent();
        targetUri = intent.getParcelableExtra(Constant.IMAGE_URI);
        coverIdx = getIntent().getIntExtra(Constant.COVER_INDEX, 0);

        mContentResolver = getContentResolver();

        loadBitmapAndDisplay();
        // 初始化seekbar
        blurRadiusBar.setMax(Constant.maxRadiusSize);
        blurPenBar.setMax(Constant.maxPenSize);
        blurRadiusBar.setProgress(Constant.defRadiuSize);
        blurPenBar.setProgress(CameraSharedPreference.getPenSizePref());

    }

    private void loadBitmapAndDisplay(){
        if (mBitmap == null) {
            String path = getFilePath(targetUri);
            //判断图片是不是旋转，是的话就进行纠正。
            if (path != null) {
                rotateDeg = Constant.getExifRotation(new File(path));
            }
            getBitmapSize();
            getBitmap();
        }

        if (mBitmap == null) {
            finish();
            return;
        }
        // 设置图片显示宽为屏幕宽度,高为宽的4/3
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ScreenUtil.screenWidth, (int) (ScreenUtil.screenWidth / Constant.pictureRatio));
        ImageView mWaterMark = new ImageView(this);
        mImageView = new TouchMoveImageView(this, mBitmap);
        mCrViewLayout.addView(mImageView, params);
        mCrViewLayout.addView(mWaterMark, params);
        // 设置默认封面
        ImageLoader.getInstance().displayImage(ImageDownloader.Scheme.FILE.wrap(NimUIKit.getCamOnLineResMgr().getItem(CamOnLineRes.Type.COVER, coverIdx).getCachePath()), mWaterMark);
        // 设置触摸监听
        mImageView.setOnTouchListener(touchViewListener);
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case SAVE_IMAGE_SUCCESS:
                    DialogMaker.dismissProgressDialog();
                    String path = (String)msg.obj;
                    PicturePreviewActivity.start(CoverBgEditActivity.this, path, (Class)getIntent().getSerializableExtra(Extras.EXTRA_CALL_CLASS));
                    break;
                case SAVE_IMAGE_ERROR:
                    DialogMaker.dismissProgressDialog();
                    Toast.makeText(CoverBgEditActivity.this, "出错了", Toast.LENGTH_SHORT).show();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    // 触摸mImageView弹出菜单消失
    View.OnTouchListener touchViewListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            Rect viewRect = new Rect();
            v.getDrawingRect(viewRect);
            int crop_bar_height = (int) (getResources().getDimension(R.dimen.crop_bar_height));
            viewRect.top += crop_bar_height;
            viewRect.bottom -= crop_bar_height;
            float m_X = event.getX(0);
            float m_Y = event.getY(0);
            if (viewRect.contains((int) m_X, (int) m_Y)) {
                pop_dialog_blur_radius_ll.setVisibility(View.GONE);
                pop_dialog_blurPenBar_rl.setVisibility(View.GONE);
            }
            return false;
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

        pop_dialog_blur_radius_ll = (LinearLayout) findViewById(R.id.crop_pop_dialg_blurradius);
        pop_dialog_blurPenBar_rl = (RelativeLayout) findViewById(R.id.crop_pop_dialg_pensize);
        crop_child_menu_blur_ll = (LinearLayout) findViewById(R.id.crop_blur_child_menu_ll);
        crop_child_menu_rotate_ll = (LinearLayout) findViewById(R.id.crop_rotate_child_menu_ll);

        blurRadiusBar = (SeekBar) findViewById(R.id.seekBar_blur_radius);
        blurPenBar = (SeekBar) findViewById(R.id.seekBar_pen_size);

        blurRadiusBar.setOnSeekBarChangeListener(this);
        blurPenBar.setOnSeekBarChangeListener(this);

        pop_dialog_blur_radius_ll.setVisibility(View.GONE);
        pop_dialog_blurPenBar_rl.setVisibility(View.GONE);

        int barrier_height = Math.round(ScreenUtil.screenHeight - ScreenUtil.screenWidth / Constant.pictureRatio);
        mCrViewLayout = (RelativeLayout) findViewById(R.id.crop_CropImage_view);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mCrViewLayout.getLayoutParams());
        int margin = barrier_height / 2;
        params.setMargins(0, margin, 0, margin);
        mCrViewLayout.setLayoutParams(params);

        initShowChildMenuAnimation();

        if (!Constant.blur_menu_shown) {
            findViewById(R.id.crop_menu_ll).setVisibility(View.GONE);
            findViewById(R.id.crop_rotate_child_menu_ll).setVisibility(View.VISIBLE);
            findViewById(R.id.crop_child_menu_rotate_back).setVisibility(View.GONE);
        }

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
        } else if (id == R.id.crop_bar_rotate_menu || id == R.id.crop_bar_blur_menu
                || id == R.id.crop_child_menu_blur_back || id == R.id.crop_child_menu_rotate_back) {
            showChildMenu(v.getId());
        } else if (id == R.id.crop_child_menu_pen_size) {
            int blurPenBar_visi = pop_dialog_blurPenBar_rl.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;
            pop_dialog_blurPenBar_rl.setVisibility(blurPenBar_visi);
            pop_dialog_blur_radius_ll.setVisibility(View.GONE);
        } else if (id == R.id.crop_child_menu_blur_size) {
            int blur_radius_visi = pop_dialog_blur_radius_ll.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;
            pop_dialog_blurPenBar_rl.setVisibility(View.GONE);
            pop_dialog_blur_radius_ll.setVisibility(blur_radius_visi);
        } else if (id == R.id.crop_child_menu_rotate) {
            mImageView.rotate(90);
        } else if (id == R.id.crop_child_menu_fanzhuan) {
            mImageView.flip_Horizon();
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
        if (Id == R.id.crop_bar_blur_menu) {
            showBlurMenu = true;
            crop_child_menu_blur_ll.startAnimation(showAni);
            isAnimating = true;
            mImageView.setBluring(true);
            if (CameraSharedPreference.isFirstUseBlur()) {
                AlertDialog tip = new AlertDialog.Builder(CoverBgEditActivity.this)
                        .setTitle("提示")
                        .setMessage("请用手指擦除不需要模糊的地方")
                        .setPositiveButton("知道了", null)
                        .setCancelable(false)
                        .create();
                tip.show();
                CameraSharedPreference.setFirstUseBlur();
            }

        } else if (Id == R.id.crop_child_menu_blur_back) {
            pop_dialog_blurPenBar_rl.setVisibility(View.GONE);
            pop_dialog_blur_radius_ll.setVisibility(View.GONE);
            // 如果进行了编辑就提示是否返回
            if (mImageView.isEdited) {
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setTitle("确定返回?")
                        .setMessage("模糊效果将不被保存,确定返回?")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                crop_child_menu_blur_ll.startAnimation(hideAni);
                                isAnimating = true;
                                /// 取消模糊化效果
                                mImageView.setBluring(false);
                            }
                        })
                        .setNegativeButton("取消", null)
                        .create();
                alertDialog.show();
            } else {
                crop_child_menu_blur_ll.startAnimation(hideAni);
                isAnimating = true;
                /// 取消模糊化效果
                mImageView.setBluring(false);
            }
        } else if (Id == R.id.crop_bar_rotate_menu) {
            showRotateMenu = true;
            crop_child_menu_rotate_ll.startAnimation(showAni);
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
        showAni.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (showBlurMenu) {
                    crop_child_menu_blur_ll.clearAnimation();
                    crop_child_menu_blur_ll.setVisibility(View.VISIBLE);
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
                if(showBlurMenu){
                    crop_child_menu_blur_ll.clearAnimation();
                    crop_child_menu_blur_ll.setVisibility(View.GONE);
                    showBlurMenu = false;
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


    // seekbar监听
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (seekBar.getId() == R.id.seekBar_blur_radius){
            // 模糊半径改变监听
            if(isRadiusChanged){
                isRadiusChanged = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mImageView.refreshCanvas();
                    }
                });
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar.getId() == R.id.seekBar_blur_radius) {
            // 模糊半径改变监听,阶梯式设置
            int newprocess;
            if (progress <= Constant.radius_stage1) {
                newprocess = Math.round(progress * Constant.radius_rate[0]);
            } else if (progress <= Constant.radius_stage2) {
                newprocess = Math.round(progress * Constant.radius_rate[1]);
            } else if (progress <= Constant.radius_stage3) {
                newprocess = Math.round(progress * Constant.radius_rate[2]);
            } else {
                newprocess = Math.round(progress * Constant.radius_rate[3]);
            }
            mImageView.setRadius(newprocess == 0 ? 1 : newprocess);
            isRadiusChanged = true;
        }else if (seekBar.getId() == R.id.seekBar_pen_size) {
            // 模糊画笔大小改变监听
            mImageView.setPenSize(progress);
            float scale = Math.max((float) progress * 1.2f, 4f) / 100;
            ((ImageView) findViewById(R.id.pen_size_view)).setScaleY(scale);
            ((ImageView) findViewById(R.id.pen_size_view)).setScaleX(scale);
        }
    }


    /**
     * 获取Bitmap分辨率，太大了就进行压缩
     * @Title: getBitmapSize
     * @return void
     * @date 2012-12-14 上午8:32:13
     */
    private void getBitmapSize(){
        InputStream is = null;
        try {

            is = getInputStream(targetUri);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options);

            switch (rotateDeg){
                case 180:case 0:
                    width = options.outWidth;
                    height = options.outHeight;
                    break;
                case 90:case 270:
                    width = options.outHeight;
                    height = options.outWidth;
                    break;
                default:
                    width = options.outWidth;
                    height = options.outHeight;
            }
        }catch(IOException e) {
            e.printStackTrace();
        }finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * 此处写方法描述
     * @Title: getBitmap
     * @return void
     * @date 2012-12-13 下午8:22:23
     */
    private void getBitmap(){
        InputStream is = null;
        try {

            try {
                is = getInputStream(targetUri);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            //shark 如果图片太大的话，压缩
            while ((width / sampleSize > ScreenUtil.screenWidth*2) || (height / sampleSize > ScreenUtil.screenHeight*2)) {
                sampleSize *= 2;
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = sampleSize;

            mBitmap = BitmapFactory.decodeStream(is, null, options);
            // 进行旋转
            if(rotateDeg>0){
                Matrix m = new Matrix();
                m.setRotate(rotateDeg);
                mBitmap = Bitmap.createBitmap(mBitmap,0,0,mBitmap.getWidth(),mBitmap.getHeight(),m,false);
            }

            // 缩放图片的尺寸
            int ww = mBitmap.getWidth();
            int wh = mBitmap.getHeight();
            float scale = 0;
            if(ww*1f/wh>Constant.pictureRatio){
                scale = (float) Math.round(ScreenUtil.screenWidth / Constant.pictureRatio) / wh;
            }else{
                scale = (float) ScreenUtil.screenWidth / ww;
            }

            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            // 产生缩放后的Bitmap对象
            mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, ww, wh, matrix, false);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * 此处写方法描述
     * @Title: rotateImage
     * @param path
     * @return void
     * @date 2012-12-14 上午10:58:26
     */
    private boolean isRotateImage(String path){

        try {
            ExifInterface exifInterface = new ExifInterface(path);

            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            if(orientation == ExifInterface.ORIENTATION_ROTATE_90 ){
                return true;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取输入流
     * @Title: getInputStream
     * @param mUri
     * @return
     * @return InputStream
     * @date 2012-12-14 上午9:00:31
     */
    private InputStream getInputStream(Uri mUri) throws IOException{
        try {
            if (mUri.getScheme().equals("file")) {
                return new java.io.FileInputStream(mUri.getPath());
            } else {
                return mContentResolver.openInputStream(mUri);
            }
        } catch (FileNotFoundException ex) {
            return null;
        }
    }
    /**
     * 根据Uri返回文件路径
     * @Title: getInputString
     * @param mUri
     * @return
     * @return String
     * @date 2012-12-14 上午9:14:19
     */
    private String getFilePath(Uri mUri){
        try {
            if (mUri.getScheme().equals("file")) {
                return mUri.getPath();
            } else {
                return getFilePathByUri(mUri);
            }
        } catch (FileNotFoundException ex) {
            return null;
        }
    }
    /**
     * 此处写方法描述
     * @Title: getFilePathByUri
     * @param mUri
     * @return
     * @return String
     * @date 2012-12-14 上午9:16:33
     */
    private String getFilePathByUri(Uri mUri) throws FileNotFoundException{
        String imgPath ;
        Cursor cursor = mContentResolver.query(mUri, null, null, null, null);
        cursor.moveToFirst();
        imgPath = cursor.getString(1); // 图片文件路径
        return imgPath;
    }
    /**
     * 旋转图片
     * @Title: rotateBitmap
     * @param isRotate 是否旋转图片
     * @return void
     * @date 2012-12-14 上午10:38:29
     */
    private void rotateBitmap(final boolean isRotate) {
        if (isFinishing()) {
            return;
        }
        if(isRotate){
            bitmapRotate(90);
        }

        mImageView.setImageBitmap(mBitmap);

    }

    /**
     * 水平翻转原图
     */
    private void bitmapHFlip(){
        Matrix matrix = new Matrix();
        matrix.postScale(-1, 1); // 镜像水平翻转
        int width = mBitmap.getWidth();
        int height = mBitmap.getHeight();

        try{
            mBitmap = Bitmap.createBitmap(mBitmap,0, 0, width, height, matrix, true);
        }catch(OutOfMemoryError ooe){

            matrix.postScale((float)1/sampleSize,(float) 1/sampleSize);
            mBitmap = Bitmap.createBitmap(mBitmap,0, 0, width, height, matrix, true);

        }
    }

    /**
     * 旋转原图
     * @Title: bitmapRotate
     * @return void
     * @date 2012-12-13 下午5:37:15
     */
    private void bitmapRotate(int degree){
        Matrix m = new Matrix();
        m.setRotate(degree);
        int width = mBitmap.getWidth();
        int height = mBitmap.getHeight();

        try{
            mBitmap = Bitmap.createBitmap(mBitmap,0, 0, width, height, m, true);
        }catch(OutOfMemoryError ooe){

            m.postScale((float)1/sampleSize,(float) 1/sampleSize);
            mBitmap = Bitmap.createBitmap(mBitmap,0, 0, width, height, m, true);

        }

    }

    /**
     * 旋转图片，每次以90度为单位
     * @Title: onRotateClicked
     * @return void
     * @date 2012-12-12 下午5:19:21
     */
    private void onRotateClicked(){

        rotateBitmap(true);

    }

    /**
     * 水平翻转图片
     */
    private void onFlipClicked(){
        bitmapHFlip();
        mImageView.setImageBitmap(mBitmap);
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
        final Bitmap croppedImage = mImageView.createNewPhoto();
        DialogMaker.showProgressDialog(CoverBgEditActivity.this, "正在保存...").setCancelable(false);
        new Thread(){
            @Override
            public void run(){
                try {
                    // 水印Bitmap
                    final Bitmap waterMark = ImageLoader.getInstance().
                            loadImageSync(ImageDownloader.Scheme.FILE.wrap(NimUIKit.getCamOnLineResMgr().getItem(CamOnLineRes.Type.COVER,coverIdx).getCachePath()));

                    final String cropPath = StorageUtil.getWritePath(StringUtil.makeMd5(System.currentTimeMillis() + ""), StorageType.TYPE_TEMP);
                    // TODO Auto-generated method stub
                    Bitmap finalBitmap = createBitmap(croppedImage, waterMark);
                    saveDrawableToCache(finalBitmap, cropPath);
                    handler.obtainMessage(SAVE_IMAGE_SUCCESS, cropPath).sendToTarget();
                    croppedImage.recycle();
                    waterMark.recycle();
                } catch (OutOfMemoryError | IOException error){
                    if (croppedImage != null && !croppedImage.isRecycled()){
                        croppedImage.recycle();
                    }
                    handler.obtainMessage(SAVE_IMAGE_ERROR).sendToTarget();
                }
            }
        }.start();
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
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mImageView.recyle();
        mBitmap.recycle();
        mBitmap = null;
    }

    @Override
    public void finish() {
        CameraSharedPreference.setPenSizePref(mImageView.getPenSize());
        super.finish();
    }
}
