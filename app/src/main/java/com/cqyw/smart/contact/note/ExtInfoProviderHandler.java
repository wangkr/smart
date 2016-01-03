package com.cqyw.smart.contact.note;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import com.cqyw.smart.config.AppCache;
import com.cqyw.smart.config.AppContext;
import com.cqyw.smart.contact.FriendNoteClient;
import com.cqyw.smart.contact.helper.FriendNoteDBOpenHelper;
import com.cqyw.smart.contact.helper.ExtInfoHelper;
import com.cqyw.smart.util.StringUtils;
import com.netease.nim.uikit.cache.NimUserInfoCache;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.joycustom.FriendExtInfoProvider;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.friend.constant.FriendFieldEnum;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Kairong on 2015/11/1.
 * mail:wangkrhust@gmail.com
 */
public class ExtInfoProviderHandler implements FriendExtInfoProvider {

    private static Map<String, UserNameInfo> acount2Friend = new HashMap<>();
    private boolean isEdit = false;

    public static ExtInfoProviderHandler getInstance(){
        return InstanceHolder.instance;
    }

    public void buildCache(){
        SQLiteDatabase db = new FriendNoteDBOpenHelper(AppContext.getContext()).getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from note", null);
        while(cursor.moveToNext()){
            acount2Friend.put(cursor.getString(cursor.getColumnIndex("friend_id")),
                    new UserNameInfo(cursor.getString(cursor.getColumnIndex("friend_nick")),
                            cursor.getString(cursor.getColumnIndex("note")),
                            cursor.getString(cursor.getColumnIndex("university")),
                            cursor.getString(cursor.getColumnIndex("jo"))));

            // 网易备注更新
            Map<FriendFieldEnum, Object> map = new HashMap<>();
            map.put(FriendFieldEnum.ALIAS, cursor.getString(cursor.getColumnIndex("note")));
            NIMClient.getService(FriendService.class).updateFriendFields(cursor.getString(cursor.getColumnIndex("friend_id")), map);
        }
        cursor.close();
        db.close();
    }

    public void clear(){
        acount2Friend.clear();
    }

    public boolean isEdit(){
        return isEdit;
    }

    @Override
    public void fetchNoteInfofromRemote() {
        FriendNoteClient.getInstance().updateLocalNoteList(AppCache.getJoyId(), AppCache.getJoyToken(), new NoteHttpCallback<List<ContactExtInfo>>() {
            @Override
            public void onSuccess(List<ContactExtInfo> notes) {
                SQLiteDatabase db = new FriendNoteDBOpenHelper(AppContext.getContext()).getWritableDatabase();
                try {
                    db.beginTransaction();
                    for (ContactExtInfo contactExtInfo : notes) {
                        db.execSQL("replace into note (friend_id, friend_nick, note, jo) values(?,?,?,?)", new Object[]{contactExtInfo.getFriends_id(), contactExtInfo.getNick()
                                , contactExtInfo.getNote(), contactExtInfo.getJo()});
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                db.close();
                buildCache();
            }

            @Override
            public void onFailed(String code, String errorMsg) {
                LogUtil.d("NoteCacheHandler", "好友备注初始化失败:" + errorMsg + " 错误码:" + code);
            }
        });
    }

    /**
     * 添加备注
     * @param context
     * @param friend_id
     * @param note
     */
    @Override
    public boolean addNote(Context context, String friend_id, String friend_nick, String note, String university, String jo){
        SQLiteDatabase db = new FriendNoteDBOpenHelper(context).getWritableDatabase();
        if(friend_id == null){
            db.close();
            return false;
        }
        Cursor cursor = db.rawQuery("select * from note where friend_id = ?", new String[]{friend_id});
        // 判断数据库是否存在
        if(!cursor.moveToFirst()){
            // 不存在
            db.execSQL("insert into note (friend_id, friend_nick, note, university, jo) values(?,?,?,?,?)", new String[]{friend_id,
                    friend_nick == null ? StringUtils.Empty : friend_nick,
                    note == null ? StringUtils.Empty : note, university == null ? StringUtils.Empty:university, jo});
            acount2Friend.put(friend_id, new UserNameInfo(friend_nick, note, university, jo));
            isEdit = true;
            cursor.close();
            db.close();
            return true;
        } else {
            // 更新数据库
            ContentValues noteContent = new ContentValues();
            noteContent.put("note", note == null ? StringUtils.Empty : note);
            db.update("note", noteContent, "friend_id = ?", new String[]{friend_id});
            // 更新缓存
            UserNameInfo userNameInfo = acount2Friend.get(friend_id);
            userNameInfo.note = note;
            acount2Friend.put(friend_id, userNameInfo);
            isEdit = true;
        }
        cursor.close();
        db.close();
        return  false;
    }

    /**
     * 删除好友备注
     * 删除好友时调用
     * @param context
     * @param friend_id
     */
    @Override
    public boolean deleteNote(Context context, String friend_id){
        SQLiteDatabase db = new FriendNoteDBOpenHelper(context).getWritableDatabase();
        if(TextUtils.isEmpty(friend_id)){
            db.close();
            return false;
        }

        // 先删除缓存
        acount2Friend.remove(friend_id);
        db.execSQL("delete from note where friend_id=?", new Object[]{friend_id});
        db.close();
        return true;
    }

    /**
     * 更新昵称
     * @param context
     * @param friend_id
     * @param friend_nick
     */
    @Override
    public boolean updateNick(Context context, String friend_id, String friend_nick){
        SQLiteDatabase db = new FriendNoteDBOpenHelper(context).getWritableDatabase();
        if(TextUtils.isEmpty(friend_id)){
            db.close();
            return false;
        }
        Cursor cursor = db.rawQuery("select * from note where friend_id = ?",new String[]{friend_id});
        // 判断数据库是否存在
        if(cursor.moveToFirst()){
            // 更新数据库
            ContentValues nickContent = new ContentValues();
            nickContent.put("friend_nick", friend_nick == null ? StringUtils.Empty : friend_nick);
            db.update("note", nickContent, "friend_id = ?", new String[]{friend_id});
            // 更新缓存
            UserNameInfo userNameInfo = acount2Friend.get(friend_id);
            userNameInfo.note = friend_nick;
            acount2Friend.put(friend_id, userNameInfo);
            cursor.close();
            db.close();
            return true;
        }
        cursor.close();
        db.close();
        return false;
    }

    /**
     * 更新备注
     * @param context
     * @param friend_id
     * @param note
     * @return 更新状态
     */
    @Override
    public boolean updateNote(Context context, String friend_id, String note){
        SQLiteDatabase db = new FriendNoteDBOpenHelper(context).getWritableDatabase();
        if(TextUtils.isEmpty(friend_id)){
            db.close();
            return false;
        }
        Cursor cursor = db.rawQuery("select * from note where friend_id = ?",new String[]{friend_id});
        // 判断数据库是否存在
        if(cursor.moveToFirst()){
            // 更新数据库
            ContentValues noteContent = new ContentValues();
            noteContent.put("note", note == null ? StringUtils.Empty : note);
            db.update("note", noteContent, "friend_id = ?", new String[]{friend_id});
            // 更新缓存
            UserNameInfo userNameInfo = acount2Friend.get(friend_id);
            if (userNameInfo != null) {
                userNameInfo.note = note;
                acount2Friend.put(friend_id, userNameInfo);
                isEdit = true;
            }
            cursor.close();
            db.close();
            return true;
        } else {
            // 插入数据库
            String friend_nick = NimUserInfoCache.getInstance().getUserName(friend_id);
            String university = ExtInfoHelper.getUniversity(friend_id);
            db.execSQL("insert into note (friend_id, friend_nick, note, university) values(?,?,?,?)", new String[]{friend_id,
                    friend_nick == null ? StringUtils.Empty : friend_nick,
                    note == null ? StringUtils.Empty : note, university == null ? StringUtils.Empty:university});
            acount2Friend.put(friend_id, new UserNameInfo(friend_nick, note, university, "0"));
            isEdit = true;
            cursor.close();
            db.close();
            return true;
        }
    }

    /**
     * 获取好友备注,优先从缓存获取
     * @param friend_id
     * @return
     */
    @Override
    public String getNote(String friend_id){
        // 从缓存读取
        if(acount2Friend.containsKey(friend_id)){
            return acount2Friend.get(friend_id).note;
        }

        return null;
    }

    /**
     * 获取好友昵称
     * @param friend_id
     * @return
     */
    @Override
    public String getNick(String friend_id){
        // 从缓存读取
        if(acount2Friend.containsKey(friend_id)){
            return acount2Friend.get(friend_id).nick;
        }
        return null;
    }

    /**
     * 获取学校
     * @param friend_id
     * @return
     */
    @Override
    public String getUniversity(String friend_id) {
        // 从缓存读取
        if(acount2Friend.containsKey(friend_id)){
            return acount2Friend.get(friend_id).university;
        }
        return null;
    }

    @Override
    public String getJo(String friend_id) {
        // 从缓存读取
        if(acount2Friend.containsKey(friend_id)){
            return acount2Friend.get(friend_id).jo;
        }

        return null;
    }

    static class InstanceHolder{
        private final static ExtInfoProviderHandler instance = new ExtInfoProviderHandler();
    }
}
