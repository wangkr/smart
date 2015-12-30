package com.cqyw.smart.config;

/**
 * Created by YUHONG on 2015/9/20.
 * Email: hongyuahu@gmail.com
 * 变量存储类
 * key 以kEY_开头
 * value 以TAG_开头
 */
public class AppConstants {
    /**
     * 设备屏幕分辨率
     */
    public static int displayWidth;
    public static int displayHeight;
    /**
     * 图片路径
     */
    public static String IMAGE_URI = "image_uri";
    /**
     * 子线程发送保存图片信号给主线程
     */
    public final static String childMsgKey = "savedPicturePath";
    /**
     * App缓存文件路径
     */
    public static String CACHEPATH = "";

    /**
     * 默认摄像头选项
     */
    public final static String defCamPosKey = "cameraPositionKey";
    /**
     * 默认闪光灯选项
     */
    public final static String defFlashLightKey = "flashLightKey";
    /**
     * 新版本下载路径
     */
    public static final String NewApkPathKey = "downloadNewApkPath";
    /**
     * 通知栏标题
     */
    public static final String NotifyTitleKey = "notifytitlekey";
    /**
     * 新版本下载地址
     */
    public static final String NewApkDownloadUrlKey = "newApkdownloadUrl";
    /**
     * 统计用户使用app的事件
     */
    public static final String stat_camera_type_front = "FRONT";
    public static final String stat_camera_type_back = "BACK";
    /**
     * 相机请求Key
     */
    public static final String CameraRequest = "camera_request";
    /**
     * 云信Servers post Key
     */
    public static final String appKeyKey = "AppKey";
    public static final String nonceKey = "Nonce";
    public static final String curTimeKey = "CurTime";
    public static final String checkSumKey = "CheckSum";

    // 发送验证码
    public static final String SendCodeURL = "https://api.netease.im/sms/sendcode.action";
    // 检验验证码
    public static final String VerifyCodeURL = "https://api.netease.im/sms/verifycode.action";

}
