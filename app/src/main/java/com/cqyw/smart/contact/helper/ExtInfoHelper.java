package com.cqyw.smart.contact.helper;

import android.text.TextUtils;

import com.cqyw.smart.AppSharedPreference;
import com.cqyw.smart.config.AppContext;
import com.cqyw.smart.contact.ExtensionParse;
import com.cqyw.smart.contact.note.ExtInfoProviderHandler;
import com.cqyw.smart.util.StringUtils;
import com.netease.nim.uikit.cache.NimUserInfoCache;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.uinfo.UserInfoProvider;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kairong on 2015/10/29.
 * mail:wangkrhust@gmail.com
 */
public class ExtInfoHelper {
    private static String TAG = ExtInfoHelper.class.getSimpleName();
    private static List<Observer<Void>> observers = new ArrayList<>();

    /**
     * 注册好友备注更改监听
     * @param observer
     * @param register
     */
    public static void registerObserver(Observer<Void> observer, boolean register) {
        if (register) {
            if (observer == null) {
                return;
            }
            synchronized(observers) {
                if (observers.contains(observer)) {
                    return;
                }
                observers.add(observer);
            }
        } else {
            if (observer == null) {
                return;
            }
            synchronized(observers) {
                int index = observers.indexOf(observer);
                if (index == -1) {
                    return;
                }
                observers.remove(index);
            }
        }
    }


    public static void buildCache(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(!AppSharedPreference.getContinuousLoginFlag()){
                    ExtInfoProviderHandler.getInstance().fetchNoteInfofromRemote();
                } else {
                    ExtInfoProviderHandler.getInstance().buildCache();
                }
            }
        }).start();
    }

    public static void clear(){
        ExtInfoProviderHandler.getInstance().clear();
    }

    /**
     * 当用户添加好友成功时，调用此函数
     * @param friend_id 朋友id
     * @param note 备注
     */
    public static boolean initFriendNote(String friend_id, String friend_nick, String note, String university, String jo){
        return ExtInfoProviderHandler.getInstance().addNote(AppContext.getContext(), friend_id, friend_nick, note, university, jo);
    }

    /**
     * 删除好友备注
     * @param friend_id 朋友id
     * @return 状态
     */
    public static boolean deleteFriendNote(String friend_id) {
        if (observers.size() > 0) {
            for (Observer<Void> observer : observers) {
                observer.onEvent(null);
            }
        }
        return ExtInfoProviderHandler.getInstance().deleteNote(AppContext.getContext(), friend_id);
    }

    /**
     * 当用户添加/修改 好友备注时，调用此函数
     * @param friend_id 朋友id
     * @param note 备注
     */
    public static boolean updateFriendNote(String friend_id, String note){
        boolean re = ExtInfoProviderHandler.getInstance().updateNote(AppContext.getContext(), friend_id, note);
        if (re && observers.size() > 0) {
            for (Observer<Void> observer : observers) {
                observer.onEvent(null);
            }
        }
        return re;
    }

    /**
     * 当用户更改昵称时，调用此函数
     * @param friend_id 朋友id
     * @param friend_nick 朋友昵称
     */
    public static boolean updateFriendNick(String friend_id, String friend_nick){
        return ExtInfoProviderHandler.getInstance().updateNick(AppContext.getContext(), friend_id, friend_nick);
    }

    /**
     * 获取好友备注
     * @param friend_id 朋友id
     * @return 返回备注
     */
    public static String getNote(String friend_id){
        if (TextUtils.isEmpty(friend_id)){
            return StringUtils.Empty;
        }
        return ExtInfoProviderHandler.getInstance().getNote(friend_id);
    }

    /**
     * 获取用户显示的名字，默认显示备注名字
     * 如果备注为空则显示昵称
     * @param user_id 用户id
     * @return 返回备注
     */
    public static String getDisplayName(String user_id){
        if (TextUtils.isEmpty(user_id)){
            return StringUtils.Empty;
        }
        // 本地缓存读取
        /*备注*/
        String note = ExtInfoProviderHandler.getInstance().getNote( user_id);
        if (!TextUtils.isEmpty(note)){
            return note;
        }
        /*昵称*/
        String  nick =  NimUserInfoCache.getInstance().getUserName(user_id);

        // 从云信获取
        if (nick == null){
            /*云信缓存*/
            UserInfoProvider.UserInfo userInfo = NimUserInfoCache.getInstance().getUserInfo(user_id);
            if (userInfo != null) {
                return userInfo.getName();
            }
            return null;
        } else {
            updateFriendNick(user_id, nick);
            return nick;
        }
    }

    public static String getUniversity(String user_id) {
        if (TextUtils.isEmpty(user_id)){
            return StringUtils.Empty;
        }
        // 本地缓存读取
        /*备注*/
        String university = ExtInfoProviderHandler.getInstance().getUniversity(user_id);
        if (!TextUtils.isEmpty(university)){
            return university;
        }

        // 从云信获取
        if (university == null){
            /*云信缓存*/
            NimUserInfo userInfo = NimUserInfoCache.getInstance().getUserInfo(user_id);
            if (userInfo != null) {
                university = ExtensionParse.getInstance().getUniversity(userInfo.getExtension());
                return university;
            }
            return null;
        } else {
            return university;
        }
    }

    public static boolean isEdit(){
        return ExtInfoProviderHandler.getInstance().isEdit();
    }


}
