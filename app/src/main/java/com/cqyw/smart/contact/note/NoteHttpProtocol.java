package com.cqyw.smart.contact.note;

import com.alibaba.fastjson.JSONArray;
import com.cqyw.smart.common.http.JoyHttpProtocol;

import java.util.List;
import java.util.Map;

/**
 * 好友备注http协议实现
 * Created by Kairong on 2015/10/29.
 * mail:wangkrhust@gmail.com
 */
public interface NoteHttpProtocol extends JoyHttpProtocol {
    void updateRemoteNoteList(String id, String token, List<Map<String, Object>> noteItem, NoteHttpCallback<Void> callback);
    void updateLocalNoteList(String id, String token, NoteHttpCallback<List<ContactExtInfo>> callback);
}
