package com.cqyw.smart.common.http;

import com.alibaba.fastjson.JSONArray;
import com.cqyw.smart.contact.extensioninfo.model.ExtensionInfo;
import com.cqyw.smart.contact.localcontact.LocalContact;
import com.cqyw.smart.location.model.NimLocation;
import com.cqyw.smart.main.model.CommentMessage;
import com.cqyw.smart.main.model.LikeMessage;
import com.cqyw.smart.main.model.ProvinceInfo;
import com.cqyw.smart.main.model.PublicSnapMessage;
import com.cqyw.smart.main.model.PublishSnapMessage;
import com.cqyw.smart.main.model.RecentSnapNews;
import com.cqyw.smart.main.model.SchoolInfo;

import java.util.List;
import java.util.Map;

/**
 * Created by Kairong on 2015/11/9.
 * mail:wangkrhust@gmail.com
 */
public interface ICommProtocol extends JoyHttpProtocol {
    interface CommCallback<T>{
        void onSuccess(T t);
        void onFailed(String code, String errorMsg);
    }
    // 获取学校信息
    void getProvinceInfo(String id, String token, CommCallback<List<ProvinceInfo>> callback);
    void getSchoolInfo(String id, String token, String pid, CommCallback<List<SchoolInfo>> callback);

    // 登出
    void logout(String accout, String token, CommCallback<Void> callback);

    // 好友关系接口
    void addFriendInfo2Server(String id, String friend_id, String token, CommCallback<Void> callback);
    void delFriendInfo2Server(String id, String friend_id, String token, CommCallback<Void> callback);
    void updateUserInfo2Server(String id, String token, Map<String, String> userInfo, CommCallback<Void> callback);
    void updateEduInfo2Server(String id, String token, Map<String, String> userInfo, CommCallback<Void> callback);
    void getExtensionInfo(String id, String token, String uid, CommCallback<ExtensionInfo> callback);
    void localcontactMatch(String id, String token, String phones, CommCallback<List<LocalContact>> callback);
    void setMatch(String id, String token, boolean match, CommCallback<Boolean> callback);

    // 反馈和更新接口
    void sendFeedback(String id, String token, String content, CommCallback<Void> callback);
    void checkUpdate(String id, String token, String vid, String vcode, CommCallback<JSONArray> callback);

    // 举报接口
    void reportUser(String id, String token, String uid, String content, CommCallback<Void> callback);
    void reportNews(String id, String token, String nid, String uid, String content, CommCallback<Void> callback);

    // 新鲜事操作接口
    void addLike(String id, String token, String uid, String nid, String cover, CommCallback<Void> callback);
    void sendSnapnews(String id, String token, PublishSnapMessage message, CommCallback<String> callback);
    void getSnapnewsByNid(String id, String token, String nid, CommCallback<PublicSnapMessage> callback);
    void getSnapnews(String id, String token, String nid, boolean isTopNid, NimLocation location, CommCallback<List<PublicSnapMessage>> callback);
    void deleteSnapnews(String id, String token, String nid, CommCallback<Void> callback);
    void getLikesByNids(String id, String token, String nids, CommCallback<List<LikeMessage>> callback);
    void getRecentNews(String id ,String token, CommCallback<List<RecentSnapNews>> callback);
    void addComment(String id, String token, String cover, CommentMessage commentMessage, CommCallback<Integer> callback);
    void getComments(String id, String token, String nid, String bottomCid, CommCallback<List<CommentMessage>> callback);
    void markRecentNews(String id ,String token, CommCallback<Void> callback);
    void markSnapnews(String id, String token, String nid, CommCallback<Void> callback);
}
