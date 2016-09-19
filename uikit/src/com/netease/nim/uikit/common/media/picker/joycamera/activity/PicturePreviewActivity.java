package com.netease.nim.uikit.common.media.picker.joycamera.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.activity.TActionBarActivity;
import com.netease.nim.uikit.common.media.picker.joycamera.Constant;
import com.netease.nim.uikit.common.media.picker.joycamera.model.PublishMessage;
import com.netease.nim.uikit.common.media.picker.util.BitmapUtil;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.storage.StorageUtil;
import com.netease.nim.uikit.joycustom.snap.SelectSnapCoverImageActivity;
import com.netease.nim.uikit.joycustom.snap.SnapConstant;
import com.netease.nim.uikit.joycustom.upyun.JoyImageUtil;
import com.netease.nim.uikit.session.constant.Extras;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.download.ImageDownloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Kairong on 2016/8/4.
 * mail:wangkrhust@gmail.com
 */
public class PicturePreviewActivity extends TActionBarActivity {
    public static final int REQ_SELECT_COVER = 1;
    private String saveDir;

    private EditText preview_send_et;
    private ImageView preview_image_iv;
    private ImageView cover_image_iv;
    private CheckBox message_type_cb;
    private int type = 0; // 0-非封面 1-封面
    private boolean isSaved = false;
    private String imagePath;
    private int coverIndex = 0;
    private int pagerIndex = 0;
    private boolean isSnapMessage = false;


    public static void start(Context context, String path, Class<?> callClass, int type){
        Intent intent = new Intent();
        intent.putExtra(Constant.IMAGE_PATH, path);
        intent.setClass(context, PicturePreviewActivity.class);
        intent.putExtra(Extras.EXTRA_CALL_CLASS, callClass);
        intent.putExtra(Extras.EXTRA_TYPE, type);
        context.startActivity(intent);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_preview);
        setTitle("发送到广场");

        preview_send_et = (EditText)findViewById(R.id.preview_text_et);
        preview_image_iv = (ImageView)findViewById(R.id.preview_image_iv);
        cover_image_iv = (ImageView)findViewById(R.id.cover_image_iv);
        message_type_cb = (CheckBox)findViewById(R.id.message_type);

        cover_image_iv.setVisibility(View.GONE);

        ainim.setDuration(800);
        ainim.setInterpolator(new DecelerateInterpolator(2));

        saveDir = StorageUtil.getCommSystemImagePath() + "/joypictures/";
        File file = new File(saveDir);
        if (!file.exists()) {
            file.mkdir();
        } else if(!file.isDirectory()) {
            file.delete();
            file.mkdir();
        }

        message_type_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isSnapMessage = isChecked;
                if (isChecked) {
                    cover_image_iv.setVisibility(View.VISIBLE);
                    cover_image_iv.startAnimation(ainim);
                } else {
                    cover_image_iv.setVisibility(View.GONE);
                }
            }
        });

        parseIntent();
        isSaved = false;
    }

    private TranslateAnimation ainim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, -1, Animation.RELATIVE_TO_SELF,0);

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SelectSnapCoverImageActivity.start(PicturePreviewActivity.this, REQ_SELECT_COVER);
        }
    };

    private void parseIntent(){
        Intent intent = getIntent();
        imagePath = intent.getStringExtra(Constant.IMAGE_PATH);
        type = intent.getIntExtra(Extras.EXTRA_TYPE, 0);
        LogUtil.d("PPPP", imagePath);
        ImageLoader.getInstance().displayImage(ImageDownloader.Scheme.FILE.wrap(imagePath), preview_image_iv);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.picture_preview_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.picture_preview_send) {
            Intent intent = new Intent();
            PublishMessage publicMessage = new PublishMessage();
            publicMessage.setLocalPath(imagePath);
            publicMessage.setContent(preview_send_et.getText().toString());
            if (isSnapMessage) {
                publicMessage.setType(PublishMessage.MessageType.SNAP);
                publicMessage.setCover(SnapConstant.getServerDefaultCoverName(pagerIndex, coverIndex));
                String upyunUrlName = JoyImageUtil.genSmartImageRltPath(JoyImageUtil.genJoyyunFilenameFromLocalPath(imagePath));
                publicMessage.setSmart(upyunUrlName);
                intent.putExtra(Extras.EXTRA_SEND_SNAP, publicMessage);
            } else {
                publicMessage.setType(PublishMessage.MessageType.PUB);
                publicMessage.setSmart("");
                intent.putExtra(Extras.EXTRA_SEND_PUBLIC, publicMessage);
            }
            intent.setClass(this, (Class)getIntent().getSerializableExtra(Extras.EXTRA_CALL_CLASS));
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }
        else if(item.getItemId() == R.id.picture_preview_save) {
            if(isSaved){
                Toast.makeText(this,"保存成功",Toast.LENGTH_SHORT).show();
                return super.onOptionsItemSelected(item);
            }
            final String filename = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.CHINA).format(new Date())+".jpg";
            Bitmap wmBitmap = BitmapFactory.decodeFile(imagePath);
            try {

                String filePath = saveDir + filename;
                Toast.makeText(this,"保存成功:"+ savaBitmap(filePath, wmBitmap),Toast.LENGTH_LONG).show();
                wmBitmap.recycle();
                System.gc();
            } catch (IOException e) {
                e.printStackTrace();
            }
            isSaved = true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 保存Image的方法，有sd卡存储到sd卡，没有就存储到手机目录
     * @param fileName
     * @param bitmap
     * @throws IOException
     */
    public String savaBitmap(String fileName, Bitmap bitmap) throws IOException{
        if(bitmap == null){
            return "";
        }
        File file = new File(fileName);

        if (type == 1){
            if(file.createNewFile()) {
                FileOutputStream fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
            }
        }
        else if(type == 0) {
            Bitmap waterMark = BitmapFactory.decodeResource(getResources(), R.drawable.joy_watermark);

            Bitmap canvasBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(canvasBitmap);
            canvas.drawBitmap(bitmap, 0, 0, null);

            int minWatermarkSize = Math.min((int) (bitmap.getWidth() * 0.1), (int) (bitmap.getHeight() * 0.1));
            if (waterMark.getWidth() > minWatermarkSize) {
                Matrix m = new Matrix();
                m.postScale(minWatermarkSize * 1.0f / waterMark.getWidth(), minWatermarkSize * 1.0f / waterMark.getHeight());
                waterMark = Bitmap.createBitmap(waterMark, 0, 0, waterMark.getWidth(), waterMark.getHeight(), m, true);
            }

            // 画水印
            int wmLeft = (int) (bitmap.getWidth() * 0.05);
            int wmTop = bitmap.getHeight() - (int) (bitmap.getHeight() * 0.05) - waterMark.getHeight();
            canvas.drawBitmap(waterMark, wmLeft, wmTop, null);

            if (file.createNewFile()) {
                FileOutputStream fos = new FileOutputStream(file);
                canvasBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
            }

            if (!waterMark.isRecycled())
                waterMark.recycle();
            if (!canvasBitmap.isRecycled())
                canvasBitmap.recycle();
        }

        Constant.refreshGallery(this,file);
        return file.getPath();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

//        switch (requestCode) {
//            case REQ_SELECT_COVER:
//                int ci = data.getIntExtra(Extras.EXTRA_COVER_INDEX, -1);
//                int pi = data.getIntExtra(Extras.EXTRA_PAGER_INDEX, -1);
//                if (ci < 0 || pi < 0 || (coverIndex == ci && pagerIndex == pi)) {
//                    return;
//                }
//                coverIndex = ci;
//                pagerIndex = pi;
//
//                cover_image_iv.setImageResource(SnapConstant.getSnapCoverResId(coverIndex, pagerIndex));
//                break;
//        }
    }
}
