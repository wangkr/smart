package com.cqyw.smart.login.protocol;

import com.alibaba.fastjson.JSONObject;
import com.cqyw.smart.common.http.JoyHttpProtocol;

/**
 * 登录协议
 * Created by Kairong on 2015/10/26.
 * mail:wangkrhust@gmail.com
 */
public interface ILoginHttpProtocol extends JoyHttpProtocol{
    // api
    String API_NAME_LOGIN = "login/login";


    /**
     * ************************ protocol interface ******************************
     */

    // 登录
    void login(String account, String password, LoginHttpCallback<JSONObject> callback);
}
