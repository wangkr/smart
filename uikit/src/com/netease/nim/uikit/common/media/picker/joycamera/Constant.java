package com.netease.nim.uikit.common.media.picker.joycamera;

import android.content.Context;
import android.content.Intent;
import android.media.ExifInterface;
import android.net.Uri;


import com.netease.nim.uikit.R;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Kairong on 2015/9/22.
 * mail:wangkrhust@gmail.com
 */
public class Constant {
    /**
     * 模糊半径radius 和 模糊擦除笔头大小pen
     */
    public static final int defRadiuSize = 24;
    public static final int maxRadiusSize = 100;
    public static final int defPenSize = 150;
    public static final int maxPenSize = 500;
    // 模糊图的缩放因子
    public static final float blurScaleFactor = 4;
    // 阶梯式调整模糊半径的阶段值
    public static final int radius_stage1 = 50;
    public static final int radius_stage2 = 70;
    public static final int radius_stage3 = 90;
    // 阶梯式调整模糊半径的比率
    public static final float[] radius_rate = {0.50f, 0.56f, 0.66f, 0.91f};
    // 是否显示【模糊背景菜单】
    public static final boolean blur_menu_shown = true;
    // 统计用户使用app的事件
    public static final String stat_camera_type_front = "Front";
    public static final String stat_camera_type_back = "Back";
    public static final String stat_pen_size = "Pen_Size";
    public static final String stat_blur_radius = "Blur_Radius";
    public static final String stat_blur_save_times = "Blur_Save";
    public static final String stat_fanzhuan = "Fanzhuan";
    public static final String stat_xuanzhuan = "Xuanzhuan";
    public static final String stat_save_picture = "Share";
    public static final String stat_share_picture = "Save";
    /**
     * 设备屏幕宽度高度,默认图片宽高，闪光灯和自动对焦功能探测设置
     * 在WelcomeActivity里面设置
     */
    public static float scale = 0;					//屏幕密度
    public static boolean hasFlashLight = false;
    public static boolean canAutoFocus = false;

    public final static float pictureRatio = 0.75f;

    public final static String childMsgKey = "savedPicturePath";
    // 默认摄像头选项
    public final static String defCamPosKey = "cameraPositionKey";
    // 默认闪光灯选项
    public final static String defFlashLightKey = "flashLightKey";
    // 是否第一次使用模糊背景功能
    public final static String isFirstUseBlurKey = "isFUBKey";
    // 默认模糊画笔大小
    public final static String penSizeKey = "penSizeKey";
    // 前置摄像头拍照参数
    public final static String frontPicParam = "frontPicParam";
    // 后置摄像头拍照参数
    public final static String backPicParam = "backPicParam";

    /**
     * 指定发布图片的宽度,单位px
     */
    public static int IMAGEWIDTH = 640;

    /**
     * 图片Uri
     */
    public static String IMAGE_URI = "image_uri";
    /**
     * 图片路径
     */
    public static String IMAGE_PATH = "image_path";
    /**
     * 封面水印下标
     */
    public static String COVER_INDEX = "cover_index";
    /**
     * 加封面水印的图片路径
     */
    public static String WM_IMAGE_URI = "watermaker_image_uri";
    /**
     * 封面水印文件资源id
     */
//    public final static int[] coverResIds = {R.drawable.nansheng,R.drawable.yizhou,R.drawable.science,R.drawable.nature
//    ,R.drawable.kantianxia,R.drawable.muscle,R.drawable.jingjixueren,R.drawable.fubusi,R.drawable.time,R.drawable.easy
//    ,R.drawable.ruili,R.drawable.playboy,R.drawable.erciyuan,R.drawable.yilin,R.drawable.nba
//    ,R.drawable.nanfangzhoumo,R.drawable.shangtoutiao};
    /**
     * 封面水印文件Icon id
     */
//    public final static int[] coverIcnIds = {R.mipmap.nanshengnvsheng,R.mipmap.yizhou,R.mipmap.science,R.mipmap.nature
//            ,R.mipmap.kantianxia,R.mipmap.muscle,R.mipmap.jingjixueren,R.mipmap.fubusi,R.mipmap.time,R.mipmap.easy
//            ,R.mipmap.ruili,R.mipmap.playboy,R.mipmap.erciyuan,R.mipmap.yilin,R.mipmap.nba
//            ,R.mipmap.nanfangzhoumo,R.mipmap.shangtoutiao};
    /**
     * AR模型资源id
     */
//    public final static int[] arResIds = {};
    /**
     * AR模型缩略图Icon id
     */
//    public final static int[] arIcnIds = {};

    /**
     * 刷新相册
     */
    public static void refreshGallery(Context mContext,File imageFile){
        Uri localUri = Uri.fromFile(imageFile);
        Intent localIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,localUri);
        mContext.sendBroadcast(localIntent);
    }
    /**
     * App缓存文件路径
     */
    public static String CACHEPATH = "";

    public static int getExifRotation(File imageFile) {
        if (imageFile == null) return -1;
        try {
            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            // We only recognize a subset of orientation tag values
            switch (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
                default:
                    return ExifInterface.ORIENTATION_UNDEFINED;
            }
        } catch (IOException e) {
            return -1;
        }
    }

    public static boolean copyExifRotation(File sourceFile, File destFile) {
        if (sourceFile == null || destFile == null) return false;
        try {
            ExifInterface exifSource = new ExifInterface(sourceFile.getAbsolutePath());
            ExifInterface exifDest = new ExifInterface(destFile.getAbsolutePath());
            exifDest.setAttribute(ExifInterface.TAG_ORIENTATION, exifSource.getAttribute(ExifInterface.TAG_ORIENTATION));
            exifDest.saveAttributes();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 将字符串转成16 位MD5值
     *
     * @param string
     * @return
     */
    public static String MD5(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(
                    string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
//        return hex.toString();// 32位
        return hex.toString().substring(8, 24);// 16位
    }

}
