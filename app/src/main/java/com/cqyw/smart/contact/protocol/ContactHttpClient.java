package com.cqyw.smart.contact.protocol;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.cqyw.smart.R;
import com.cqyw.smart.common.http.JoyHttpClient;
import com.cqyw.smart.config.AppCache;
import com.cqyw.smart.config.AppConfig;
import com.cqyw.smart.config.AppConstants;
import com.cqyw.smart.config.AppContext;
import com.cqyw.smart.config.JoyServers;
import com.cqyw.smart.util.StringUtils;
import com.cqyw.smart.util.nimserver.CheckSumBuilder;
import com.netease.nim.uikit.common.util.log.LogUtil;

import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * 通讯录数据获取协议的实现
 * <p/>
 * Created by huangjun on 2015/3/6.
 */
public class ContactHttpClient implements IContactHttpProtocol {
    private static final String TAG = "ContactHttpClient";


    /**
     * ************************ constant ******************************
     */

    // api
    private static String API_NAME_REGISTER;
    private static String API_NAME_RESETPASSWORD;
    private static String API_VERIFY_CODE;
    private static String API_VERIFY_CODE1;

    private static ContactHttpClient instance;

    public static synchronized ContactHttpClient getInstance() {
        if (instance == null) {
            instance = new ContactHttpClient();
        }

        return instance;
    }

    private ContactHttpClient() {
        JoyHttpClient.getInstance().init();
    }

    public static void init() {
        API_NAME_REGISTER = AppContext.getContext().getString(R.string.api_name_register);
        API_NAME_RESETPASSWORD = AppContext.getContext().getString(R.string.api_name_resetpassword);
        API_VERIFY_CODE = AppContext.getContext().getString(R.string.api_verify_code);
        API_VERIFY_CODE1 = AppContext.getContext().getString(R.string.api_verify_code1);
    }

    /**
     * *********************************** IContactHttpProtocol ******************************************
     */

    /**
     * 本地服务器注册
     * @param account 账号
     * @param password 密码
     * @param code 验证码
     * @param callback 回调
     */
    @Override
    public void register(String account, String password, String code, final ContactHttpCallback<Void> callback) {
        String url = JoyServers.joyServer() + API_NAME_REGISTER;

        Map<String, String> headers = new HashMap<>(1);
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(REQUEST_KEY_PHONE, account);
        jsonObject.put(REQUEST_KEY_PASSWORD, password);
        jsonObject.put(REQUEST_KEY_CODE, code);

        JoyHttpClient.getInstance().execute(url, headers, jsonObject, new JoyHttpClient.JoyHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    LogUtil.e(TAG, "register failed : code = " + code + ", errorMsg = " + errorMsg);
                    if (callback != null) {
                        callback.onFailed(code, errorMsg);
                    }
                    return;
                }

                try {
                    JSONObject resObj = JSONObject.parseObject(response);
                    String resCode = resObj.getString(RESULT_KEY_CODE).substring(4, 6);
                    LogUtil.d("ContactHttpClient", "register code = "+ resCode);
                    if (resCode.equals(STATUS_CODE_SUCCESS)) {
                        callback.onSuccess(null);
                    } else {
                        String error = resObj.getString(RESULT_KEY_MSG);
                        callback.onFailed(Integer.valueOf(resCode), error);
                    }
                } catch (JSONException e) {
                    callback.onFailed(-1, e.getMessage());
                }
            }
        }, false, true);
    }

    /**
     * 重置密码
     * @param account 账号
     * @param new_password 新密码
     * @param code 验证码
     * @param callback 回调
     */
    @Override
    public void resetPassword(String account, String new_password, String code, final ContactHttpCallback<Void> callback) {
        String url = JoyServers.joyServer() + API_NAME_RESETPASSWORD;

        Map<String, String> headers = new HashMap<>(1);
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(REQUEST_KEY_PHONE, account);
        jsonObject.put(REQUEST_KEY_PASSWORD, new_password);
        jsonObject.put(REQUEST_KEY_CODE, code);

        JoyHttpClient.getInstance().execute(url, headers, jsonObject, new JoyHttpClient.JoyHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    LogUtil.e(TAG, "reset password failed : code = " + code + ", errorMsg = " + errorMsg);
                    if (callback != null) {
                        callback.onFailed(code, errorMsg);
                    }
                    return;
                }

                try {
                    JSONObject resObj = JSONObject.parseObject(response);
                    String resCode = resObj.getString(RESULT_KEY_CODE).substring(4, 6);
                    if (resCode.equals(STATUS_CODE_SUCCESS)) {
                        callback.onSuccess(null);
                    } else {
                        String error = resObj.getString(RESULT_KEY_MSG);
                        callback.onFailed(Integer.valueOf(resCode), error);
                    }
                } catch (JSONException e) {
                    callback.onFailed(-1, e.getMessage());
                }
            }
        }, false, true);
    }

    @Override
    public void verifyCode(String phone, String code,final ContactHttpCallback<Void> callback) {
        String url = AppConstants.VerifyCodeURL;
        Map<String, String> headers = new HashMap<>();
        String nonce = StringUtils.getRandomNumber(6);
        String curTime = StringUtils.curTime();
        headers.put(AppConstants.appKeyKey, AppConfig.AppKey);
        headers.put(AppConstants.nonceKey, nonce);
        headers.put(AppConstants.curTimeKey, curTime);
        headers.put(AppConstants.checkSumKey, CheckSumBuilder.getCheckSum(AppConfig.AppSecret, nonce, curTime));
        headers.put("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");


        JSONObject jsonObject = new JSONObject();
        jsonObject.put("mobile", phone);
        jsonObject.put(REQUEST_KEY_CODE, code);

        JoyHttpClient.getInstance().execute(url, headers, jsonObject, new JoyHttpClient.JoyHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    LogUtil.e(TAG, "send code failed : code = " + code + ", errorMsg = " + errorMsg);
                    if (callback != null) {
                        callback.onFailed(code, errorMsg);
                    }
                    return;
                }

                try {
                    JSONObject resObj = JSONObject.parseObject(response);
                    int resCode = resObj.getIntValue(RESULT_KEY_CODE);
                    if (resCode == HttpStatus.SC_OK) {
                        callback.onSuccess(null);
                    } else {
                        String error = resObj.getString("msg");
                        callback.onFailed(resCode, error);
                    }
                } catch (JSONException e) {
                    callback.onFailed(-1, e.getMessage());
                }
            }
        }, false, true);
    }

    /**
     * 获取验证码。注册使用
     * @param phone
     * @param callback
     */
    @Override
    public void getVertifyCode(String phone,final ContactHttpCallback<Void> callback) {
        String url = JoyServers.joyServer() + API_VERIFY_CODE;
        Map<String, String> headers = new HashMap<>(1);
        headers.put("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(REQUEST_KEY_PHONE, phone);


        JoyHttpClient.getInstance().execute(url, headers, jsonObject, new JoyHttpClient.JoyHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    LogUtil.e(TAG, "send code failed : code = " + code + ", errorMsg = " + errorMsg);
                    if (callback != null) {
                        callback.onFailed(code, errorMsg);
                    }
                    return;
                }

                try {
                    JSONObject resObj = JSONObject.parseObject(response);
                    String resCode = resObj.getString(RESULT_KEY_CODE).substring(4, 6);
                    if (TextUtils.equals(resCode, STATUS_CODE_SUCCESS)) {
                        callback.onSuccess(null);
                    } else {
                        String error = resObj.getString(RESULT_KEY_MSG);
                        callback.onFailed(Integer.valueOf(resCode), error);
                    }
                } catch (JSONException e) {
                    callback.onFailed(-1, e.getMessage());
                }
            }
        }, false, true);
    }

    /**
     * 找回密码使用
     * @param phone
     * @param callback
     */
    @Override
    public void getVertifyCode1(String phone,final ContactHttpCallback<Void> callback) {
        String url = JoyServers.joyServer() + API_VERIFY_CODE1;
        Map<String, String> headers = new HashMap<>(1);
        headers.put("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(REQUEST_KEY_PHONE, phone);


        JoyHttpClient.getInstance().execute(url, headers, jsonObject, new JoyHttpClient.JoyHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    LogUtil.e(TAG, "send code failed : code = " + code + ", errorMsg = " + errorMsg);
                    if (callback != null) {
                        callback.onFailed(code, errorMsg);
                    }
                    return;
                }

                try {
                    JSONObject resObj = JSONObject.parseObject(response);
                    String resCode = resObj.getString(RESULT_KEY_CODE).substring(4, 6);
                    if (TextUtils.equals(resCode, STATUS_CODE_SUCCESS)) {
                        callback.onSuccess(null);
                    } else {
                        String error = resObj.getString(RESULT_KEY_MSG);
                        callback.onFailed(Integer.valueOf(resCode), error);
                    }
                } catch (JSONException e) {
                    callback.onFailed(-1, e.getMessage());
                }
            }
        }, false, true);
    }

    private String readAppKey() {
        try {
            ApplicationInfo appInfo = AppCache.getContext().getPackageManager()
                    .getApplicationInfo(AppCache.getContext().getPackageName(), PackageManager.GET_META_DATA);
            if (appInfo != null) {
                return appInfo.metaData.getString("com.netease.nim.appKey");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
