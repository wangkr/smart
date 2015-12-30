package com.cqyw.smart.main.service;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cqyw.smart.main.database.AccountDBOpenHelper;
import com.cqyw.smart.main.model.PublicSnapMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kairong on 2015/11/21.
 * mail:wangkrhust@gmail.com
 */
public class MessageMarkDBService {
    private AccountDBOpenHelper accountDBOpenHelper;
    public MessageMarkDBService(Context context) {
        accountDBOpenHelper = new AccountDBOpenHelper(context);
    }

    public void saveMarks(Map<String, String> nids) {
        SQLiteDatabase db = accountDBOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (String nid : nids.keySet()) {
                db.execSQL("replace into message_mark (nid, uid) values (?, ?)", new Object[]{nid,nids.get(nid)});
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        db.close();
    }

    public Map<String, String> findAllMarks() {
        SQLiteDatabase db = accountDBOpenHelper.getWritableDatabase();
        Map<String ,String> nids = new HashMap<>();
        Cursor cursor = db.rawQuery("select * from message_mark", null);
        while (cursor.moveToNext()) {
            nids.put(cursor.getString(cursor.getColumnIndex("nid")), cursor.getString(cursor.getColumnIndex("uid")));
        }
        cursor.close();
        db.close();
        return nids;
    }

    public void deleteAll() {
        SQLiteDatabase db = accountDBOpenHelper.getWritableDatabase();
        db.execSQL("delete from message_mark");
        db.close();
    }
}
