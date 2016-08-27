package com.cqyw.smart.contact;

import android.util.Log;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.cqyw.smart.R;
import com.cqyw.smart.common.http.JoyHttpClient;
import com.cqyw.smart.config.AppContext;
import com.cqyw.smart.config.JoyServers;
import com.cqyw.smart.contact.note.ContactExtInfo;
import com.cqyw.smart.contact.note.NoteHttpCallback;
import com.cqyw.smart.contact.note.NoteHttpProtocol;
import com.netease.nim.uikit.common.util.log.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 从服务器端获取好友备注
 * Created by Kairong on 2015/10/16.
 * mail:wangkrhust@gmail.com
 */
public class FriendNoteClient implements NoteHttpProtocol{
    private static final String TAG = "FriendNoteClient";


    private static String API_FRIENDSNOTE_LIST_GET;
    private static String API_FRIENDSNOTE_LIST_SET;

    private static FriendNoteClient instance = null;

    public static synchronized FriendNoteClient getInstance(){
        if(instance == null){
            return new FriendNoteClient();
        }
        return instance;
    }

    public static void init() {
        API_FRIENDSNOTE_LIST_GET = AppContext.getContext().getString(R.string.api_friendsnote_list_get);
        API_FRIENDSNOTE_LIST_SET = AppContext.getContext().getString(R.string.api_friendsnote_list_set);
    }

    private FriendNoteClient(){
        JoyHttpClient.getInstance().init();
    }
    @Override
    public void updateRemoteNoteList(String id, String token, List<Map<String, Object>> notelist, final NoteHttpCallback<Void> callback) {
        String url = JoyServers.joyServer() + API_FRIENDSNOTE_LIST_SET;
        Map<String, String> headers = new HashMap<>(1);
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");

        // 转成json
        JSONArray ja = new JSONArray();
        for (Map<String,Object> map:notelist) {
            ja.add(new JSONObject(map));
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(REQUEST_KEY_ID, id);
        jsonObject.put(REQUEST_KEY_TOKEN, token);
        jsonObject.put(REQUEST_KEY_NOTELIST, ja.toString());


        JoyHttpClient.getInstance().execute(url, headers, jsonObject, new JoyHttpClient.JoyHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    LogUtil.e(TAG, "local server get friend note failed : code = " + code + ", errorMsg = " + errorMsg);
                    if (callback != null) {
                        callback.onFailed(""+code, errorMsg);
                    }
                    return;
                }

                try {
                    JSONObject resObj = JSONObject.parseObject(response);
                    String resCode = resObj.getString(RESULT_KEY_CODE).substring(4, 6);
                    if (resCode.equals(STATUS_CODE_SUCCESS)) {
                        callback.onSuccess(null);
                    } else {
                        String error = resObj.getString(RESULT_KEY_MSG);
                        callback.onFailed(resCode, error);
                    }
                } catch (JSONException e) {
                    callback.onFailed("-1", e.getMessage());
                }
            }
        }, true, true);
    }

    @Override
    public void updateLocalNoteList(String id, String token, final NoteHttpCallback<List<ContactExtInfo>> callback) {
        String url = JoyServers.joyServer() + API_FRIENDSNOTE_LIST_GET;
        Map<String, String> headers = new HashMap<>(1);
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");

        JSONObject body = new JSONObject();
        body.put(REQUEST_KEY_ID, id);
        body.put(REQUEST_KEY_TOKEN, token);

        JoyHttpClient.getInstance().execute(url, headers, body, new JoyHttpClient.JoyHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    LogUtil.e(TAG, "local server get friend note failed : code = " + code + ", errorMsg = " + errorMsg);
                    if (callback != null) {
                        callback.onFailed(""+code, errorMsg);
                    }
                    return;
                }

                try {
                    JSONObject resObj = JSONObject.parseObject(response);
                    String resCode = resObj.getString(RESULT_KEY_CODE).substring(4, 6);
                    if (resCode.equals(STATUS_CODE_SUCCESS)) {
                        JSONArray jsonArray = resObj.getJSONArray(RESULT_KEY_DATA);
                        List<ContactExtInfo> contactExtInfos = new ArrayList<ContactExtInfo>();
                        for (int i = 0;i < jsonArray.size(); i++) {
                            JSONObject jo = jsonArray.getJSONObject(i);
                            ContactExtInfo contactExtInfo = new ContactExtInfo();
                            contactExtInfo.setFriends_id(jo.getString("friends_id"));
                            contactExtInfo.setJo(jo.getString("jo"));
                            contactExtInfo.setNick(jo.getString("nick"));
                            contactExtInfo.setNote(jo.getString("note"));
                            contactExtInfos.add(contactExtInfo);
                            LogUtil.d(TAG, contactExtInfo.toString());
                        }
                        callback.onSuccess(contactExtInfos);
                    } else {
                        String error = resObj.getString(RESULT_KEY_MSG);
                        callback.onFailed(resCode, error);
                    }
                } catch (JSONException e) {
                    callback.onFailed("-1", e.getMessage());
                }
            }
        }, true, true);
    }
}
