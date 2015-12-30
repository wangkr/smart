package com.netease.nim.uikit.uinfo;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.cache.TeamDataCache;
import com.netease.nim.uikit.common.util.string.StringUtil;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.uinfo.UserInfoProvider;

import java.util.List;

public class UserInfoHelper {

    private static UserInfoObservable userInfoObservable;

    // 默认显示用户好友的备注
    public static String getUserName(String account) {
        String note = NimUIKit.getContactProvider().getUserDisplayName(account);
        if (!TextUtils.isEmpty(note)){
            return note;
        }

        UserInfoProvider.UserInfo userInfo = NimUIKit.getUserInfoProvider().getUserInfo(account);
        if (userInfo != null && !TextUtils.isEmpty(userInfo.getName())) {
            return userInfo.getName();
        } else {
            return "未知";
        }
    }

    public static String getUserUniversity(String account) {
        String ext = NimUIKit.getContactProvider().getExtension(account);
        if (!TextUtils.isEmpty(ext)) {
            JSONObject jsonObject = JSONObject.parseObject(ext);
            return jsonObject.getString("university");
        }

        return StringUtil.Empty;
    }


    // 获取用户显示在标题栏和最近联系人中的名字
    public static String getUserTitleName(String id, SessionTypeEnum sessionType) {
        if (sessionType == SessionTypeEnum.P2P) {
            if (NimUIKit.getAccount().equals(id)) {
                return "我的电脑";
            } else {
                return getUserName(id);
            }
        }  else if (sessionType == SessionTypeEnum.Team) {
            return TeamDataCache.getInstance().getTeamName(id);
        }
        return id;
    }

    /**
     * 注册用户资料变化观察者。<br>
     *     注意：不再观察时(如Activity destroy后)，要unregister，否则会造成资源泄露
     * @param observer 观察者
     */
    public static void registerObserver(UserInfoObservable.UserInfoObserver observer) {
        if (userInfoObservable == null) {
            userInfoObservable = new UserInfoObservable(NimUIKit.getContext());
        }
        userInfoObservable.registerObserver(observer);
    }

    /**
     * 注销用户资料变化观察者。
     * @param observer 观察者
     */
    public static void unregisterObserver(UserInfoObservable.UserInfoObserver observer) {
        if (userInfoObservable != null) {
            userInfoObservable.unregisterObserver(observer);
        }
    }

    /**
     * 当用户资料发生改动时，请调用此接口，通知更新UI
     * @param accounts 有用户信息改动的帐号列表
     */
    public static void notifyChanged(List<String> accounts) {
        if (userInfoObservable != null) {
            userInfoObservable.notifyObservers(accounts);
        }
    }
}
