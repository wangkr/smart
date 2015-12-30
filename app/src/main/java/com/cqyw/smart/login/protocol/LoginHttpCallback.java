package com.cqyw.smart.login.protocol;

/**
 * 登录回调
 * Created by Kairong on 2015/10/26.
 * mail:wangkrhust@gmail.com
 */
public interface LoginHttpCallback<T> {
    void onSuccess(T t);
    void onFailed(String code, String errorMsg);
}
