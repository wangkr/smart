package com.cqyw.smart.login.protocol;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.cqyw.smart.common.http.JoyHttpClient;
import com.cqyw.smart.config.JoyServers;
import com.netease.nim.uikit.common.util.log.LogUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 本地服务器登录客户端
 * Created by Kairong on 2015/10/27.
 * mail:wangkrhust@gmail.com
 */
public class LoginHttpClient implements ILoginHttpProtocol {
    private static final String TAG = "LoginHttpClient";

    private static LoginHttpClient instance;

    public static synchronized LoginHttpClient getInstance() {
        if (instance == null) {
            instance = new LoginHttpClient();
        }

        return instance;
    }

    private LoginHttpClient(){
        JoyHttpClient.getInstance().init();
    }

    @Override
    public void login(String account, String password, final LoginHttpCallback<JSONObject> callback) {
        String url = JoyServers.joyServer() + API_NAME_LOGIN;

        Map<String, String> headers = new HashMap<>(1);
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(REQUEST_KEY_PHONE, account);
        jsonObject.put(REQUEST_KEY_PASSWORD, password);

        JoyHttpClient.getInstance().execute(url, headers, jsonObject, new JoyHttpClient.JoyHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    LogUtil.e(TAG, "local server login failed : code = " + code + ", errorMsg = " + errorMsg);
                    if (callback != null) {
                        callback.onFailed(""+code, errorMsg);
                    }
                    return;
                }

                try {
                    JSONObject resObj = JSONObject.parseObject(response);
                    String resCode = resObj.getString(RESULT_KEY_CODE).substring(4, 6);
                    if (resCode.equals(STATUS_CODE_SUCCESS)) {
                        callback.onSuccess(resObj.getJSONObject(RESULT_KEY_DATA));
                    } else {
                        String error = resObj.getString(RESULT_KEY_MSG);
                        callback.onFailed(resCode, error);
                    }
                } catch (JSONException e) {
                    callback.onFailed("-1", e.getMessage());
                }
            }
        }, false, true);
    }
}
