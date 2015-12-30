package com.cqyw.smart.main.model;

import android.content.Context;


/**
 * 广场新鲜事列表一些点击事件的响应处理函数
 * Created by Kairong on 2015/11/14.
 * mail:wangkrhust@gmail.com
 */
public interface PublicAvatarOnclickListener {
    // 头像点击事件处理，一般用于打开用户资料页面
    void onAvatarClicked(Context context, PublicSnapMessage message);

    void onAvatarLongClicked(Context context, PublicSnapMessage message);
}
