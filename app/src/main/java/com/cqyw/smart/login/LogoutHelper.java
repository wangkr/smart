package com.cqyw.smart.login;

import com.cqyw.smart.common.http.JoyHttpClient;
import com.cqyw.smart.config.AppCache;
import com.cqyw.smart.contact.extensioninfo.ExtensionInfoCache;
import com.netease.nim.uikit.LoginSyncDataStatusObserver;
import com.netease.nim.uikit.NimUIKit;

/**
 * 注销帮助类
 * Created by huangjun on 2015/10/8.
 */
public class LogoutHelper {
    public static void logout() {
        // 清理缓存&注销监听&清除状态
        NimUIKit.clearCache();
        AppCache.clear();
        ExtensionInfoCache.clear();
        LoginSyncDataStatusObserver.getInstance().reset();
        JoyHttpClient.getInstance().cancellAll();
    }
}
