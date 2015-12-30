package com.cqyw.smart.main.service;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.cqyw.smart.main.database.MsgLikeDBOpenHelper;
import com.cqyw.smart.main.model.LikeMessage;
import com.cqyw.smart.main.model.PublicSnapMessage;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Kairong on 2015/11/17.
 * mail:wangkrhust@gmail.com
 */
public class LikeMessageDBService {
    private MsgLikeDBOpenHelper msgLikeDBOpenHelper;
    public LikeMessageDBService(Context context) {
        msgLikeDBOpenHelper = new MsgLikeDBOpenHelper(context);
    }

    public void saveLikeMessage(LikeMessage message) {
        SQLiteDatabase db = msgLikeDBOpenHelper.getWritableDatabase();
        for (String uid:message.getUids()) {
            // 采取去重插入法
            db.execSQL("replace into public_snap_like (id ,nid,uid) values(NULL, ?, ?)", new Object[]{message.getNid(), uid});
        }
        db.close();
    }

    public void saveLikeMsgsByPublicSnapMsgs (List<PublicSnapMessage> publicSnapMessages) {
        SQLiteDatabase db = msgLikeDBOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (PublicSnapMessage publicSnapMessage : publicSnapMessages) {
                LikeMessage likeMessage = publicSnapMessage.getLikeMessage();
                for (String uid:likeMessage.getUids()) {
                    // 采取去重插入法
                    db.execSQL("replace into public_snap_like (id ,nid,uid) values(NULL, ?, ?)", new Object[]{likeMessage.getNid(), uid});
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        db.close();
    }

    public void saveLikeMessageList(List<LikeMessage> messages) {
        SQLiteDatabase db = msgLikeDBOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (LikeMessage message : messages) {
                // 插入没有被删除的新鲜事的赞
                if (!TextUtils.equals("-1", message.getStatus()))
                for (String uid : message.getUids()) {
                    db.execSQL("replace into public_snap_like (id, nid,uid) values(NULL, ?, ?)", new Object[]{message.getNid(), uid});
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        db.close();
    }

    public void deleteLikeMessageByNid(String nid) {
        SQLiteDatabase db = msgLikeDBOpenHelper.getWritableDatabase();
        db.execSQL("delete from public_snap_like where nid=?",
                new Object[] { nid });
        db.close();
    }

    public void deleteAllLikeMessage() {
        SQLiteDatabase db = msgLikeDBOpenHelper.getWritableDatabase();
        db.execSQL("delete from public_snap_like");
        db.close();
    }

    public LikeMessage findLikeMessageByNid(String nid) {
        SQLiteDatabase db = msgLikeDBOpenHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(
                "select * from public_snap_like where nid=? order by id desc",
                new String[]{nid});

        LikeMessage likeMessage = new LikeMessage();
        likeMessage.setNid(nid);
        List<String> uids = new ArrayList<>();
        while (cursor.moveToNext()) {
            uids.add(cursor.getString(cursor.getColumnIndex("uid")));
        }
        likeMessage.setUids(uids);
        cursor.close();
        db.close();

        return likeMessage;
    }

    public List<LikeMessage> findLikeMessageListByNids(String _nids) {
        String[] nids = _nids.split(" ");
        SQLiteDatabase db = msgLikeDBOpenHelper.getWritableDatabase();
        List<LikeMessage> messages = new ArrayList<>();
        for(String nid:nids) {
            Cursor cursor = db.rawQuery(
                    "select * from public_snap_like where nid=? order by id desc",
                    new String[]{nid});
            LikeMessage likeMessage = new LikeMessage();
            likeMessage.setNid(nid);
            List<String> uids = new ArrayList<>();
            while (cursor.moveToNext()) {
                uids.add(cursor.getString(cursor.getColumnIndex("uid")));
            }

            likeMessage.setUids(uids);
            messages.add(likeMessage);
            cursor.close();
        }

        return messages;
    }
}
