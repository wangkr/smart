package com.cqyw.smart.common.http;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.cqyw.smart.R;
import com.cqyw.smart.config.AppContext;
import com.cqyw.smart.config.JoyServers;
import com.cqyw.smart.contact.extensioninfo.model.ExtensionInfo;
import com.cqyw.smart.contact.localcontact.LocalContact;
import com.cqyw.smart.location.model.NimLocation;
import com.cqyw.smart.main.model.CommentMessage;
import com.cqyw.smart.main.model.LikeMessage;
import com.cqyw.smart.main.model.ProvinceInfo;
import com.cqyw.smart.main.model.PublicSnapMessage;
import com.netease.nim.uikit.common.media.picker.joycamera.model.PublishMessage;
import com.cqyw.smart.main.model.RecentSnapNews;
import com.cqyw.smart.main.model.SchoolInfo;
import com.cqyw.smart.main.model.SnapMsgConstant;
import com.cqyw.smart.main.model.SnapNewTypeEnum;
import com.cqyw.smart.main.util.MainUtils;
import com.cqyw.smart.util.SystemTools;
import com.netease.nim.uikit.common.media.picker.joycamera.model.CamOnLineRes;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.nim.uikit.joycustom.upyun.JoyImageUtil;
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Kairong on 2015/11/9.
 * mail:wangkrhust@gmail.com
 */
public class JoyCommClient implements ICommProtocol{
    private static final String TAG = "JoyCommClient";

    public static String API_NAME_LOGOUT;
    public static String API_NAME_FRIENDS;
    public static String API_NAME_DELFRIENDS;
    public static String API_NAME_UPDATEINFO;
    public static String API_NAME_UPDATEEDU;
    public static String API_GET_EXTINFO;
    public static String API_USERINFO_REPORT;
    public static String API_LOCCONTACT_MATCH;
    public static String API_FEEDBACK_URL;
    public static String API_CHECK_UPDATE;
    public static String API_DOWN_NEWVERSION;
    public static String API_COMMENT_ADDLIKE;
    public static String API_GET_COMMENT;
    public static String API_ADD_COMMENT;
    public static String API_GETNEWBY_NID;
    public static String API_GETNEWS_LIST;
    public static String API_GETNEWS_OLDLIST;
    public static String API_DELETE_NEWS;
    public static String API_SEND_NEWS;
    public static String API_GETLIKESBY_NIDS;
    public static String API_NEWS_MARK;
    public static String API_NEWS_REPORT;
    public static String API_GET_RECENTNEW;
    public static String API_RECENTNEWS_MARK;
    public static String API_GET_PROVINCEINFO;
    public static String API_GET_SCHOOLINFO;
    public static String API_SET_UNMATCH;
    public static String API_SET_MATCH;
    public static String API_GET_MODELS;
    public static String API_COUNT_MODELS;

    private static JoyCommClient instance = null;

    public static JoyCommClient getInstance(){

        if(instance == null){
            synchronized (JoyCommClient.class) {
                if(instance == null) {
                    instance = new JoyCommClient();
                }
            }
        }
        return instance;
    }

    public static void init() {
        API_NAME_LOGOUT      = AppContext.getContext().getString(R.string.api_name_logout);
        API_NAME_FRIENDS     = AppContext.getContext().getString(R.string.api_name_friends);
        API_NAME_DELFRIENDS  = AppContext.getContext().getString(R.string.api_name_delfriends);
        API_NAME_UPDATEINFO  = AppContext.getContext().getString(R.string.api_name_updateinfo);
        API_NAME_UPDATEEDU   = AppContext.getContext().getString(R.string.api_name_updateedu);
        API_GET_EXTINFO      = AppContext.getContext().getString(R.string.api_get_extensioninfo);
        API_USERINFO_REPORT  = AppContext.getContext().getString(R.string.api_userinfo_report);
        API_LOCCONTACT_MATCH = AppContext.getContext().getString(R.string.api_loccontact_match);
        API_FEEDBACK_URL     = AppContext.getContext().getString(R.string.api_feedback_url);
        API_CHECK_UPDATE     = AppContext.getContext().getString(R.string.api_check_update);
        API_DOWN_NEWVERSION  = AppContext.getContext().getString(R.string.api_down_newversion);
        API_COMMENT_ADDLIKE  = AppContext.getContext().getString(R.string.api_comment_addlike);
        API_GET_COMMENT      = AppContext.getContext().getString(R.string.api_get_comment);
        API_ADD_COMMENT      = AppContext.getContext().getString(R.string.api_add_comment);
        API_GETNEWBY_NID     = AppContext.getContext().getString(R.string.api_getnewby_nid);
        API_GETNEWS_LIST     = AppContext.getContext().getString(R.string.api_getnews_list);
        API_GETNEWS_OLDLIST  = AppContext.getContext().getString(R.string.api_getnews_oldlist);
        API_DELETE_NEWS      = AppContext.getContext().getString(R.string.api_delete_news);
        API_SEND_NEWS        = AppContext.getContext().getString(R.string.api_send_news);
        API_GETLIKESBY_NIDS  = AppContext.getContext().getString(R.string.api_getlikesby_nids);
        API_NEWS_MARK        = AppContext.getContext().getString(R.string.api_news_mark);
        API_NEWS_REPORT      = AppContext.getContext().getString(R.string.api_news_report);
        API_GET_RECENTNEW    = AppContext.getContext().getString(R.string.api_get_recentnew);
        API_RECENTNEWS_MARK  = AppContext.getContext().getString(R.string.api_recentnews_mark);
        API_GET_PROVINCEINFO = AppContext.getContext().getString(R.string.api_get_provinceinfo);
        API_GET_SCHOOLINFO   = AppContext.getContext().getString(R.string.api_get_schoolinfo);
        API_SET_MATCH        = AppContext.getContext().getString(R.string.api_set_match);
        API_SET_UNMATCH      = AppContext.getContext().getString(R.string.api_set_unmatch);
        API_GET_MODELS       = AppContext.getContext().getString(R.string.api_get_models);
        API_COUNT_MODELS     = AppContext.getContext().getString(R.string.api_count_models);

    }

    private JoyCommClient(){
        JoyHttpClient.getInstance().init();
    }

    @Override
    public void setMatch(String id, String token, final boolean match, final CommCallback<Boolean> callback) {
        String url = JoyServers.joyServer() + ( match ? API_SET_MATCH : API_SET_UNMATCH);

        Map<String, String> headers = new HashMap<>(1);
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(REQUEST_KEY_ID, id);
        jsonObject.put(REQUEST_KEY_TOKEN, token);

        JoyHttpClient.getInstance().execute(url, headers, jsonObject, new JoyHttpClient.JoyHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    LogUtil.e(TAG, "set match failed : code = " + code + ", errorMsg = " + errorMsg);
                    if (callback != null) {
                        callback.onFailed("" + code, errorMsg);
                    }
                    return;
                }

                if (callback == null) {
                    return;
                }

                try {
                    JSONObject resObj = JSONObject.parseObject(response);
                    String resCode = resObj.getString(RESULT_KEY_CODE).substring(4, 6);
                    if (resCode.equals(STATUS_CODE_SUCCESS)) {
                        callback.onSuccess(match);
                    } else {
                        String error = resObj.getString(RESULT_KEY_MSG);
                        callback.onFailed(resCode, error);
                    }
                } catch (JSONException e) {
                    callback.onFailed("-1", e.getMessage());
                }
            }
        },true, true);
    }

    @Override
    public void getProvinceInfo(String id, String token, final CommCallback<List<ProvinceInfo>> callback) {
        String url = JoyServers.joyServer() + API_GET_PROVINCEINFO;

        Map<String, String> headers = new HashMap<>(1);
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(REQUEST_KEY_ID, id);
        jsonObject.put(REQUEST_KEY_TOKEN, token);

        JoyHttpClient.getInstance().execute(url, headers, jsonObject, new JoyHttpClient.JoyHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    LogUtil.e(TAG, "get province info failed : code = " + code + ", errorMsg = " + errorMsg);
                    if (callback != null) {
                        callback.onFailed("" + code, errorMsg);
                    }
                    return;
                }

                if (callback == null) {
                    return;
                }

                try {
                    JSONObject resObj = JSONObject.parseObject(response);
                    String resCode = resObj.getString(RESULT_KEY_CODE).substring(4, 6);
                    if (resCode.equals(STATUS_CODE_SUCCESS)) {
                        List<ProvinceInfo> provinceInfos = new ArrayList<ProvinceInfo>();
                        JSONArray ja = resObj.getJSONArray(RESULT_KEY_DATA);
                        for(int i = 0; i < ja.size(); i++) {
                            ProvinceInfo provinceInfo = ja.getObject(i, ProvinceInfo.class);
                            provinceInfos.add(provinceInfo);
                        }
                        callback.onSuccess(provinceInfos);
                    } else {
                        String error = resObj.getString(RESULT_KEY_MSG);
                        callback.onFailed(resCode, error);
                    }
                } catch (JSONException e) {
                    callback.onFailed("-1", e.getMessage());
                }
            }
        },true, true);
    }

    @Override
    public void getSchoolInfo(String id, String token, String pid, final CommCallback<List<SchoolInfo>> callback) {
        String url = JoyServers.joyServer() + API_GET_SCHOOLINFO;

        Map<String, String> headers = new HashMap<>(1);
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(REQUEST_KEY_ID, id);
        jsonObject.put(REQUEST_KEY_TOKEN, token);
        jsonObject.put(REQUEST_KEY_PID, pid);

        JoyHttpClient.getInstance().execute(url, headers, jsonObject, new JoyHttpClient.JoyHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    LogUtil.e(TAG, "get school info failed : code = " + code + ", errorMsg = " + errorMsg);
                    if (callback != null) {
                        callback.onFailed("" + code, errorMsg);
                    }
                    return;
                }

                if (callback == null) {
                    return;
                }

                try {
                    JSONObject resObj = JSONObject.parseObject(response);
                    String resCode = resObj.getString(RESULT_KEY_CODE).substring(4, 6);
                    if (resCode.equals(STATUS_CODE_SUCCESS)) {
                        List<SchoolInfo> schoolInfos = new ArrayList<SchoolInfo>();
                        JSONArray ja = resObj.getJSONArray(RESULT_KEY_DATA);
                        for(int i = 0; i < ja.size(); i++) {
                            SchoolInfo schoolInfo = ja.getObject(i, SchoolInfo.class);
                            schoolInfos.add(schoolInfo);
                        }
                        callback.onSuccess(schoolInfos);
                    } else {
                        String error = resObj.getString(RESULT_KEY_MSG);
                        callback.onFailed(resCode, error);
                    }
                } catch (JSONException e) {
                    callback.onFailed("-1", e.getMessage());
                }
            }
        },true, true);
    }

    @Override
    public void logout(String id, String token, final CommCallback<Void> callback) {
        String url = JoyServers.joyServer() + API_NAME_LOGOUT;

        Map<String, String> headers = new HashMap<>(1);
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(REQUEST_KEY_ID, id);
        jsonObject.put(REQUEST_KEY_TOKEN, token);

        JoyHttpClient.getInstance().execute(url, headers, jsonObject, new JoyHttpClient.JoyHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    LogUtil.e(TAG, "logout failed : code = " + code + ", errorMsg = " + errorMsg);
                    if (callback != null) {
                        callback.onFailed("" + code, errorMsg);
                    }
                    return;
                }

                if (callback == null) {
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
        },true, true);
    }

    @Override
    public void localcontactMatch(String id, String token, String phones,final CommCallback<List<LocalContact>> callback) {
        String url = JoyServers.joyServer() + API_LOCCONTACT_MATCH;
        Map<String, String> headers = new HashMap<>(1);
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(REQUEST_KEY_ID, id);
        jsonObject.put(REQUEST_KEY_TOKEN, token);
        jsonObject.put(REQUEST_KEY_PHONES, phones);

//        LogUtil.e(TAG, "local contact match  json=" + jsonObject.toString());

        JoyHttpClient.getInstance().execute(url, headers, jsonObject, new JoyHttpClient.JoyHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    LogUtil.e(TAG, "local contact match  failed : code = " + code + ", errorMsg = " + errorMsg);
                    if (callback != null) {
                        callback.onFailed("" + code, errorMsg);
                    }
                    return;
                }

                if (callback == null) {
                    return;
                }

                try {
                    JSONObject resObj = JSONObject.parseObject(response);
                    String resCode = resObj.getString(RESULT_KEY_CODE).substring(4, 6);
                    if (resCode.equals(STATUS_CODE_SUCCESS)) {
                        // 进行解析并封装成类数组
                        List<LocalContact> localContacts = new ArrayList<LocalContact>();
                        JSONArray dataObj = resObj.getJSONArray(RESULT_KEY_DATA);
                        for (int i = 0; i < dataObj.size(); i++) {
                            LocalContact localContact = dataObj.getObject(i, LocalContact.class);
                            localContacts.add(localContact);
                        }
                        callback.onSuccess(localContacts);

                    } else {
                        String errorMsg1 = resObj.getString(RESULT_KEY_MSG);
                        callback.onFailed(resCode, errorMsg1);
                        LogUtil.e(TAG, "local contact match  failed : code = " + code + ", errorMsg = " + errorMsg);
                    }
                } catch (JSONException e) {
                    callback.onFailed("-1", e.getMessage());
                    LogUtil.e(TAG, "local contact match  failed : code = " + code + ", errorMsg = " + errorMsg);
                }
            }
        },true, false);
    }

    /**
     * 删除好友成功
     * @param id
     * @param friend_id
     * @param token
     * @param callback
     */
    @Override
    public void delFriendInfo2Server(String id, String token, String friend_id, final CommCallback<Void> callback) {
        String url = JoyServers.joyServer() + API_NAME_DELFRIENDS;

        Map<String, String> headers = new HashMap<>(1);
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(REQUEST_KEY_ID, id);
        jsonObject.put(REQUEST_KEY_TOKEN, token);
        jsonObject.put(REQUEST_KEY_UID, friend_id);

        JoyHttpClient.getInstance().execute(url, headers, jsonObject, new JoyHttpClient.JoyHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    LogUtil.e(TAG, "find password failed : code = " + code + ", errorMsg = " + errorMsg);
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
        },true, false);
    }

    /**
     * 添加好友成功
     * @param id 添加用户id
     * @param friend_id 被添加用户id
     * @param token token
     * @param callback 回调
     */
    @Override
    public void addFriendInfo2Server(String id, String friend_id, String token, final CommCallback<Void> callback){
        String url = JoyServers.joyServer() + API_NAME_FRIENDS;

        Map<String, String> headers = new HashMap<>(1);
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(REQUEST_KEY_ID, id);
        jsonObject.put(REQUEST_KEY_TOKEN, token);
        jsonObject.put(REQUEST_KEY_UID, friend_id);

        JoyHttpClient.getInstance().execute(url, headers, jsonObject, new JoyHttpClient.JoyHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    LogUtil.e(TAG, "find password failed : code = " + code + ", errorMsg = " + errorMsg);
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
        },true, false);
    }

    @Override
    public void uploadResUsedtimes(String id, String token, Map<Integer, Integer> map,final CommCallback<Void> callback) {
        String url = JoyServers.joyServer() + API_COUNT_MODELS;

        Map<String, String> headers = new HashMap<>(1);
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");

        final JSONObject jsonObject = new JSONObject();
        jsonObject.put(REQUEST_KEY_ID, id);
        jsonObject.put(REQUEST_KEY_TOKEN, token);
//        int[][] data = new int[map.size()][2];
//        int i = 0;
        JSONArray jsonArray = new JSONArray();
        for(Integer i : map.keySet()){
            JSONObject jo = new JSONObject();
            jo.put(REQUEST_KEY_ID, i);
            jo.put(REQUEST_KEY_TIMES, map.get(i));
            jsonArray.add(jo);
        }
        jsonObject.put(REQUEST_KEY_DATA, jsonArray);

        LogUtil.d(TAG, jsonObject.toJSONString());

        JoyHttpClient.getInstance().execute(url, headers, jsonObject, new JoyHttpClient.JoyHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    LogUtil.e(TAG, "update userinfo to joyserver failed : code = " + code + ", errorMsg = " + errorMsg);
                    if (callback != null) {
                        callback.onFailed(code + "", errorMsg);
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
    public void getOnlineRes(String id, String token, final CommCallback<Map<String,List<CamOnLineRes>>> callback) {
        String url = JoyServers.joyServer() + API_GET_MODELS;

        Map<String, String> headers = new HashMap<>(1);
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");

        final JSONObject jsonObject = new JSONObject();
        jsonObject.put(REQUEST_KEY_ID, id);
        jsonObject.put(REQUEST_KEY_TOKEN, token);

        JoyHttpClient.getInstance().execute(url, headers, jsonObject, new JoyHttpClient.JoyHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    LogUtil.e(TAG, "get online res from joyserver failed : code = " + code + ", errorMsg = " + errorMsg);
                    if (callback != null) {
                        callback.onFailed(code + "", errorMsg);
                    }
                    return;
                }

                try {
                    JSONObject resObj = JSONObject.parseObject(response);
                    String resCode = resObj.getString(RESULT_KEY_CODE).substring(4, 6);
                    if (resCode.equals(STATUS_CODE_SUCCESS)) {
                        // 进行解析并封装成类数组
                        JSONArray jsonArray = resObj.getJSONArray(REQUEST_KEY_DATA);
                        Map<String, List<CamOnLineRes>> maps = new HashMap<String, List<CamOnLineRes>>();
                        maps.put("ar", new ArrayList<CamOnLineRes>());
                        maps.put("cover", new ArrayList<CamOnLineRes>());
                        for(int i = 0;i < jsonArray.size();i++){
                            JSONObject jo = jsonArray.getJSONObject(i);
                            CamOnLineRes colr = new CamOnLineRes();
                            colr.setId(jo.getIntValue("id"));
                            colr.setLocalIndex(jo.getIntValue("no"));
                            colr.setUrl(JoyImageUtil.getOnlineResAbsUrl(jo.getString("main")));
                            colr.setIconUrl(JoyImageUtil.getOnlineResAbsUrl(jo.getString("thumbnail")));
                            colr.setType(jo.getIntValue("stype") == 1 ? CamOnLineRes.Type.AR : CamOnLineRes.Type.COVER);
                            colr.setStatus(CamOnLineRes.Status.NONE);

                            maps.get(colr.getType() == CamOnLineRes.Type.AR ? "ar" : "cover").add(colr);
                        }

                        callback.onSuccess(maps);
                    } else {
                        String errorMsg1 = resObj.getString(RESULT_KEY_MSG);
                        callback.onFailed(resCode, errorMsg1);
                    }
                } catch (JSONException e) {
                    callback.onFailed("-1", e.getMessage());
                }
            }
        },true, false);

    }

    @Override
    public void getExtensionInfo(String id, String token, final String uid, final CommCallback<ExtensionInfo> callback) {;
        String url = JoyServers.joyServer() + API_GET_EXTINFO;

        Map<String, String> headers = new HashMap<>(1);
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(REQUEST_KEY_ID, id);
        jsonObject.put(REQUEST_KEY_TOKEN, token);
        jsonObject.put(REQUEST_KEY_UID, uid);

        JoyHttpClient.getInstance().execute(url, headers, jsonObject, new JoyHttpClient.JoyHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    LogUtil.e(TAG, "get userinfo from joyserver failed : code = " + code + ", errorMsg = " + errorMsg);
                    if (callback != null) {
                        callback.onFailed(code + "", errorMsg);
                    }
                    return;
                }

                try {
                    JSONObject resObj = JSONObject.parseObject(response);
                    String resCode = resObj.getString(RESULT_KEY_CODE).substring(4, 6);
                    if (resCode.equals(STATUS_CODE_SUCCESS)) {
                        JSONObject data = resObj.getJSONObject(RESULT_KEY_DATA);
                        ExtensionInfo extensionInfo = new ExtensionInfo();
                        extensionInfo.setId(uid);
                        extensionInfo.setPhotos(data.getString("photos"));
                        extensionInfo.setJo(data.getString("jo"));
                        extensionInfo.setNewsno(data.getString("newsno"));
                        callback.onSuccess(extensionInfo);
                    } else {
                        String error = resObj.getString(RESULT_KEY_MSG);
                        LogUtil.d(TAG, "get extensionInfo failed code="+resCode);
                        callback.onFailed(resCode, error);
                    }
                } catch (JSONException e) {
                    callback.onFailed("-1", e.getMessage());
                }
            }
        },true, true);

    }

    /**
     * 更新用户资料到本地服务器
     * @param id id
     * @param token token
     * @param userInfo 用户信息
     * @param callback 回调
     */
    @Override
    public void updateUserInfo2Server(String id, String token, Map<String, String> userInfo, final CommCallback<Void> callback) {
        String url = JoyServers.joyServer() + API_NAME_UPDATEINFO;

        Map<String, String> headers = new HashMap<>(1);
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(REQUEST_KEY_ID, id);
        jsonObject.put(REQUEST_KEY_TOKEN, token);
        jsonObject.putAll(userInfo);

        JoyHttpClient.getInstance().execute(url, headers, jsonObject, new JoyHttpClient.JoyHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    LogUtil.e(TAG, "update userinfo to joyserver failed : code = " + code + ", errorMsg = " + errorMsg);
                    if (callback != null) {
                        callback.onFailed(code + "", errorMsg);
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
        },true, true);
    }

    /**
     * 更新用户学校信息
     * @param id id
     * @param token token
     * @param userInfo 用户信息
     * @param callback 回调
     */
    @Override
    public void updateEduInfo2Server(String id, String token, Map<String, String> userInfo, final CommCallback<Void> callback) {
        String url = JoyServers.joyServer() + API_NAME_UPDATEEDU;

        Map<String, String> headers = new HashMap<>(1);
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(REQUEST_KEY_ID, id);
        jsonObject.put(REQUEST_KEY_TOKEN, token);
        jsonObject.putAll(userInfo);

//        LogUtil.d(TAG, jsonObject.toString());

        JoyHttpClient.getInstance().execute(url, headers, jsonObject, new JoyHttpClient.JoyHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    LogUtil.e(TAG, "update education info failed : code = " + code + ", errorMsg = " + errorMsg);
                    if (callback != null) {
                        callback.onFailed(code+"", errorMsg);
                    }
                    return;
                }

                try {
                    JSONObject resObj = JSONObject.parseObject(response);
                    String resCode = resObj.getString(RESULT_KEY_CODE).substring(4, 6);
                    LogUtil.d(TAG, "update education result:"+resObj.toString());
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
        },true, true);
    }

    /**
     * 发送反馈
     * @param id id
     * @param token token
     * @param content 反馈内容
     * @param callback 回调
     */
    @Override
    public void sendFeedback(String id, String token, String content,final CommCallback<Void> callback) {
        String url = JoyServers.joyServer() + API_FEEDBACK_URL;
        Map<String, String> headers = new HashMap<>(1);
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(REQUEST_KEY_ID, id);
        jsonObject.put(REQUEST_KEY_TOKEN, token);
        jsonObject.put(REQUEST_KEY_CONTENT, content);
        jsonObject.put(REQUEST_KEY_DISPLAY, SystemTools.getDisplayVersion());
        jsonObject.put(REQUEST_KEY_VERSION, SystemTools.getAppVersionName(AppContext.getContext()));
        jsonObject.put(REQUEST_KEY_SDKVERSION, SystemTools.getSDKVersion());


        JoyHttpClient.getInstance().execute(url, headers, jsonObject, new JoyHttpClient.JoyHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    LogUtil.e(TAG, "send feedback failed : code = " + code + ", errorMsg = " + errorMsg);
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
        },true, true);
    }

    @Override
    public void checkUpdate(String id, String token, String vid, String vcode, final CommCallback<JSONArray> callback) {
        String url = JoyServers.joyServer() + API_CHECK_UPDATE;
        Map<String, String> headers = new HashMap<>(1);
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(REQUEST_KEY_ID, id);
        jsonObject.put(REQUEST_KEY_TOKEN, token);
        jsonObject.put(REQUEST_KEY_VID, vid);
        jsonObject.put(REQUEST_KEY_VCODE, vcode);

        JoyHttpClient.getInstance().execute(url, headers, jsonObject, new JoyHttpClient.JoyHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    LogUtil.e(TAG, "check update failed : code = " + code + ", errorMsg = " + errorMsg);
                    if (callback != null) {
                        callback.onFailed(""+code, errorMsg);
                    }
                    return;
                }

                try {
                    JSONObject resObj = JSONObject.parseObject(response);
                    String resCode = resObj.getString(RESULT_KEY_CODE).substring(4, 6);
                    if (resCode.equals(STATUS_CODE_SUCCESS)) {
                        JSONArray data = resObj.getJSONArray(RESULT_KEY_DATA);
                        callback.onSuccess(data);
                    } else if (resCode.equals(STATUS_CODE_NEWEASTVISION)) {
                        callback.onFailed(resCode, "已是最新版本");
                    } else {
                        String errorMsg1 = resObj.getString(RESULT_KEY_MSG);
                        callback.onFailed(resCode, errorMsg1);
                    }
                } catch (JSONException e) {
                    callback.onFailed("-1", e.getMessage());
                }
            }
        },true, true);
    }

    @Override
    public void addLike(String id, String token, String uid, String nid, String cover, final CommCallback<Void> callback) {
        String url = JoyServers.joyServer() + API_COMMENT_ADDLIKE;
        Map<String, String> headers = new HashMap<>(1);
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(REQUEST_KEY_ID, id);
        jsonObject.put(REQUEST_KEY_TOKEN, token);
        jsonObject.put(SnapMsgConstant.MSG_KEY_COVER, cover);
        jsonObject.put(REQUEST_KEY_UID, uid);
        jsonObject.put(REQUEST_KEY_NID, nid);

        JoyHttpClient.getInstance().execute(url, headers, jsonObject, new JoyHttpClient.JoyHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    LogUtil.e(TAG, "add like failed : code = " + code + ", errorMsg = " + errorMsg);
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
                    } else if (resCode.equals(STATUS_CODE_NONEXIST)) {
                        callback.onFailed(STATUS_CODE_NONEXIST, "新鲜事已被删除");
                    } else {
                        String errorMsg1 = resObj.getString(RESULT_KEY_MSG);
                        callback.onFailed(resCode, errorMsg1);
                    }
                } catch (JSONException e) {
                    callback.onFailed("-1", e.getMessage());
                }
            }
        },true, true);
    }

    @Override
    public void getSnapnewsByNid(String id, String token, String nid, final CommCallback<PublicSnapMessage> callback) {
        String url = JoyServers.joyServer() + API_GETNEWBY_NID;
        Map<String, String> headers = new HashMap<>(1);
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(REQUEST_KEY_ID, id);
        jsonObject.put(REQUEST_KEY_TOKEN, token);
        jsonObject.put(REQUEST_KEY_NID, nid);

//        LogUtil.d(TAG, "url="+url+" body="+jsonObject.toString());

        JoyHttpClient.getInstance().execute(url, headers, jsonObject, new JoyHttpClient.JoyHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    LogUtil.e(TAG, "get snapnews bynid failed : code = " + code + ", errorMsg = " + errorMsg);
                    if (callback != null) {
                        callback.onFailed(""+code, errorMsg);
                    }
                    return;
                }

                try {
                    JSONObject resObj = JSONObject.parseObject(response);
                    String resCode = resObj.getString(RESULT_KEY_CODE).substring(4, 6);
                    if (resCode.equals(STATUS_CODE_SUCCESS)) {
                        PublicSnapMessage snapMessage = resObj.getObject(REQUEST_KEY_DATA, PublicSnapMessage.class);
                        snapMessage.setMsgStatus(MsgStatusEnum.success);
                        snapMessage.setAttachStatus(AttachStatusEnum.def);
                        snapMessage.setDirection(MsgDirectionEnum.In);
                        callback.onSuccess(snapMessage);
                    } else {
                        String errorMsg1 = resObj.getString(RESULT_KEY_MSG);
                        callback.onFailed(resCode, errorMsg1);
                    }
                } catch (JSONException e) {
                    callback.onFailed("-1", e.getMessage());
                }
            }
        },true, true);
    }

    @Override
    public void deleteSnapnews(String id, String token, String nid, final CommCallback<Void> callback) {
        String url = JoyServers.joyServer() + API_DELETE_NEWS;
        Map<String, String> headers = new HashMap<>(1);
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(REQUEST_KEY_ID, id);
        jsonObject.put(REQUEST_KEY_TOKEN, token);
        jsonObject.put(REQUEST_KEY_NID, nid);


        JoyHttpClient.getInstance().execute(url, headers, jsonObject, new JoyHttpClient.JoyHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    LogUtil.e(TAG, "delete snapnews failed : code = " + code + ", errorMsg = " + errorMsg);
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
                        String errorMsg1 = resObj.getString(RESULT_KEY_MSG);
                        callback.onFailed(resCode, errorMsg1);
                    }
                } catch (JSONException e) {
                    callback.onFailed("-1", e.getMessage());
                }
            }
        },true, true);
    }

    @Override
    public void sendSnapnews(String id, String token, PublicSnapMessage message, final CommCallback<String> callback) {
        String url = JoyServers.joyServer() + API_SEND_NEWS;
        Map<String, String> headers = new HashMap<>(1);
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(REQUEST_KEY_ID, id);
        jsonObject.put(REQUEST_KEY_TOKEN, token);
        jsonObject.put(SnapMsgConstant.MSG_KEY_LAT, message.getLat());
        jsonObject.put(SnapMsgConstant.MSG_KEY_LNG, message.getLng());
        jsonObject.put(SnapMsgConstant.MSG_KEY_CONTENT, message.getContent());
        jsonObject.put(SnapMsgConstant.MSG_KEY_COVER, message.getCover());
        jsonObject.put(SnapMsgConstant.MSG_KEY_SMART, message.getSmart());
        jsonObject.put(SnapMsgConstant.MSG_KEY_TYPE, message.getType());


        JoyHttpClient.getInstance().execute(url, headers, jsonObject, new JoyHttpClient.JoyHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    LogUtil.e(TAG, "send snapnews failed : code = " + code + ", errorMsg = " + errorMsg);
                    if (callback != null) {
                        callback.onFailed(""+code, errorMsg);
                    }
                    return;
                }

                if (callback == null) {
                    return;
                }

                try {
                    JSONObject resObj = JSONObject.parseObject(response);
                    String resCode = resObj.getString(RESULT_KEY_CODE).substring(4, 6);
                    if (resCode.equals(STATUS_CODE_SUCCESS)) {
                        JSONObject data = resObj.getJSONObject(REQUEST_KEY_DATA);
                        callback.onSuccess(data.getString(REQUEST_KEY_NID));
                    } else {
                        String errorMsg1 = resObj.getString(RESULT_KEY_MSG);
                        callback.onFailed(resCode, errorMsg1);
                    }
                } catch (JSONException e) {
                    callback.onFailed("-1", e.getMessage());
                }
            }
        },true, true);
    }

    @Override
    public void getSnapnews(String id, String token, String nid, boolean isTopNid, NimLocation location,final CommCallback<List<PublicSnapMessage>> callback) {
        String url = JoyServers.joyServer() + (isTopNid?API_GETNEWS_LIST:API_GETNEWS_OLDLIST);
        Map<String, String> headers = new HashMap<>(1);
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");

        JSONObject jsonObject = new JSONObject();

        jsonObject.put(REQUEST_KEY_ID, id);
        jsonObject.put(REQUEST_KEY_TOKEN, token);
        jsonObject.put(isTopNid ? REQUEST_KEY_TNID : REQUEST_KEY_BNID, nid);

        String[] pos = MainUtils.getLastKnownLocation();
        if (pos == null) {
            callback.onFailed("-100", "无法获取定位");
            return;
        }
        jsonObject.put(SnapMsgConstant.MSG_KEY_LAT, pos[0]);
        jsonObject.put(SnapMsgConstant.MSG_KEY_LNG, pos[1]);


        JoyHttpClient.getInstance().execute(url, headers, jsonObject, new JoyHttpClient.JoyHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    LogUtil.e(TAG, "get snapnews failed : code = " + code + ", errorMsg = " + errorMsg);
                    if (callback != null) {
                        callback.onFailed("" + code, errorMsg);
                    }
                    return;
                }

                if (callback == null) {
                    return;
                }

                try {
                    JSONObject resObj = JSONObject.parseObject(response);
                    String resCode = resObj.getString(RESULT_KEY_CODE).substring(4, 6);
                    if (resCode.equals(STATUS_CODE_SUCCESS)) {
                        // 进行解析并封装成类数组
                        JSONArray jsonArray = resObj.getJSONArray(REQUEST_KEY_DATA);
                        List<PublicSnapMessage> snapMessages = new ArrayList<PublicSnapMessage>(jsonArray.size());
                        for(int i = 0;i < jsonArray.size();i++){
                            PublicSnapMessage snapMessage = jsonArray.getObject(i, PublicSnapMessage.class);
                            snapMessage.setMsgStatus(MsgStatusEnum.success);
                            snapMessage.setAttachStatus(AttachStatusEnum.def);
                            snapMessage.setDirection(MsgDirectionEnum.In);
                            snapMessages.add(snapMessage);
                        }
                        callback.onSuccess(snapMessages);
                    } else {
                        String errorMsg1 = resObj.getString(RESULT_KEY_MSG);
                        callback.onFailed(resCode, errorMsg1);
                    }
                } catch (JSONException e) {
                    callback.onFailed("-1", e.getMessage());
                }
            }
        },true, false);
    }

    @Override
    public void getLikesByNids(String id, String token, final String nids, final CommCallback<List<LikeMessage>> callback) {
        if (TextUtils.isEmpty(nids)) {
            callback.onFailed("-1", "新鲜事nids为空");
            return;
        }

        String url = JoyServers.joyServer() + API_GETLIKESBY_NIDS;
        Map<String, String> headers = new HashMap<>(1);
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(REQUEST_KEY_ID, id);
        jsonObject.put(REQUEST_KEY_TOKEN, token);
        jsonObject.put(REQUEST_KEY_NIDS, nids);

        JoyHttpClient.getInstance().execute(url, headers, jsonObject, new JoyHttpClient.JoyHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    LogUtil.e(TAG, "get likes by nids  failed : code = " + code + ", errorMsg = " + errorMsg);
                    if (callback != null) {
                        callback.onFailed("" + code, errorMsg);
                    }
                    return;
                }

                if (callback == null) {
                    return;
                }

                try {
                    JSONObject resObj = JSONObject.parseObject(response);
                    String resCode = resObj.getString(RESULT_KEY_CODE).substring(4, 6);
                    if (resCode.equals(STATUS_CODE_SUCCESS)) {
                        // 进行解析并封装成类数组
                        String[] keys = nids.split(" ");
                        JSONObject dataObj = resObj.getJSONObject(RESULT_KEY_DATA);
                        List<LikeMessage> likeMsgs = new ArrayList<LikeMessage>();
                        for (int i = 0; i < keys.length; i++) {
                            LikeMessage likeMessage = dataObj.getObject(keys[i], LikeMessage.class);
                            likeMessage.setNid(keys[i]);
                            likeMessage.reverseUids();
                            likeMsgs.add(likeMessage);
                        }
                        callback.onSuccess(likeMsgs);

                    } else {
                        String errorMsg1 = resObj.getString(RESULT_KEY_MSG);
                        callback.onFailed(resCode, errorMsg1);
                    }
                } catch (JSONException e) {
                    callback.onFailed("-1", e.getMessage());
                }
            }
        },true, true);
    }

    @Override
    public void getRecentNews(String id, String token, final CommCallback<List<RecentSnapNews>> callback) {
        String url = JoyServers.joyServer() + API_GET_RECENTNEW;
        Map<String, String> headers = new HashMap<>(1);
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(REQUEST_KEY_ID, id);
        jsonObject.put(REQUEST_KEY_TOKEN, token);

        JoyHttpClient.getInstance().execute(url, headers, jsonObject, new JoyHttpClient.JoyHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    LogUtil.e(TAG, "get recent news  failed : code = " + code + ", errorMsg = " + errorMsg);
                    if (callback != null) {
                        callback.onFailed("" + code, errorMsg);
                    }
                    return;
                }

                if (callback == null) {
                    return;
                }

                try {
                    JSONObject resObj = JSONObject.parseObject(response);
                    String resCode = resObj.getString(RESULT_KEY_CODE).substring(4, 6);
                    if (resCode.equals(STATUS_CODE_SUCCESS)) {
                        // 进行解析并封装成类数组
                        int total = 0;
                        List<RecentSnapNews> recentNewsList = new ArrayList<RecentSnapNews>();
                        JSONObject dataObj = resObj.getJSONObject(RESULT_KEY_DATA);
                        JSONArray commentObj = dataObj.getJSONArray("comment");
                        for (int i = 0; i < commentObj.size(); i++) {
                            RecentSnapNews recentSnapNews = commentObj.getObject(i, RecentSnapNews.class);
                            recentSnapNews.setType(SnapNewTypeEnum.COMMENT.getValue());
                            recentSnapNews.setIntime(TimeUtil.getNowDateTime("yyyy-MM-dd HH:mm:ss"));
                            recentNewsList.add(recentSnapNews);
                            total++;
                        }
                        JSONArray likeObj = dataObj.getJSONArray("like");
                        for (int i = 0; i < likeObj.size(); i++) {
                            RecentSnapNews recentSnapNews = likeObj.getObject(i, RecentSnapNews.class);
                            recentSnapNews.setType(SnapNewTypeEnum.LIKE.getValue());
                            recentSnapNews.setIntime(TimeUtil.getNowDateTime("yyyy-MM-dd HH:mm:ss"));
                            recentNewsList.add(recentSnapNews);
                            total++;
                        }
                        JSONArray warnObj = dataObj.getJSONArray("warning");
                        for (int i = 0; i < warnObj.size(); i++) {
                            RecentSnapNews recentSnapNews = new RecentSnapNews();
                            recentSnapNews.setContent(warnObj.getString(i));
                            recentSnapNews.setType(SnapNewTypeEnum.WARNING.getValue());
                            recentSnapNews.setIntime(TimeUtil.getNowDateTime("yyyy-MM-dd HH:mm:ss"));
                            recentNewsList.add(recentSnapNews);
                            total++;
                        }
                        JSONArray ansObj = dataObj.getJSONArray("answer");
                        for (int i = 0; i < ansObj.size(); i++) {
                            RecentSnapNews recentSnapNews = new RecentSnapNews();
                            recentSnapNews.setContent(ansObj.getString(i));
                            recentSnapNews.setType(SnapNewTypeEnum.ANSWER.getValue());
                            recentSnapNews.setIntime(TimeUtil.getNowDateTime("yyyy-MM-dd HH:mm:ss"));
                            recentNewsList.add(recentSnapNews);
                            total++;
                        }
                        callback.onSuccess(recentNewsList);

                    } else {
                        String errorMsg1 = resObj.getString(RESULT_KEY_MSG);
                        callback.onFailed(resCode, errorMsg1);
                    }
                } catch (JSONException e) {
                    callback.onFailed("-1", e.getMessage());
                }
            }
        },true, false);
    }

    @Override
    public void getComments(String id, String token, String nid, String bottomCid, final CommCallback<List<CommentMessage>> callback) {
        String url = JoyServers.joyServer() + API_GET_COMMENT;
        Map<String, String> headers = new HashMap<>(1);
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(REQUEST_KEY_ID, id);
        jsonObject.put(REQUEST_KEY_TOKEN, token);
        jsonObject.put(REQUEST_KEY_NID, nid);
        jsonObject.put(REQUEST_KEY_BCID, bottomCid);


        JoyHttpClient.getInstance().execute(url, headers, jsonObject, new JoyHttpClient.JoyHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    LogUtil.e(TAG, "get comments  failed : code = " + code + ", errorMsg = " + errorMsg);
                    if (callback != null) {
                        callback.onFailed("" + code, errorMsg);
                    }
                    return;
                }

                if (callback == null) {
                    return;
                }

                try {
                    JSONObject resObj = JSONObject.parseObject(response);
                    String resCode = resObj.getString(RESULT_KEY_CODE).substring(4, 6);
                    if (resCode.equals(STATUS_CODE_SUCCESS)) {
                        // 进行解析并封装成类数组
                        JSONArray dataObj = resObj.getJSONArray(RESULT_KEY_DATA);
                        List<CommentMessage> commentMessages = new ArrayList<CommentMessage>();
                        for (int i = 0; i < dataObj.size(); i++) {
                            CommentMessage commentMessage = dataObj.getObject(i, CommentMessage.class);
                            commentMessages.add(commentMessage);
                        }
                        callback.onSuccess(commentMessages);

                    } else {
                        String errorMsg1 = resObj.getString(RESULT_KEY_MSG);
                        callback.onFailed(resCode, errorMsg1);
                    }
                } catch (JSONException e) {
                    callback.onFailed("-1", e.getMessage());
                }
            }
        },true, false);
    }

    @Override
    public void addComment(String id, String token, String cover, CommentMessage commentMessage,final CommCallback<Integer> callback) {
        String url = JoyServers.joyServer() + API_ADD_COMMENT;
        Map<String, String> headers = new HashMap<>(1);
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(REQUEST_KEY_ID, id);
        jsonObject.put(REQUEST_KEY_TOKEN, token);
        jsonObject.put(SnapMsgConstant.MSG_KEY_COVER, cover);
        jsonObject.put(REQUEST_KEY_NID, commentMessage.getNid());
        jsonObject.put(REQUEST_KEY_CONTENT, commentMessage.getContent());
        jsonObject.put(REQUEST_KEY_NUID, commentMessage.getNuid());

        if (!TextUtils.isEmpty(commentMessage.getAted())) {
            jsonObject.put(REQUEST_KEY_ATED, commentMessage.getAted());
        }


        JoyHttpClient.getInstance().execute(url, headers, jsonObject, new JoyHttpClient.JoyHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    LogUtil.e(TAG, "send snapnews failed : code = " + code + ", errorMsg = " + errorMsg);
                    if (callback != null) {
                        callback.onFailed(""+code, errorMsg);
                    }
                    return;
                }

                if (callback == null) {
                    return;
                }

                try {
                    JSONObject resObj = JSONObject.parseObject(response);
                    String resCode = resObj.getString(RESULT_KEY_CODE).substring(4, 6);
                    if (resCode.equals(STATUS_CODE_SUCCESS)) {
                        JSONObject data = resObj.getJSONObject(REQUEST_KEY_DATA);
                        callback.onSuccess(data.getInteger(REQUEST_KEY_CID));
                    } else if (resCode.equals(STATUS_CODE_NONEXIST)) {
                        callback.onFailed(STATUS_CODE_NONEXIST, "新鲜事已被删除");
                    } else {
                        String errorMsg1 = resObj.getString(RESULT_KEY_MSG);
                        callback.onFailed(resCode, errorMsg1);
                    }
                } catch (JSONException e) {
                    callback.onFailed("-1", e.getMessage());
                }
            }
        },true, true);
    }

    @Override
    public void reportNews(String id, String token, String nid, String uid, String content,final CommCallback<Void> callback) {
        String url = JoyServers.joyServer() + API_NEWS_REPORT;
        Map<String, String> headers = new HashMap<>(1);
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(REQUEST_KEY_ID, id);
        jsonObject.put(REQUEST_KEY_TOKEN, token);
        jsonObject.put(REQUEST_KEY_NID, nid);
        jsonObject.put(REQUEST_KEY_UID, uid);
        jsonObject.put(REQUEST_KEY_CONTENT, content);

        JoyHttpClient.getInstance().execute(url, headers, jsonObject, new JoyHttpClient.JoyHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    LogUtil.e(TAG, "report news failed : code = " + code + ", errorMsg = " + errorMsg);
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
                        String errorMsg1 = resObj.getString(RESULT_KEY_MSG);
                        callback.onFailed(resCode, errorMsg1);
                    }
                } catch (JSONException e) {
                    callback.onFailed("-1", e.getMessage());
                }
            }
        },true, true);
    }

    @Override
    public void reportUser(String id, String token, String uid,String content, final CommCallback<Void> callback) {
        String url = JoyServers.joyServer() + API_USERINFO_REPORT;
        Map<String, String> headers = new HashMap<>(1);
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(REQUEST_KEY_ID, id);
        jsonObject.put(REQUEST_KEY_TOKEN, token);
        jsonObject.put(REQUEST_KEY_UID, uid);
        jsonObject.put(REQUEST_KEY_CONTENT, content);


        JoyHttpClient.getInstance().execute(url, headers, jsonObject, new JoyHttpClient.JoyHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    LogUtil.e(TAG, "report user failed : code = " + code + ", errorMsg = " + errorMsg);
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
                        String errorMsg1 = resObj.getString(RESULT_KEY_MSG);
                        callback.onFailed(resCode, errorMsg1);
                    }
                } catch (JSONException e) {
                    callback.onFailed("-1", e.getMessage());
                }
            }
        },true, true);
    }

    @Override
    public void markRecentNews(String id, String token,final CommCallback<Void> callback) {
        String url = JoyServers.joyServer() + API_RECENTNEWS_MARK;
        Map<String, String> headers = new HashMap<>(1);
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(REQUEST_KEY_ID, id);
        jsonObject.put(REQUEST_KEY_TOKEN, token);

        JoyHttpClient.getInstance().execute(url, headers, jsonObject, new JoyHttpClient.JoyHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    LogUtil.e(TAG, "mark recentnews failed : code = " + code + ", errorMsg = " + errorMsg);
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
                        String errorMsg1 = resObj.getString(RESULT_KEY_MSG);
                        callback.onFailed(resCode, errorMsg1);
                    }
                } catch (JSONException e) {
                    callback.onFailed("-1", e.getMessage());
                }
            }
        },true, false);
    }

    @Override
    public void markSnapnews(String id, String token, String nids,final CommCallback<Void> callback) {
        String url = JoyServers.joyServer() + API_NEWS_MARK;
        Map<String, String> headers = new HashMap<>(1);
        headers.put(HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded; charset=utf-8");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(REQUEST_KEY_ID, id);
        jsonObject.put(REQUEST_KEY_TOKEN, token);
        jsonObject.put(REQUEST_KEY_NIDS, nids);

        JoyHttpClient.getInstance().execute(url, headers, jsonObject, new JoyHttpClient.JoyHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    LogUtil.e(TAG, "mark snapnews failed : code = " + code + ", errorMsg = " + errorMsg);
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
                        String errorMsg1 = resObj.getString(RESULT_KEY_MSG);
                        callback.onFailed(resCode, errorMsg1);
                    }
                } catch (JSONException e) {
                    callback.onFailed("-1", e.getMessage());
                }
            }
        },true, false);
    }
}
