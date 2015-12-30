package com.netease.nim.uikit.joycustom.upyun;

import android.graphics.Bitmap;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.util.file.FileUtil;
import com.netease.nim.uikit.common.util.string.MD5;
import com.netease.nim.uikit.common.util.string.StringUtil;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Kairong on 2015/10/31.
 * mail:wangkrhust@gmail.com
 */
public class JoyImageUtil {
    public static  String DATE_FORMAT = "yyyy-MM-dd";
    // 运行前先设置好以下三个参数
    public static  String BUCKET_NAME;
    public static  String OPERATOR_NAME;
    public static  String OPERATOR_PWD;
    public static  String SEPARATOR = "!";

    public static  String BUCKET_NAME_SMART   ;
    public static  String OPERATOR_NAME_SMART ;
    public static  String OPERATOR_PWD_SMART  ;

    /** 绑定的域名 */
    private static  String URL;
    private static  String JOY_HEAD_FOLDER;
    private static  String JOY_COVER_FOLDER;
    private static  String JOY_GALPHOTO_FOLDER;
    private static UpYun joyyunComm;
    private static UpYun joyyunSmart;

    private static boolean uploadIMMsgSmartState = false;
    private static int uploadTimes = 0;

    private static DisplayImageOptions coverNormalOpt;
    private static DisplayImageOptions coverSmallOpt;

    public static void init() {
        BUCKET_NAME = NimUIKit.getContext().getString(R.string.bucket_name);
        OPERATOR_NAME = NimUIKit.getContext().getString(R.string.operator_name);
        OPERATOR_PWD = NimUIKit.getContext().getString(R.string.operator_pwd);
        BUCKET_NAME_SMART = NimUIKit.getContext().getString(R.string.bucket_name_smart);
        OPERATOR_NAME_SMART = NimUIKit.getContext().getString(R.string.operator_name_smart);
        OPERATOR_PWD_SMART = NimUIKit.getContext().getString(R.string.operator_pwd_smart);
        URL = "http://" + BUCKET_NAME+ NimUIKit.getContext().getString(R.string.upyun_name);
        JOY_HEAD_FOLDER = NimUIKit.getContext().getString(R.string.joyyun_head_foler);
        JOY_COVER_FOLDER = NimUIKit.getContext().getString(R.string.joyyun_cover_folder);
        JOY_GALPHOTO_FOLDER = NimUIKit.getContext().getString(R.string.joyyun_galphoto_folder);

        // 初始化空间
        joyyunComm = new UpYun(BUCKET_NAME, OPERATOR_NAME, OPERATOR_PWD);
        joyyunSmart = new UpYun(BUCKET_NAME_SMART, OPERATOR_NAME_SMART, OPERATOR_PWD_SMART);
        // 初始化加载图片选项
        coverNormalOpt = createCoverImageOptions(ImageType.V_1080);
        coverSmallOpt = createCoverImageOptions(ImageType.V_120);
    }

    public static UpYun getJoyyunComm(){
        return joyyunComm;
    }

    public static UpYun getJoyyunSmart(){
        return joyyunSmart;
    }

    public enum ImageType{
        V_60(0),
        V_100(1),
        V_120(2),
        V_300ad(3),
        V_480(4),
        V_540(5),
        V_720(6),
        V_1080(7),
        V_ORG(8),
        V_300(9);
        private int value;
        ImageType(int value){
            this.value = value;
        }

        public boolean biggerThan(ImageType type) {
            return this.value > type.value;
        }
    }

    /**
     * 上传头像
     * @param file
     * @return
     */
    public static AbortableFuture<String> uploadHeadImage(final File file) {
        AbortableFuture<String> uploadTask = new AbortableFuture<String>() {
            @Override
            public boolean abort() {
                UpYun._stop_lock.stop();
                return false;
            }

            @Override
            public void setCallback(final RequestCallback requestCallback) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String fileName = new SimpleDateFormat(DATE_FORMAT).format(new Date(System.currentTimeMillis())) + UpYun.SEPARATOR + genJoyyunFilenameFromLocalPath(file.getPath());
                        String filePath = UpYun.SEPARATOR + JOY_HEAD_FOLDER + UpYun.SEPARATOR + fileName;
                        boolean result = false;
                        try {
                            result = getJoyyunComm().writeFile(filePath, file, true);
                        } catch (IOException e) {
                            e.printStackTrace();
                            requestCallback.onException(e.getCause());
                        }

                        if (result) {
                            requestCallback.onSuccess(fileName);
                        } else {
                            requestCallback.onFailed(-1);
                        }
                    }
                }).start();
            }
        };
        return uploadTask;
    }

    /**
     * 显示头像
     * @param v
     * @param fileName
     * @param imageType
     */
    public static void bindHeadImageView(ImageView v, String fileName, ImageType imageType){
        bindHeadImageView(v, fileName, imageType, null);
    }

    /**
     * 显示头像
     * @param v
     * @param fileName
     * @param imageType
     * @param options
     */
    public static void bindHeadImageView(final @NonNull ImageView v, @NonNull String fileName, @NonNull ImageType imageType, DisplayImageOptions options){
        if (TextUtils.isEmpty(fileName)) {
            return;
        }
        if (options == null) {
            ImageLoader.getInstance().displayImage(getHeadImageAbsUrl(fileName, imageType), v, DisplayImageOptions.createSimple(), new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    v.setImageBitmap(loadedImage);
                }
            });
        } else {
            ImageLoader.getInstance().displayImage(getHeadImageAbsUrl(fileName, imageType), v, options, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    v.setImageBitmap(loadedImage);
                }
            });
        }
    }
    private static final DisplayImageOptions createCoverImageOptions(ImageType type) {
        return new DisplayImageOptions.Builder()
                .showImageOnLoading(type.biggerThan(ImageType.V_120) ? R.drawable.joy_cover_loading:R.drawable.joy_cover_loading_small)
                .showImageOnFail(type.biggerThan(ImageType.V_120) ? R.drawable.joy_image_download_failed:R.drawable.joy_image_download_failed_small)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }

    /**
     * 显示封面图片
     * @param v
     * @param upFilePath
     */
    public static void bindCoverImageView(@NonNull final ImageView v, @NonNull String upFilePath, ImageType type){
        if (TextUtils.isEmpty(upFilePath)){
            int resId = type.biggerThan(ImageType.V_720) ? R.drawable.joy_cover_loading :
                    type.biggerThan(ImageType.V_120) ? R.drawable.joy_cover_loading_medium : R.drawable.joy_cover_loading_small;
            v.setImageResource(resId);
            return;
        }
        ImageLoader.getInstance().displayImage( getCoverAbsUrl(upFilePath, type), v, type.biggerThan(ImageType.V_120) ? coverNormalOpt : coverSmallOpt, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                v.setImageBitmap(loadedImage);
            }
        });
    }

    /**
     * 获取封面url
     * @param coverName 封面名字
     * @return
     */
    public static String getCoverAbsUrl(String coverName, ImageType type){
        if (coverName.charAt(0) == '/') {
            return URL + UpYun.SEPARATOR + JOY_COVER_FOLDER + coverName + genJoyyunUrlSuffix(type);
        }
        return URL + UpYun.SEPARATOR + JOY_COVER_FOLDER + UpYun.SEPARATOR + coverName + genJoyyunUrlSuffix(type);
    }

    /**
     * 获取头像在Upyun的完整url
     * @param upFilePath
     * @param imageType
     * @return
     */
    public static  String getHeadImageAbsUrl(String upFilePath, ImageType imageType){
        if (TextUtils.isEmpty(upFilePath)) {
            return null;
        }

        if (upFilePath.charAt(0) == '/') {
            return URL + UpYun.SEPARATOR + JOY_HEAD_FOLDER + upFilePath + genJoyyunUrlSuffix(imageType);
        }

        return URL + UpYun.SEPARATOR + JOY_HEAD_FOLDER + UpYun.SEPARATOR + upFilePath + genJoyyunUrlSuffix(imageType);
    }

    /**
     * 获取照片墙照片在UpYun的完整url
     * @param upFilePath
     * @param imageType
     * @return
     */
    public static String getGalPhotosAbsUrl (String upFilePath, ImageType imageType) {
        if (TextUtils.isEmpty(upFilePath)) {
            return null;
        }

        if (upFilePath.charAt(0) == '/') {
            return URL + UpYun.SEPARATOR + JOY_GALPHOTO_FOLDER + upFilePath + genJoyyunUrlSuffix(imageType);
        }

        return URL + UpYun.SEPARATOR + JOY_GALPHOTO_FOLDER + UpYun.SEPARATOR + upFilePath + genJoyyunUrlSuffix(imageType);
    }

    /**
     * 获取smart图在Upyun的相对路径
     * @param upFileName
     * @return
     */
    public static  String genSmartImageRltPath(String upFileName){
        if (TextUtils.isEmpty(upFileName)) {
            return null;
        }

        return UpYun.SEPARATOR +  new SimpleDateFormat(DATE_FORMAT).format(new Date(System.currentTimeMillis())) + UpYun.SEPARATOR + upFileName;
    }

    /**
     * 获取照片墙图片在UpYun上的相对路径
     * @param upFileName
     * @return
     */
    public static String genGalPhotoRltPath (String upFileName) {
        if (TextUtils.isEmpty(upFileName)) {
            return null;
        }

        return UpYun.SEPARATOR + JOY_GALPHOTO_FOLDER + UpYun.SEPARATOR + upFileName;
    }

    /**
     * 上传IMMessage中的smart附件
     * @param file 文件
     * @param message 消息
     * @param upyunUrlName UpYun文件名
     */
    public static void uploadIMMessageSmart(final File file, final IMMessage message, final String upyunUrlName) {
        final Handler handler = new Handler();
        message.setAttachStatus(AttachStatusEnum.transferring);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // 上传文件
                try {
                    uploadIMMsgSmartState = JoyImageUtil.getJoyyunSmart().writeFile(upyunUrlName, file, true);
                    uploadTimes ++;
                } catch (IOException e){
                    e.printStackTrace();
                    message.setAttachStatus(AttachStatusEnum.fail);
                    handler.removeCallbacks(this);
                    uploadIMMsgSmartState = false;
                    uploadTimes = 0;
                    file.delete();
                }
                // 实现失败后重发定时操作
                if (!uploadIMMsgSmartState && uploadTimes < 2) {
                    handler.postDelayed(this, 1000);
                } else {
                    message.setAttachStatus(uploadIMMsgSmartState? AttachStatusEnum.transferred:AttachStatusEnum.fail);
                    handler.removeCallbacks(this);
                    uploadIMMsgSmartState = false;
                    uploadTimes = 0;
                    file.delete();
                }

            }
        };
        handler.postDelayed(runnable, 100);
    }

    /**
     * 上传PublicSnapMessage中的smart附件
     * @param file 文件
     * @param upyunUrlName UpYun文件名
     */
    public static boolean uploadPublicSnapSmart(final File file, final String upyunUrlName) {
        boolean success = false;
        int times = 0;
        try {
            while ( !success && times < 2 ) {
                success = JoyImageUtil.getJoyyunSmart().writeFile(upyunUrlName, file, true);
                times++;
            }
            if (success && file != null) {
                if (file.delete()) return true;
            }
        } catch (IOException e){
           e.printStackTrace();
        }
        return success;
    }

    /**
     * 生成UpYun文件名
     * @param originfilePath 文件路径
     * @return
     */
    public static String genJoyyunFilenameFromLocalPath(String originfilePath){
        String fileName = FileUtil.getFileNameFromPath(originfilePath);
        return genJoyyunFilenameFromLocalName(fileName);
    }

    /**
     * 生成UpYun文件名
     * @param originFileName 文件名
     * @return
     */
    public static String genJoyyunFilenameFromLocalName(String originFileName){
        String nameMD5 = MD5.getStringMD5(originFileName+System.currentTimeMillis());
        String ext = FileUtil.getExtensionName(originFileName);
        return nameMD5 + StringUtil.getRandomString(8) + "."+ext;
    }

    /**
     * 根据图片类型生成url后缀
     * @param type
     * @return
     */
    private static String genJoyyunUrlSuffix(ImageType type){
        String suffix = "";
        switch (type){
            case V_60:
                suffix = "!60";
                break;
            case V_100:
                suffix = "!100";
                break;
            case V_120:
                suffix = "!120";
                break;
            case V_300:
                suffix = "!300";
                break;
            case V_300ad:
                suffix = "!300ad";
                break;
            case V_480:
                suffix = "!480";
                break;
            case V_540:
                suffix = "!540";
                break;
            case V_720:
                suffix = "!720";
                break;
            case V_1080:
                suffix = "!1080";
                break;
            default:
                break;
        }
        return suffix;
    }
}
