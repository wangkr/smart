package com.cqyw.smart.contact.note;

/**
 * Created by Kairong on 2015/10/29.
 * mail:wangkrhust@gmail.com
 */
public interface NoteHttpCallback<T>{
    void onSuccess(T note);
    void onFailed(String code, String errorMsg);
}
