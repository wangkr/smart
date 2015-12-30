package com.cqyw.smart.contact.extensioninfo;

import com.cqyw.smart.config.AppContext;
import com.cqyw.smart.contact.extensioninfo.model.ExtensionInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kairong on 2015/12/25.
 * mail:wangkrhust@gmail.com
 */
public class ExtensionInfoCache {
    private static Map<String, ExtensionInfo> extInfoCache = new HashMap<>();
    private static ExtensionInfoDBService dbService;

    public static void build() {
        dbService = new ExtensionInfoDBService(AppContext.getContext());
        extInfoCache.putAll(dbService.getAllExtensionInfo());
    }

    public static ExtensionInfo getExtensionInfo (String account) {
        return extInfoCache.get(account);
    }

    public static void saveExtensionInfo (String account, ExtensionInfo extensionInfo) {
        extInfoCache.put(account, extensionInfo);
    }

    public static void clear() {
        dbService.saveExtensionInfoList(extInfoCache);
        extInfoCache.clear();
    }
}
