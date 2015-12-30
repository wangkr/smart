package com.cqyw.smart.main.service;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cqyw.smart.main.database.MsgCommentDBOpenHelper;
import com.cqyw.smart.main.model.CommentMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kairong on 2015/11/17.
 * mail:wangkrhust@gmail.com
 */
public class CommentMessageDBService {
    private MsgCommentDBOpenHelper msgCommentDBOpenHelper;
    public CommentMessageDBService(Context context) {
        msgCommentDBOpenHelper = new MsgCommentDBOpenHelper(context);
    }

    public void saveCommentMessage(CommentMessage message) {
        SQLiteDatabase db = msgCommentDBOpenHelper.getWritableDatabase();
        db.execSQL("insert into public_snap_comment (id, nid, uid, content, intime, ated) values(?, ?, ?, ?, ?, ?)",
                new Object[]{Integer.valueOf(message.getId()), message.getNid(), message.getUid(),
                        message.getContent(), message.getIntime(), message.getAted()});
        db.close();
    }

    public void saveCommentMessageList(List<CommentMessage> messages) {
        SQLiteDatabase db = msgCommentDBOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (CommentMessage message : messages) {
                db.execSQL("replace into public_snap_comment (id, nid, uid, content, intime, ated) values(?, ?, ?, ?, ?, ?)",
                        new Object[]{Integer.valueOf(message.getId()), message.getNid(), message.getUid(),
                                message.getContent(), message.getIntime(), message.getAted()});
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        db.close();
    }

    public void deleteCommentMessageByNid(String nid) {
        SQLiteDatabase db = msgCommentDBOpenHelper.getWritableDatabase();
        db.execSQL("delete from public_snap_comment where nid=?",
                new Object[] { nid });
        db.close();
    }

    public void deleteAllCommentMessage() {
        SQLiteDatabase db = msgCommentDBOpenHelper.getWritableDatabase();
        db.execSQL("delete from public_snap_comment");
        db.close();
    }

    public List<CommentMessage> findCommentMessagesByNid(String nid) {
        SQLiteDatabase db = msgCommentDBOpenHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(
                "select * from public_snap_comment where nid=? order by id asc",
                new String[]{nid});

        List<CommentMessage> messages = new ArrayList<>();
        while (cursor.moveToNext()) {
            CommentMessage commentMessage = new CommentMessage();
            commentMessage.setNid(nid);
            commentMessage.setId(cursor.getInt(cursor.getColumnIndex("id")) + "");
            commentMessage.setContent(cursor.getString(cursor.getColumnIndex("content")));
            commentMessage.setUid(cursor.getString(cursor.getColumnIndex("uid")));
            commentMessage.setIntime(cursor.getString(cursor.getColumnIndex("intime")));
            commentMessage.setAted(cursor.getString(cursor.getColumnIndex("ated")));
            messages.add(commentMessage);
        }
        cursor.close();
        db.close();

        return messages;
    }
}
