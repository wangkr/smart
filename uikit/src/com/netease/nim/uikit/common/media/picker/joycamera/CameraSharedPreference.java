package com.netease.nim.uikit.common.media.picker.joycamera;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.preference.PreferenceManager;

/**
 * Created by Kairong on 2016/7/29.
 * mail:wangkrhust@gmail.com
 */
public class CameraSharedPreference {
    private static Context mContext;

    public static void init(Context context){
        mContext = context;
    }
    public static void setCameraPos(int cameraPos){
        SharedPreferences lastCamPos = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = lastCamPos.edit();
        editor.putInt(Constant.defCamPosKey,cameraPos);
        editor.apply();
    }
    public static int getCameraPos(){
        SharedPreferences camPos = PreferenceManager.getDefaultSharedPreferences(mContext);
        return camPos.getInt(Constant.defCamPosKey, Camera.CameraInfo.CAMERA_FACING_FRONT);
    }

    public static String getFrontPicParam(){
        SharedPreferences fpp = PreferenceManager.getDefaultSharedPreferences(mContext);
        return fpp.getString(Constant.frontPicParam, "");
    }

    public static void setFrontPicParam(String param){
        SharedPreferences fpp = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = fpp.edit();
        editor.putString(Constant.frontPicParam,param);
        editor.apply();
    }

    public static String getBackPicParam(){
        SharedPreferences fpp = PreferenceManager.getDefaultSharedPreferences(mContext);
        return fpp.getString(Constant.backPicParam, "");
    }


    public static void setBackPicParam(String param){
        SharedPreferences bpp = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = bpp.edit();
        editor.putString(Constant.backPicParam,param);
        editor.apply();
    }

    public static void setFlashLight(int mode){
        // 写入用户闪光灯首选项
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Constant.defFlashLightKey,mode);
        editor.apply();
    }
    public static int getFlashLight(){
        SharedPreferences camPos = PreferenceManager.getDefaultSharedPreferences(mContext);
        return camPos.getInt(Constant.defFlashLightKey, CameraHelper.FLIGHT_OFF);
    }
    public static boolean isFirstUseBlur(){
        SharedPreferences isFUB = PreferenceManager.getDefaultSharedPreferences(mContext);
        return isFUB.getBoolean(Constant.isFirstUseBlurKey, true);
    }
    public static void setFirstUseBlur(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constant.isFirstUseBlurKey,false);
        editor.apply();
    }
    public static int getPenSizePref(){
        SharedPreferences penSizePref = PreferenceManager.getDefaultSharedPreferences(mContext);
        return penSizePref.getInt(Constant.penSizeKey, Constant.defPenSize);
    }
    public static void setPenSizePref(int penSize){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Constant.penSizeKey,penSize);
        editor.apply();
    }
}
