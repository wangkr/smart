package com.cqyw.smart.contact.helper;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.cqyw.smart.AppSharedPreference;
import com.cqyw.smart.R;
import com.cqyw.smart.common.http.ICommProtocol;
import com.cqyw.smart.common.http.JoyCommClient;
import com.cqyw.smart.config.AppCache;
import com.cqyw.smart.config.AppContext;
import com.cqyw.smart.contact.activity.UserProfileSettingActivity;
import com.cqyw.smart.contact.extensioninfo.model.ExtensionInfo;
import com.cqyw.smart.contact.extensioninfo.model.GalleryPhoto;
import com.cqyw.smart.contact.extensioninfo.model.GalleryPhotoTemp;
import com.cqyw.smart.util.Utils;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.joycustom.snap.SnapConstant;
import com.netease.nim.uikit.joycustom.upyun.JoyImageUtil;
import com.netease.nim.uikit.joycustom.upyun.UpYun;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.constant.GenderEnum;
import com.netease.nimlib.sdk.uinfo.constant.UserInfoFieldEnum;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hzxuwen on 2015/9/17.
 */
public class UserUpdateHelper {
    public static final String KEY_INFO_SEX = "sex";
    public static final String KEY_INFO_BIRTH = "birthday";
    public static final String KEY_INFO_HEAD = "head";
    public static final String KEY_INFO_NICK = "nick";
    public static final String KEY_INFO_PHOTOS = "photos";
    public static final String KEY_EDU_UNIVERSITY = "university";
    public static final String KEY_EDU_GRADE = "grade";
    public static final String KEY_EDU_ENTRYYEAR = "inyear";
    private static final String TAG = UserUpdateHelper.class.getSimpleName();

    /**
     * 更新云信用户资料
     */
    public static void update(final UserInfoFieldEnum field, final Object value, RequestCallbackWrapper<Void> callback) {
        Map<UserInfoFieldEnum, Object> fields = new HashMap<>(1);
        fields.put(field, value);
        update(fields, callback);
    }

    private static void update(final Map<UserInfoFieldEnum, Object> fields, final RequestCallbackWrapper<Void> callback) {
        NIMClient.getService(UserService.class).updateUserInfo(fields).setCallback(new RequestCallbackWrapper<Void>() {
            @Override
            public void onResult(int code, Void result, Throwable exception) {

                if (code == ResponseCode.RES_SUCCESS) {
                    LogUtil.i(TAG, "update userInfo success, update fields count=" + fields.size());
                } else {
                    if (exception != null) {
                        Toast.makeText(AppCache.getContext(), R.string.user_info_update_failed, Toast.LENGTH_SHORT).show();
                        LogUtil.i(TAG, "update userInfo failed, exception=" + exception.getMessage());
                    }
                }
                if (callback != null) {
                    callback.onResult(code, result, exception);
                }
            }
        });
    }

    /**
     * 更新本地服务器用户资料
     */

    public static void updateJoyUserInfo(NimUserInfo userInfo, String updateKey, String updateValue, ExtensionInfo extensionInfo, final RequestCallbackWrapper<Void> callback) {
        Map<String, String> map = new HashMap<>();
        map.put(KEY_INFO_NICK ,userInfo.getName());
        map.put(KEY_INFO_BIRTH, userInfo.getBirthday());
        map.put(KEY_INFO_HEAD, userInfo.getAvatar());
        map.put(KEY_INFO_SEX, (userInfo.getGenderEnum() == GenderEnum.FEMALE ? "0" : "1"));
        map.put(KEY_INFO_PHOTOS, extensionInfo.getPhotos());

        map.put(updateKey, updateValue);

        LogUtil.d(TAG, "updateJoyUserInfo="+map.toString());

        if (TextUtils.isEmpty(AppCache.getJoyId()) || TextUtils.isEmpty(AppSharedPreference.getCacheJoyToken())) {
            callback.onResult(-1, null, new Throwable("用户登录信息为空"));
            return;
        }
        JoyCommClient.getInstance().updateUserInfo2Server(AppCache.getJoyId(), AppSharedPreference.getCacheJoyToken(), map, new ICommProtocol.CommCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                callback.onResult(ResponseCode.RES_SUCCESS, aVoid, null);
            }

            @Override
            public void onFailed(String code, String errorMsg) {
                callback.onResult(Integer.valueOf(code), null, new Throwable(errorMsg));
            }
        });
    }

    public static void updateEduInfo(String university, String entryYear, String grade, final RequestCallbackWrapper<Void> callback) {
        Map<String, String> map = new HashMap<>();
        map.put(KEY_EDU_UNIVERSITY ,university);
        map.put(KEY_EDU_GRADE, grade);
        map.put(KEY_EDU_ENTRYYEAR, entryYear);

        if (TextUtils.isEmpty(AppCache.getJoyId()) || TextUtils.isEmpty(AppSharedPreference.getCacheJoyToken())) {
            callback.onResult(-1, null, new Throwable("用户登录信息为空"));
            return;
        }
        JoyCommClient.getInstance().updateEduInfo2Server(AppCache.getJoyId(), AppSharedPreference.getCacheJoyToken(), map, new ICommProtocol.CommCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                callback.onResult(ResponseCode.RES_SUCCESS, aVoid, null);
            }

            @Override
            public void onFailed(String code, String errorMsg) {
                callback.onResult(Integer.valueOf(code), null, new Throwable(errorMsg));
            }
        });
    }

    /**
     * 上传照片墙照片
     * @param galleryPhotoTemps
     */
    public static void uploadGalPhoto(final Handler handler, final List<GalleryPhotoTemp> galleryPhotoTemps) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int total = 0;
                // 统计总数
                for (GalleryPhotoTemp gpt1 : galleryPhotoTemps) {
                    if (gpt1.isLocal()) {
                        total++;
                    }
                }
                int curNum = 0;
                Bundle bundle = new Bundle();
                for (GalleryPhotoTemp gpt :
                        galleryPhotoTemps) {
                    if (gpt.isLocal()) {
                        // 上传到UpYun
                        String fileName = new SimpleDateFormat(JoyImageUtil.DATE_FORMAT).format(new Date(System.currentTimeMillis())) + UpYun.SEPARATOR + JoyImageUtil.genJoyyunFilenameFromLocalPath(gpt.getPath());
                        try {
                            if (JoyImageUtil.getJoyyunComm().writeFile(JoyImageUtil.genGalPhotoRltPath(fileName), new File(gpt.getPath()), true)) {
                                gpt.setUrl(fileName);
                                gpt.setLocal(false);
                                curNum++;
                                bundle.putInt("progress", curNum);
                                bundle.putInt("total", total);
                                bundle.putSerializable("gpt", gpt);
                                Message msg = Message.obtain(handler, UserProfileSettingActivity.UPLOAD_PROGRESS);
                                msg.setData(bundle);
                                msg.sendToTarget();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Message.obtain(handler, UserProfileSettingActivity.UPLOAD_FAILED).sendToTarget();
                        }
                    }
                }

                Message.obtain(handler, UserProfileSettingActivity.UPLOAD_SUCCESS).sendToTarget();
                galleryPhotoTemps.clear();
            }
        }).start();

    }
}
