package com.cqyw.smart.util;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Build;
import android.os.Build.VERSION;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.cqyw.smart.config.AppContext;
import com.cqyw.smart.common.BaseApplication;

/**
 * 常用的系统工具类
 * @author HongYu
 * @author Kairong
 *
 * 2015年7月30日
 */
@SuppressLint("SimpleDateFormat")
public class SystemTools {

    public SystemTools() {
    }

    public static String getDataTime(String format) {
        SimpleDateFormat df = new SimpleDateFormat(format);
        return df.format(new Date());
    }

    public static String getDataTime() {
        return getDataTime("HH:mm");
    }

    public static String getPhoneIMEI(Context cxt) {
        TelephonyManager tm = (TelephonyManager)cxt.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }

    public static int getSDKVersion() {
        return VERSION.SDK_INT;
    }

    public static String getSystemVersion() {
        return VERSION.RELEASE;
    }

    public static boolean checkNet(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null;
    }

    public static boolean isWiFi(Context cxt) {
        ConnectivityManager cm = (ConnectivityManager)cxt.getSystemService(Context.CONNECTIVITY_SERVICE);
        State state = cm.getNetworkInfo(1).getState();
        return State.CONNECTED == state;
    }

    public static String getAppVersionName(Context context) {
        String version = "0";
        try {
            version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            return version;
        } catch (NameNotFoundException var3) {
            throw new RuntimeException(SystemTools.class.getName() + "the application not found");
        }
    }

    public static int getAppVersionCode(Context context) {
        try {
            int version1 = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
            return version1;
        } catch (NameNotFoundException var3) {
            throw new RuntimeException(SystemTools.class.getName() + "the application not found");
        }
    }

    /**
     * 隐藏键盘输入
     * @param view
     */
    public static void hideSoftKeyboard(View view) {
        if (view == null)
            return;
        ((InputMethodManager) BaseApplication.getContext().getSystemService(
                Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                view.getWindowToken(), 0);
    }

    /**
     *获取手机型号
     */
    public static String getDisplayVersion(){
        return Build.MODEL;
    }
    
    /**
     * 获取屏幕的分辨率
     * @return
     */
    public static DisplayMetrics getDisplayMetrics() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((WindowManager) AppContext.getContext().getSystemService(
                Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(
                displaymetrics);
        return displaymetrics;
    }
    /**
     * 是否有闪光灯
     */
    public static boolean supportedFlashLight(){
        return AppContext.getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    /**
     * 是否支持自动聚焦
     */
    public static boolean supportedAutoFocus(){
        return AppContext.getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS);
    }

    /**
     * 获取设备ID
     */
    public static String getDeviceID(){
        return Settings.Secure.getString(AppContext.getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

}
