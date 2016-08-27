package com.cqyw.smart.main.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cqyw.smart.main.database.AccountDBOpenHelper;
import com.cqyw.smart.main.model.PublicSnapMessage;
import com.netease.nim.uikit.common.media.picker.joycamera.model.PublishMessage;
import com.cqyw.smart.main.model.SnapMsgConstant;

import java.util.List;

/**
 * 用户自己发表的snapnews数据库缓存
 * Created by Kairong on 2015/11/19.
 * mail:wangkrhust@gmail.com
 */
public class MySnapMessageDBService {
    private AccountDBOpenHelper accountDBOpenHelper;
    public MySnapMessageDBService(Context context) {
        accountDBOpenHelper = new AccountDBOpenHelper(context);
    }

    public String saveNewPublishSnapMessage(PublishMessage message) {
        String id = "";
        SQLiteDatabase db = accountDBOpenHelper.getWritableDatabase();
        db.execSQL("insert into my_snap_message( id, lat, lng, content, cover, smart, status, _type, local_path)" +
                "values(NULL, ?, ?, ?, ?, ?, ?, ?, ?)", new Object[]{message.getLat(), message.getLng(), message.getContent(),
                message.getCover(), message.getSmart(), message.getStatus(), message.getType().value(), message.getLocalPath()});
        Cursor cursor = db.rawQuery("select last_insert_rowid() as newid from my_snap_message", null);
        if (cursor.moveToNext()) {
            id = cursor.getInt(cursor.getColumnIndex("newid"))+"";
        }
        cursor.close();
        db.close();

        return id;
    }

    public void deleteMyPublicSnapMessageById(String id) {
        SQLiteDatabase db = accountDBOpenHelper.getWritableDatabase();
        db.execSQL("delete from my_snap_message where id=?",
                new Object[] { Integer.valueOf(id) });
        db.close();
    }

    public void updateMyPublicSnapMessageById (String id, int status) {
        SQLiteDatabase db = accountDBOpenHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from my_snap_message where id=?", new String[]{id});

        if (cursor.moveToFirst()) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(SnapMsgConstant.MSG_KEY_STATUS, status);
            db.update("my_snap_message", contentValues, "where id=?", new String[]{id});
        }

        cursor.close();
        db.close();
    }
}
