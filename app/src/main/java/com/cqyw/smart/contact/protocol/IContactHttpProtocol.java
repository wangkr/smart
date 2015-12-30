package com.cqyw.smart.contact.protocol;

import com.cqyw.smart.common.http.JoyHttpProtocol;

import java.util.Map;

/**
 * 通讯录数据获取协议
 * <p/>
 * Created by huangjun on 2015/3/6.
 */
public interface IContactHttpProtocol extends JoyHttpProtocol{

    /**
     * ************************ protocol interface ******************************
     */

    // 注册
    void register(String account, String password, String code, ContactHttpCallback<Void> callback);
    // 找回密码
    void resetPassword(String account, String password, String code, ContactHttpCallback<Void> callback);

    // 获取/验证 验证码
    void getVertifyCode(String phone, ContactHttpCallback<Void> callback);
    void getVertifyCode1(String phone, ContactHttpCallback<Void> callback);
    void verifyCode(String phone, String code, ContactHttpCallback<Void> callback);
}
