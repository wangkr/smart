package com.cqyw.smart.main.util;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baidu.location.BDLocation;
import com.cqyw.smart.AppSharedPreference;
import com.cqyw.smart.common.http.ICommProtocol;
import com.cqyw.smart.common.http.JoyCommClient;
import com.cqyw.smart.config.AppCache;
import com.cqyw.smart.config.AppContext;
import com.cqyw.smart.location.model.NimLocation;
import com.cqyw.smart.main.model.PublicSnapMessage;
import com.cqyw.smart.main.model.RecentSnapNews;
import com.cqyw.smart.main.service.MessageMarkDBService;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nim.uikit.joycustom.snap.SnapConstant;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Kairong on 2015/11/17.
 * mail:wangkrhust@gmail.com
 */
public class SnapnewsUtils {
    public interface NewsCallback<T>{
        void onResult(int itemNum, T data, String errorMsg);
    }

    /**
     * 从服务器拉取最新的消息
     * @param nid
     * @param ifNew
     * @param location
     * @param callback
     */
    public static void pullNewsFromServer(String nid, boolean ifNew, NimLocation location, final Activity context,
                                          final NewsCallback<List<PublicSnapMessage>> callback) {
        JoyCommClient.getInstance().getSnapnews(AppCache.getJoyId(), AppSharedPreference.getCacheJoyToken(), nid, ifNew,
                location, new ICommProtocol.CommCallback<List<PublicSnapMessage>>() {
            @Override
            public void onSuccess(final List<PublicSnapMessage> messages) {
                if (messages != null) {
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (messages.size() == 0) {
                                callback.onResult(0, messages, "没有更多新鲜事了");
                            } else {
                                callback.onResult(messages.size(), messages, null);
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailed(String code,final String errorMsg) {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResult(-1, null, errorMsg);
                    }
                });
            }
        });
    }



    public static LinkedList<PublicSnapMessage> getPubSnapListBySavedBundle(Bundle bundle){
        LinkedList<PublicSnapMessage> list = new LinkedList<PublicSnapMessage>();
        for (int i = 0; i < bundle.size(); i++) {
            list.add(new PublicSnapMessage(bundle.getBundle("PM" + i)));
        }
        return list;
    }

    public static Bundle getPubSnapListSaveBundle(List<PublicSnapMessage> list){
        Bundle bundle = new Bundle();
        for (int i = 0; i < list.size(); i++) {
            bundle.putBundle("PM" + i, list.get(i).getSaveBundle());
        }
        return bundle;
    }

    public static LinkedList<RecentSnapNews> getRecentNewsListBySavedBundle(Bundle bundle) {
        LinkedList<RecentSnapNews> list = new LinkedList<RecentSnapNews>();
        for (int i = 0; i < bundle.size(); i++) {
            list.add(new RecentSnapNews(bundle.getBundle("RSN" + i)));
        }
        return list;
    }

    public static Bundle getRecentSnapListSaveBundle(LinkedList<RecentSnapNews> list){
        Bundle bundle = new Bundle();
        for (int i = 0; i < list.size(); i++) {
            bundle.putBundle("RSN" + i, list.get(i).getSaveBundle());
        }
        return bundle;
    }

    public static void markPublicSnapMsg(final PublicSnapMessage message) {
        final MessageMarkDBService messageMarkDBService = new MessageMarkDBService(AppContext.getContext());
        JSONArray ja = new JSONArray();
        JSONObject jo = new JSONObject();
        jo.put("nid", message.getId());
        jo.put("uid", message.getUid());
        ja.add(jo);
        JoyCommClient.getInstance().markSnapnews(AppCache.getJoyId(), AppSharedPreference.getCacheJoyToken(), ja.toString(), new ICommProtocol.CommCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }

            @Override
            public void onFailed(String code, String errorMsg) {
                Map<String, String> nids = new HashMap<String, String>();
                nids.put(message.getId(), message.getUid());
                messageMarkDBService.saveMarks(nids);
            }
        });
    }

    public static void markFailedPublicSnapMsg() {
        final MessageMarkDBService messageMarkDBService = new MessageMarkDBService(AppContext.getContext());
        Map<String, String> nids = messageMarkDBService.findAllMarks();
        if (nids.size() > 0) {
            JSONArray jsonArray = new JSONArray();
            for (String nid: nids.keySet()) {
                JSONObject jo = new JSONObject();
                jo.put("nid",nid);
                jo.put("uid",nids.get(nid));
                jsonArray.add(jo);
            }
            JoyCommClient.getInstance().markSnapnews(AppCache.getJoyId(), AppSharedPreference.getCacheJoyToken(), jsonArray.toString(), new ICommProtocol.CommCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    messageMarkDBService.deleteAll();
                }

                @Override
                public void onFailed(String code, String errorMsg) {

                }
            });
        }
    }

}
