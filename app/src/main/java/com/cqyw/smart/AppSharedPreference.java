package com.cqyw.smart;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.text.TextUtils;

import com.cqyw.smart.config.AppCache;
import com.cqyw.smart.config.AppConstants;
import com.cqyw.smart.config.AppContext;
import com.netease.nim.uikit.common.util.sys.TimeUtil;

/**
 * Created by Kairong on 2015/9/29.
 * mail:wangkrhust@gmail.com
 */
public class AppSharedPreference {

    private static final String KEY_NIM_TOKEN = "nim_token";
    private static final String KEY_JOY_TOKEN = "joy_token";
    private static final String KEY_JOY_ID = "joy_id";
    private static final String KEY_LOGIN_FLAG = "login_flag";
    private static final String KEY_FIRST_IN = "first_in";
    private static final String KEY_EDUINFO_EDITED = "edu_info_edited";
    private static final String KEY_TOKEN_TIME = "token_time";
    private static final String KEY_LASTCHECKUPDATE_TIME = "last_checkupdate_time";
    private static final String KEY_USER_ACCOUNT = "user_account";
    private static final String KEY_USER_MD5PWD = "user_md5pwd";
    private static final String KEY_LASTREFRESH_TIME = "last_refresh_time";
    private static final String KEY_LASTKNOWN_POSITION = "last_known_position";
    private static final String KEY_NOTETABLE_UPLOADSTATE = "notetable_uploadstate";
    private static final String KEY_LOCALCONTACT_SWITCH = "localcontact_switch";
    private static final String KEY_FIRST_START_CAMERA = "first_start_camera";

    public AppSharedPreference(){
    }

    public static void saveNimToken(String token) {
        AppContext.set(KEY_NIM_TOKEN, token);
    }

    public static String getNimToken() {
        return AppContext.get(KEY_NIM_TOKEN, null);
    }

    public static void saveUserAccount(String account) {
        AppContext.set(KEY_USER_ACCOUNT, account);
    }

    public static String getUserAccount(){
        return AppContext.get(KEY_USER_ACCOUNT, null);
    }

    public static void setLocalcontactSwitch(boolean match) {
        AppContext.set(KEY_LOCALCONTACT_SWITCH, match);
    }

    public static boolean isFirstStartCam(){
        return AppContext.get(KEY_FIRST_START_CAMERA, true);
    }

    public static void setFirstStartCam(boolean firstStartCam) {
        AppContext.set(KEY_FIRST_START_CAMERA, firstStartCam);
    }

    public static boolean getLocalcontactSwitch() {
        return AppContext.get(KEY_LOCALCONTACT_SWITCH, true);
    }

    public static void saveUserMD5Passwd(String passwd) {
        AppContext.set(KEY_USER_MD5PWD, passwd);
    }

    public static String getUserMD5Passwd(){
        return AppContext.get(KEY_USER_MD5PWD, null);
    }

    public static long getTokenSavedTime() {
        return AppContext.get(KEY_TOKEN_TIME, TimeUtil.currentTimeMillis());
    }

    public static void saveJoyToken(String token){
        /*同时记录保存token的时间*/
        AppContext.set(KEY_TOKEN_TIME, TimeUtil.currentTimeMillis());
        AppContext.set(KEY_JOY_TOKEN, token);
    }

    public static String getCacheJoyToken(){
        return AppCache.getJoyToken();
    }

    public static String getJoyToken() {
        return AppContext.get(KEY_JOY_TOKEN, null);
    }

    public static void setNotetableUpState(boolean success) {
        AppContext.set(KEY_NOTETABLE_UPLOADSTATE, success);
    }

    public static boolean getNoteTableUploadState() {
        return AppContext.get(KEY_NOTETABLE_UPLOADSTATE, false);
    }

    public static void saveJoyId(String joyId){
        AppContext.set(KEY_JOY_ID, joyId);
    }

    public static String getJoyId(){
        return AppContext.get(KEY_JOY_ID, null);
    }

    public static void setContinuousLoginFlag(boolean isContinuous){
        AppContext.set(KEY_LOGIN_FLAG, isContinuous);
    }

    public static boolean getContinuousLoginFlag(){
        return AppContext.get(KEY_LOGIN_FLAG, false);
    }

    public static void saveFirstIn(){
        AppContext.set(KEY_FIRST_IN, false);
    }

    public static boolean isFirstIn() {
        return AppContext.get(KEY_FIRST_IN, true);
    }

    public static void setEduinfoEdited(boolean eduinfoEdited){
        AppContext.set(KEY_EDUINFO_EDITED, eduinfoEdited);
    }

    public static boolean isEduinfoEdited(){
        return AppContext.get(KEY_EDUINFO_EDITED, false);
    }

    public static void setLastCheckUpdateTime(String time) {
        AppContext.set(KEY_LASTCHECKUPDATE_TIME, time);
    }

    public static String getLastCheckUpdateTime(){
        return AppContext.get(KEY_LASTCHECKUPDATE_TIME, null);
    }

    public static void saveLastRefreshTime() {
        AppContext.set(KEY_LASTREFRESH_TIME, TimeUtil.getDateTimeString(TimeUtil.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss"));
    }

    public static String getLastRefreshTime(){
        return AppContext.get(KEY_LASTREFRESH_TIME, TimeUtil.getDateTimeString(TimeUtil.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss"));
    }

    public static void saveLastKnownPosition(String lat, String lng) {
        AppContext.set(KEY_LASTKNOWN_POSITION, lat + " " + lng);
    }

    public static String getLastKnownPosition() {
        return AppContext.get(KEY_LASTKNOWN_POSITION, null);
    }

}
