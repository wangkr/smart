package com.cqyw.smart.config;

import android.content.Context;

import com.cqyw.smart.AppSharedPreference;
import com.netease.nim.uikit.NimUIKit;

/**
 * Created by jezhee on 2/20/15.
 */
public class AppCache {

    private static Context context;

    private static String id;

    private static String joyToken;
    // 验证状态1未验证， 2验证
    private static int status;

    public static void clear() {
        id = null;
        joyToken = null;
        AppSharedPreference.saveJoyToken("");
        AppSharedPreference.saveJoyId("");
    }

    public static String getJoyToken() {
        return joyToken;
    }

    public static void setJoyToken(String joyToken) {
        AppCache.joyToken = joyToken;
    }

    public static String getJoyId() {
        return id;
    }

    public static void setJoyId(String id) {
        AppCache.id = id;
        NimUIKit.setAccount(id);
    }

    public static boolean isStatusValid() {
        return status == 2;
    }

    public static void setStatus(int status) {
        AppCache.status = status;
    }

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        AppCache.context = context.getApplicationContext();
    }
}
